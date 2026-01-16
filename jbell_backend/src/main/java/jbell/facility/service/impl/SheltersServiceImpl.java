package jbell.facility.service.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jbell.common.dto.SafetyDataResponse;
import jbell.facility.dto.SheltersDTO;
import jbell.facility.eunm.ApiType;
import jbell.facility.mapper.SheltersMapper;
import jbell.facility.service.SheltersService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class SheltersServiceImpl implements SheltersService {

    private final SheltersMapper sheltersMapper;
    private final WebClient safetyDataWebClient;

    @Value("${VITE_API_SHELTER_TEMPORARY_HOUSING_KEY}") private String imsiKey;
    @Value("${VITE_API_SHELTER_HEAT_KEY}") private String heatKey;
    @Value("${VITE_API_SHELTER_COLD_WAVE}") private String coldKey;
    @Value("${VITE_API_SHELTER_EARTHQUAKE1}") private String quakeKey;
    @Value("${VITE_API_SHELTER_CIVIL_DEFENSE_NUCLEAR}") private String nuclearKey;
    @Value("${VITE_API_SHELTER_CIVIL_DEFENSE_DISASTER}") private String civilKey;

    public SheltersServiceImpl(SheltersMapper sheltersMapper, 
                               @Qualifier("safetyDataWebClient") WebClient safetyDataWebClient) {
        this.sheltersMapper = sheltersMapper;
        this.safetyDataWebClient = safetyDataWebClient;
    }

    @Override
    public void syncAllShelters() {
        log.info("▶▶▶ 대피소 통합 동기화 프로세스 시작 (전체 페이지 순회)");
        
        Flux.fromIterable(Arrays.asList(ApiType.values()))
            .flatMap(this::syncOneApiReactive) // 6개 API 병렬 실행 시작
            .subscribe(
                null,
                error -> log.error("!!! 동기화 중 치명적 오류: {}", error.getMessage()),
                () -> log.info("▶▶▶ 모든 대피소 데이터 동기화 완료")
            );
    }

    // 특정 API를 1페이지부터 끝까지 가져오는 진입점
    private Mono<Void> syncOneApiReactive(ApiType type) {
        return recursiveFetch(type, 1);
    }

    // 재귀적으로 페이지를 호출하는 핵심 로직
    private Mono<Void> recursiveFetch(ApiType type, int pageNo) {
        return fetchPage(type, pageNo)
            .flatMap(response -> {
                List<Map<String, Object>> items = response.getBody();
                
                // 1. 현재 페이지 데이터 필터링 및 DB 저장 (비동기)
                Mono<Void> saveProcess = processAndSave(items, type, pageNo);
                
                // 2. 다음 페이지 존재 여부 확인 (보통 1000건 꽉 차면 다음 페이지가 있음)
                if (items != null && items.size() >= 1000) {
                    log.info("[{}] {}페이지 완료 -> 다음 {}페이지 요청", type.apiId, pageNo, pageNo + 1);
                    return saveProcess.then(recursiveFetch(type, pageNo + 1));
                } else {
                    log.info("[{}] 수집 완료 (마지막 페이지: {})", type.apiId, pageNo);
                    return saveProcess;
                }
            })
            .onErrorResume(e -> {
                log.error("[{}] {}페이지 호출 실패: {}", type.apiId, pageNo, e.getMessage());
                return Mono.empty(); // 에러 발생 시 해당 API는 중단하고 다음 API 진행
            });
    }

    private Mono<SafetyDataResponse> fetchPage(ApiType type, int pageNo) {
        String key = getServiceKeyByType(type);
        // URL 직접 조립 (인코딩 방지)
        String fullUrl = String.format(
            "https://www.safetydata.go.kr/V2/api/%s?serviceKey=%s&numOfRows=1000&pageNo=%d&returnType=json",
            type.apiId, key, pageNo
        );

        return safetyDataWebClient.get()
            .uri(fullUrl)
            .retrieve()
            .bodyToMono(SafetyDataResponse.class)
            .timeout(Duration.ofSeconds(30))
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)));
    }

    private Mono<Void> processAndSave(List<Map<String, Object>> items, ApiType type, int pageNo) {
        if (items == null || items.isEmpty()) return Mono.empty();

        return Mono.fromRunnable(() -> {
            try {
                List<SheltersDTO> dtoList = items.stream()
                    .map(item -> {
                        String fullAddr = safeString(item.get(type.addrField));
                        if(fullAddr.isEmpty()) fullAddr = safeString(item.get("DTL_ADRES"));
                        return new Object[]{item, fullAddr};
                    })
                    // 주소에 '전북' 또는 '전라북도'가 포함된 데이터만 추출
                    .filter(obj -> {
                        String addr = (String) obj[1];
                        return addr.contains("전북") || addr.contains("전라북도");
                    })
                    .map(obj -> convertToDto((Map<String, Object>) obj[0], (String) obj[1], type))
                    .collect(Collectors.toList());

                if (!dtoList.isEmpty()) {
                    sheltersMapper.upsertShelters(dtoList);
                    log.info("[{}] {}페이지: 전북 데이터 {}건 저장", type.apiId, pageNo, dtoList.size());
                }
            } catch (Exception e) {
                log.error("[{}] 저장 중 오류: {}", type.apiId, e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private SheltersDTO convertToDto(Map<String, Object> item, String fullAddr, ApiType type) {
        String[] addrParts = fullAddr.split(" ");
        Double lat, lon;
        if ("DMS".equals(type.latField)) {
            lat = calculateDegree(item.get("LAT_PROVIN"), item.get("LAT_MIN"), item.get("LAT_SEC"));
            lon = calculateDegree(item.get("LOT_PROVIN"), item.get("LOT_MIN"), item.get("LOT_SEC"));
        } else {
            lat = safeDouble(item.get(type.latField));
            lon = safeDouble(item.get(type.lonField));
        }

        return SheltersDTO.builder()
            .fcltNm(safeString(item.get(type.nameField)))
            .fcltSeCd(type.apiId)
            .ctpvNm(addrParts.length > 0 ? addrParts[0] : "")
            .sggNm(addrParts.length > 1 ? addrParts[1] : "")
            .roadNmAddr(fullAddr)
            .lat(lat).lot(lon)
            .opnYn("N".equals(safeString(item.get("OPN_YN"))) ? "N" : "Y")
            .useYn("Y")
            .build();
    }

    private String getServiceKeyByType(ApiType type) {
        return switch (type) {
            case SHELTER_IMSI -> imsiKey;
            case SHELTER_HEAT -> heatKey;
            case SHELTER_COLD -> coldKey;
            case SHELTER_EARTHQUAKE -> quakeKey;
            case SHELTER_NUCLEAR -> nuclearKey;
            case SHELTER_CIVIL -> civilKey;
        };
    }

    private String safeString(Object obj) { return obj == null ? "" : String.valueOf(obj).trim(); }
    private Double safeDouble(Object obj) {
        try { return obj == null ? 0.0 : Double.parseDouble(String.valueOf(obj)); }
        catch (Exception e) { return 0.0; }
    }
    private Double calculateDegree(Object prov, Object min, Object sec) {
        try {
            double p = Double.parseDouble(String.valueOf(prov));
            double m = Double.parseDouble(String.valueOf(min));
            double s = Double.parseDouble(String.valueOf(sec));
            return p + (m / 60.0) + (s / 3600.0);
        } catch (Exception e) { return 0.0; }
    }

    @Override
    public void syncOneApi(String apiUrl, String serviceKey, ApiType type) {
        syncOneApiReactive(type).subscribe();
    }
}