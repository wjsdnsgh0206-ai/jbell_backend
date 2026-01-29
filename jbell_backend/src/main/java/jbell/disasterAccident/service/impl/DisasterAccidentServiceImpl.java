package jbell.disasterAccident.service.impl;

import java.nio.charset.Charset;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import jbell.disasterAccident.dto.DisasterAccidentDTO;
import jbell.disasterAccident.mapper.DisasterAccidentMapper;
import jbell.disasterAccident.service.DisasterAccident;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@Slf4j
public class DisasterAccidentServiceImpl implements DisasterAccident {

    private final WebClient publicDataWebClient; // 공공데이터용 (apis.data.go.kr)
    private final WebClient apihubDataWebClient; // 기상청 API허브용 (apihub.kma.go.kr)
    private final DisasterAccidentMapper mapper;

    public DisasterAccidentServiceImpl(
            @Qualifier("publicDataWebClient") WebClient publicDataWebClient, 
            @Qualifier("apihubDataWebClient") WebClient apihubDataWebClient, 
            DisasterAccidentMapper disasterAccidentMapper) {
        this.publicDataWebClient = publicDataWebClient;
        this.apihubDataWebClient = apihubDataWebClient;
        this.mapper = disasterAccidentMapper;
    }

    @Value("${publicdata.servicekey}")
    private String serviceKey;

    @Value("${apihubdata.servicekey}")
    private String apiHubKey;

    @Override
    public Mono<Void> fetchAndSaveForestFireRisk() {
        return Mono.defer(() -> {
            java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
                    .fromHttpUrl("http://apis.data.go.kr/1400377/forestPoint/forestPointListGeongugSearch")
                    .queryParam("ServiceKey", serviceKey)
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", "10")
                    .queryParam("_type", "json")
                    .queryParam("excludeForecast", "0")
                    .build(true).toUri();

            return publicDataWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMapIterable(node -> {
                    JsonNode items = node.path("response").path("body").path("items").path("item");
                    if (items.isMissingNode()) return java.util.Collections.emptyList();
                    return items.isArray() ? (Iterable<JsonNode>) items::elements 
                                         : java.util.Collections.singletonList(items);
                })
                .doOnNext(item -> {
                    try {
                        // [기존 테이블 매핑 전략]
                        // 1. fire_id: 날짜(20260128) + 지역코드 등을 조합하여 숫자형태 생성
                        String dateStr = item.path("analdate").asText().replaceAll("[^0-9]", "").substring(0, 10);
                        long virtualId = Long.parseLong(dateStr); 

                        DisasterAccidentDTO dto = DisasterAccidentDTO.builder()
                            .fireId(virtualId)                             // PK 충족
                            .fireDamageArea(item.path("meanavg").asDouble()) // 평균지수를 면적 컬럼에 임시 저장
                            .fireLocVillage(item.path("doname").asText())   // 발생장소_시도
                            .fireStartTime(item.path("analdate").asText() + ":00:00") // 발생일시 (DATETIME 형식 맞춤)
                            .fireEndTime(item.path("analdate").asText() + ":00:00")   // 종료일시 (필수값 충족)
                            .fireCause("산불위험예보")                        // 발생원인에 구분값 기록
                            .build();

                        mapper.insertForestFire(dto); // 기존 매퍼 메서드 재사용
                    } catch (Exception e) {
                        log.error("기존 테이블 매핑 저장 에러: {}", e.getMessage());
                    }
                })
                .then();
        });
    }
    
    @Override
    public Mono<Void> fetchAndSaveEarthquake(String startDate) {
        return apihubDataWebClient.get()
            .uri(uriBuilder -> {
                uriBuilder.path("/typ01/url/eqk_now.php");
                // 파라미터가 있을 때만 tm 추가
                if (startDate != null && !startDate.isEmpty()) {
                    uriBuilder.queryParam("tm", startDate);
                }
                uriBuilder.queryParam("authKey", apiHubKey);	
                return uriBuilder.build();
            })
            .retrieve()
            .bodyToMono(byte[].class)
            // 1. 타임아웃을 60초로 대폭 늘림 (기상청 서버 지연 대비)
            .timeout(java.time.Duration.ofSeconds(60)) 
            // 2. 에러 발생 시 2초 간격으로 최대 3번 재시도
            .retryWhen(reactor.util.retry.Retry.fixedDelay(3, java.time.Duration.ofSeconds(2))
                .doBeforeRetry(retrySignal -> log.warn("지진 API 재시도 중... (시도 횟수: {})", retrySignal.totalRetries() + 1)))
            .map(bytes -> new String(bytes, Charset.forName("EUC-KR")))
            .flatMap(data -> {
                if (data == null || data.trim().isEmpty()) return Mono.empty();
                log.info("지진 데이터 수신 성공!");
                
                String[] lines = data.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("#") || trimmed.isEmpty() || trimmed.contains("TP")) continue;
                    try {
                        String content = trimmed.split("=")[0];
                        String[] parts = content.split("\\s+");
                        if (parts.length < 7) continue;

                        int lonIndex = content.indexOf(parts[6]) + parts[6].length();
                        String remaining = content.substring(lonIndex).trim();
                        String[] details = remaining.split(",");

                        DisasterAccidentDTO dto = DisasterAccidentDTO.builder()
                            .tmFc(parts[1])
                            .seq(Long.parseLong(parts[2]))
                            .mt(Double.parseDouble(parts[4]))
                            .lat(Double.parseDouble(parts[5]))
                            .lon(Double.parseDouble(parts[6]))
                            .loc(details[0].trim())
                            .rem(details.length > 2 ? details[2].trim() : "")
                            .build();
                        mapper.insertEarthquake(dto);
                    } catch (Exception e) {
                        log.error("지진 파싱 에러: {}", e.getMessage());
                    }
                }
                return Mono.empty();
            })
            .onErrorResume(e -> {
                log.error("지진 API 최종 연결 실패 (타임아웃 가능성 높음): {}", e.getMessage());
                return Mono.empty();
            })
            .then();
    }

    @Override
    @Transactional
    public Mono<Void> fetchAndSaveTyphoon(String year) {
        log.info("{}년 태풍 데이터 수집 시작...", year);
        
        return apihubDataWebClient.get() // [수정] 기상청 전용 클라이언트 사용
            .uri(uriBuilder -> uriBuilder
                .path("/typ01/url/typ_lst.php")
                .queryParam("YY", year)
                .queryParam("authKey", apiHubKey)
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .flatMap(data -> {
                String[] lines = data.split("\n");
                return Flux.fromArray(lines)
                    .filter(line -> !line.startsWith("#") && !line.trim().isEmpty() && !line.contains("YY"))
                    .concatMap(line -> {
                        String[] cols = line.trim().split("\\s+");
                        try {
                            DisasterAccidentDTO info = DisasterAccidentDTO.builder()
                                .typhoonYear(Integer.parseInt(cols[0]))
                                .typhoonNo(Integer.parseInt(cols[1]))
                                .typhoonActiveYn(cols[2].equals("1") ? "Y" : "N")
                                .typhoonName(cols[6])
                                .typhoonNameDesc(cols.length > 8 ? cols[8] : "")
                                .build();
                            mapper.insertTyphoonInfo(info);
                            return fetchAndSaveTyphoonTrack(cols[0], cols[1]);
                        } catch (Exception e) {
                            log.error("태풍 목록 파싱 에러: {}", line);
                            return Mono.empty();
                        }
                    })
                    .then();
            })
            .onErrorResume(e -> Mono.empty());
    }

    private Mono<Void> fetchAndSaveTyphoonTrack(String yy, String typ) {
        return apihubDataWebClient.get() // [수정] 기상청 전용 클라이언트 사용
            .uri(uriBuilder -> uriBuilder
                .path("/typ01/url/typ_data.php")
                .queryParam("YY", yy)
                .queryParam("mode", "1")
                .queryParam("authKey", apiHubKey)
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .timeout(java.time.Duration.ofSeconds(30))
            .doOnNext(data -> {
                String[] lines = data.split("\n");
                for (String line : lines) {
                    if (line.startsWith("#") || line.trim().isEmpty() || line.contains("FT")) continue;
                    String[] cols = line.trim().split("\\s+");
                    if (cols[0].equals("0")) { 
                        try {
                            DisasterAccidentDTO track = DisasterAccidentDTO.builder()
                                .typhoonYear(Integer.parseInt(cols[1]))
                                .typhoonNo(Integer.parseInt(cols[2]))
                                .typhoonReportNo(Integer.parseInt(cols[3]))
                                .typhoonAnalysisDatetime(cols[5])
                                .typhoonLat(Double.parseDouble(cols[7]))
                                .typhoonLon(Double.parseDouble(cols[8]))
                                .typhoonMoveSpeed(Double.parseDouble(cols[10]))
                                .typhoonCentralPressure(Integer.parseInt(cols[11]))
                                .typhoonMaxWindSpeed(Double.parseDouble(cols[12]))
                                .typhoonRadius15ms(Integer.parseInt(cols[13]))
                                .typhoonLocation(cols[cols.length-1])
                                .build();
                            mapper.insertTyphoonTrack(track);
                        } catch (Exception e) { log.error("태풍 경로 저장 에러"); }
                    }
                }
            })
            .onErrorResume(e -> Mono.empty())
            .then();
    }

    @Override
    public Mono<Void> fetchAndSaveLandslide() {
        return Mono.defer(() -> {
            java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
                    .fromHttpUrl("https://apis.data.go.kr/1400000/predictionInfoService/predictionInfoList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("_type", "json")
                    .build(true).toUri();

            return publicDataWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMapIterable(node -> {
                    JsonNode items = node.path("response").path("body").path("items").path("item");
                    if (items.isMissingNode()) return java.util.Collections.emptyList();
                    return items.isArray() ? (Iterable<JsonNode>) items::elements 
                                         : java.util.Collections.singletonList(items);
                })
                .doOnNext(item -> {
                    // [로그 분석 기반 수정] 실제 API 필드명으로 교체
                    String frcstNm = item.path("lndslFrcstNm").asText();    // 주의보/경보
                    String sggNm = item.path("sgg").asText();              // 지역명
                    String rawDate = item.path("prctnInfoAnlssDt").asText(); // 분석일시

                    if (rawDate == null || rawDate.isEmpty()) {
                        log.warn("산사태 데이터 날짜 누락: {}", item);
                        return;
                    }

                    try {
                        DisasterAccidentDTO dto = DisasterAccidentDTO.builder()
                            .lnldFrcstNm(frcstNm)
                            .sggNm(sggNm)
                            .predcAnlsDt(rawDate) // 이미 "2025-10-25 13:00:00" 형식이므로 바로 저장 가능
                            .build();

                        mapper.insertLandslide(dto);
                        log.info("산사태 데이터 저장 완료: {} {}", sggNm, frcstNm);
                    } catch (Exception e) { 
                        log.error("산사태 DB 저장 에러: {}", e.getMessage()); 
                    }
                })
                .then();
        });
    }
}

