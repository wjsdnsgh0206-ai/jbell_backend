package jbell.disasterAccident.service.impl;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

	public DisasterAccidentServiceImpl(@Qualifier("publicDataWebClient") WebClient publicDataWebClient,
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

	// =================================================
	// 산불
	// =================================================
	@Override
	public Mono<Void> fetchAndSaveForestFireRisk() {
		return Mono.defer(() -> {
			java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
					.fromHttpUrl("http://apis.data.go.kr/1400377/forestPoint/forestPointListGeongugSearch")
					.queryParam("ServiceKey", serviceKey).queryParam("pageNo", "1").queryParam("numOfRows", "10")
					.queryParam("_type", "json").queryParam("excludeForecast", "0").build(true).toUri();

			return publicDataWebClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class).flatMapIterable(node -> {
				JsonNode items = node.path("response").path("body").path("items").path("item");
				if (items.isMissingNode())
					return java.util.Collections.emptyList();
				return items.isArray() ? (Iterable<JsonNode>) items::elements
						: java.util.Collections.singletonList(items);
			}).doOnNext(item -> {
				try {
					// [기존 테이블 매핑 전략]
					// 1. fire_id: 날짜(20260128) + 지역코드 등을 조합하여 숫자형태 생성
					String dateStr = item.path("analdate").asText().replaceAll("[^0-9]", "").substring(0, 10);
					long virtualId = Long.parseLong(dateStr);

					DisasterAccidentDTO dto = DisasterAccidentDTO.builder().fireId(virtualId) // PK 충족
							.fireDamageArea(item.path("meanavg").asDouble()) // 평균지수를 면적 컬럼에 임시 저장
							.fireLocVillage(item.path("doname").asText()) // 발생장소_시도
							.fireStartTime(item.path("analdate").asText() + ":00:00") // 발생일시 (DATETIME 형식 맞춤)
							.fireEndTime(item.path("analdate").asText() + ":00:00") // 종료일시 (필수값 충족)
							.fireCause("산불위험예보") // 발생원인에 구분값 기록
							.build();

					mapper.insertForestFire(dto); // 기존 매퍼 메서드 재사용
				} catch (Exception e) {
					log.error("기존 테이블 매핑 저장 에러: {}", e.getMessage());
				}
			}).then();
		});
	}

	@Override
	public List<DisasterAccidentDTO> getForestFireList() {
		return mapper.selectForestFireList();
	}

	// =================================================
	// 산불 예측 지수
	// =================================================
	// 📌 관리자용: API 긁어와서 DB에 저장하기
	@Override
	public Mono<Void> fetchAndSaveForestFireRiskData() { 
	    return Mono.defer(() -> {
	        java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
	                .fromHttpUrl("http://apis.data.go.kr/1400377/forestPoint/forestPointListSidoSearch")
	                .queryParam("ServiceKey", serviceKey)
	                .queryParam("pageNo", "1")
	                .queryParam("numOfRows", "20") // 전국 데이터를 일단 가져옴
	                .queryParam("_type", "json")
	                .queryParam("excludeForecast", "0")
	                .build(true).toUri();

	        return publicDataWebClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class).flatMapIterable(node -> {
	            JsonNode items = node.path("response").path("body").path("items").path("item");
	            if (items.isMissingNode()) return java.util.Collections.emptyList();
	            return items.isArray() ? (Iterable<JsonNode>) items::elements : java.util.Collections.singletonList(items);
	        })
	        // ⭐ 여기서 '전북' 또는 '전라북도'가 포함된 데이터만 거름
	        .filter(item -> {
	            String doName = item.path("doname").asText("");
	            return doName.contains("전북") || doName.contains("전라북도");
	        })
	        .doOnNext(item -> {
	            try {
	                DisasterAccidentDTO dto = DisasterAccidentDTO.builder()
	                        .analDate(item.path("analdate").asText())
	                        .doName(item.path("doname").asText())
	                        .avgIndex(item.path("meanavg").asDouble())
	                        .maxIndex(item.path("maxi").asDouble())
	                        .minIndex(item.path("mini").asDouble())
	                        .build();

	                mapper.insertForestFireRisk(dto); 
	                log.info("✅ 전북 산불 예보 저장 완료: {}", dto.getAnalDate());
	            } catch (Exception e) {
	                log.error("산불 예보 DB 저장 에러: {}", e.getMessage());
	            }
	        }).then();
	    });
	}
	// 📌 화면용: DB에서 가져오기 (API 사용 중지 시에도 DB 값 활용 가능)
	@Override
	public List<DisasterAccidentDTO> getForestFireRiskList() {
	    return mapper.selectForestFireRiskList();
	}

	// =================================================
	// 지진
	// =================================================
	@Override
	public Mono<Void> fetchAndSaveEarthquake(String startDate) {
	    return apihubDataWebClient.get().uri(uriBuilder -> {
	    	uriBuilder.path("/typ01/url/eqk_list.php");
	        // [수정] startDate가 없으면 자동으로 '최근 1년' 데이터 수집하도록 변경
	        String tm1;
//	        String tm2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
	        String tm2 = LocalDateTime.now()
	                .minusMinutes(2) // 2분 정도 여유를 줌
	                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

	        if (startDate != null && !startDate.isEmpty()) {
	            tm1 = startDate; // 사용자가 입력한 날짜 (형식: YYYYMMDDHHmm 지켜야 함)
	        } else {
	            // 입력 없으면 1년 전 날짜를 기본값으로 세팅
	            tm1 = LocalDateTime.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
	        }

	        uriBuilder.queryParam("tm1", tm1);
	        uriBuilder.queryParam("tm2", tm2);
	        uriBuilder.queryParam("authKey", apiHubKey);
	        uriBuilder.queryParam("help", "1");
	        
	        log.info("지진 데이터 요청 기간: {} ~ {}", tm1, tm2); // 로그로 확인

	        return uriBuilder.build();
	    })
	    .retrieve()
	    .bodyToMono(byte[].class)
	    .map(bytes -> new String(bytes, Charset.forName("EUC-KR")))
	    .flatMap(data -> {
	        if (data == null || data.trim().isEmpty()) return Mono.empty();
	        
	        log.info("지진 데이터 수신 성공! 파싱 시작...");
	        String[] lines = data.split("\n");

	        for (String line : lines) {
	            String trimmed = line.trim();
	            // 주석이나 빈 줄, 헤더 제외
	            if (trimmed.startsWith("#") || trimmed.isEmpty() || trimmed.contains("TP")) continue;

	            try {
	                // 공백으로 데이터 분리
	                String[] parts = trimmed.split("\\s+");
	                
	                // 1. TP 확인 (3: 국내지진만 저장)
	                if (!"3".equals(parts[0])) continue;

	                // 2. 파트별 매핑 (보내준 가이드 기준)
	                // parts[1]: TM_FC, parts[2]: SEQ, parts[4]: MT, parts[5]: LAT, parts[6]: LON
	                
	                // 3. LOC(위치) 및 REM(참고) 추출
	                // LOC는 7번 인덱스부터 시작해서 "=" 전까지임
	                StringBuilder locBuilder = new StringBuilder();
	                for (int i = 7; i < parts.length; i++) {
	                    if (parts[i].contains("=")) break;
	                    locBuilder.append(parts[i]).append(" ");
	                }
	                String fullLocInfo = locBuilder.toString().trim(); // 예: "제주 제주시 서쪽...해역,최대진도 Ⅰ,지진피해..."
	                String[] locDetails = fullLocInfo.split(",");

	                DisasterAccidentDTO dto = DisasterAccidentDTO.builder()
	                        .tmFc(parts[1])
	                        .seq(Long.parseLong(parts[2]))
	                        .mt(Double.parseDouble(parts[4]))
	                        .lat(Double.parseDouble(parts[5]))
	                        .lon(Double.parseDouble(parts[6]))
	                        .loc(locDetails[0]) // 위치 (첫 번째 쉼표 전까지)
	                        .rem(locDetails.length > 2 ? locDetails[2] : "") // 참고사항
	                        .build();

	                mapper.insertEarthquake(dto);
	                log.info("지진 저장 완료: {} - {}", dto.getTmFc(), dto.getLoc());

	            } catch (Exception e) {
	                log.error("지진 파싱 에러 발생 라인: {} | 에러: {}", trimmed, e.getMessage());
	            }
	        }
	        return Mono.empty();
	    }).then();
	}
	
	@Override
	public List<DisasterAccidentDTO> getEarthquakeList() {
	    return mapper.selectEarthquakeList();
	}
	

	// =================================================
	// 태풍 특보
	// =================================================
	@Override
	@Transactional
	public Mono<Void> fetchAndSaveTyphoon(String year) {
		log.info("{}년 태풍 데이터 수집 시작...", year);

		return apihubDataWebClient.get() // [수정] 기상청 전용 클라이언트 사용
				.uri(uriBuilder -> uriBuilder.path("/typ01/url/typ_lst.php").queryParam("YY", year)
						.queryParam("authKey", apiHubKey).build())
				.retrieve().bodyToMono(String.class).flatMap(data -> {
					String[] lines = data.split("\n");
					return Flux.fromArray(lines)
							.filter(line -> !line.startsWith("#") && !line.trim().isEmpty() && !line.contains("YY"))
							.concatMap(line -> {
								String[] cols = line.trim().split("\\s+");
								try {
									DisasterAccidentDTO info = DisasterAccidentDTO.builder()
											.typhoonYear(Integer.parseInt(cols[0])).typhoonNo(Integer.parseInt(cols[1]))
											.typhoonActiveYn(cols[2].equals("1") ? "Y" : "N").typhoonName(cols[6])
											.typhoonNameDesc(cols.length > 8 ? cols[8] : "").build();
									mapper.insertTyphoonInfo(info);
									return fetchAndSaveTyphoonTrack(cols[0], cols[1]);
								} catch (Exception e) {
									log.error("태풍 목록 파싱 에러: {}", line);
									return Mono.empty();
								}
							}).then();
				}).onErrorResume(e -> Mono.empty());
	}

	// =================================================
	// 태풍 경로
	// =================================================
	private Mono<Void> fetchAndSaveTyphoonTrack(String yy, String typ) {
		return apihubDataWebClient.get() // [수정] 기상청 전용 클라이언트 사용
				.uri(uriBuilder -> uriBuilder.path("/typ01/url/typ_data.php").queryParam("YY", yy)
						.queryParam("mode", "1").queryParam("authKey", apiHubKey).build())
				.retrieve().bodyToMono(String.class).timeout(java.time.Duration.ofSeconds(30)).doOnNext(data -> {
					String[] lines = data.split("\n");
					for (String line : lines) {
						if (line.startsWith("#") || line.trim().isEmpty() || line.contains("FT"))
							continue;
						String[] cols = line.trim().split("\\s+");
						if (cols[0].equals("0")) {
							try {
								DisasterAccidentDTO track = DisasterAccidentDTO.builder()
										.typhoonYear(Integer.parseInt(cols[1])).typhoonNo(Integer.parseInt(cols[2]))
										.typhoonReportNo(Integer.parseInt(cols[3])).typhoonAnalysisDatetime(cols[5])
										.typhoonLat(Double.parseDouble(cols[7])).typhoonLon(Double.parseDouble(cols[8]))
										.typhoonMoveSpeed(Double.parseDouble(cols[10]))
										.typhoonCentralPressure(Integer.parseInt(cols[11]))
										.typhoonMaxWindSpeed(Double.parseDouble(cols[12]))
										.typhoonRadius15ms(Integer.parseInt(cols[13]))
										.typhoonLocation(cols[cols.length - 1]).build();
								mapper.insertTyphoonTrack(track);
							} catch (Exception e) {
								log.error("태풍 경로 저장 에러");
							}
						}
					}
				}).onErrorResume(e -> Mono.empty()).then();
	}

	// =================================================
	// 산사태
	// =================================================
	@Override
	public Mono<Void> fetchAndSaveLandslide() {
		return Mono.defer(() -> {
			java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
					.fromHttpUrl("https://apis.data.go.kr/1400000/predictionInfoService/predictionInfoList")
					.queryParam("serviceKey", serviceKey).queryParam("_type", "json").build(true).toUri();

			return publicDataWebClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class).flatMapIterable(node -> {
				JsonNode items = node.path("response").path("body").path("items").path("item");
				if (items.isMissingNode())
					return java.util.Collections.emptyList();
				return items.isArray() ? (Iterable<JsonNode>) items::elements
						: java.util.Collections.singletonList(items);
			}).doOnNext(item -> {
				// [로그 분석 기반 수정] 실제 API 필드명으로 교체
				String frcstNm = item.path("lndslFrcstNm").asText(); // 주의보/경보
				String sggNm = item.path("sgg").asText(); // 지역명
				String rawDate = item.path("prctnInfoAnlssDt").asText(); // 분석일시

				if (rawDate == null || rawDate.isEmpty()) {
					log.warn("산사태 데이터 날짜 누락: {}", item);
					return;
				}

				try {
					DisasterAccidentDTO dto = DisasterAccidentDTO.builder().lnldFrcstNm(frcstNm).sggNm(sggNm)
							.predcAnlsDt(rawDate) // 이미 "2025-10-25 13:00:00" 형식이므로 바로 저장 가능
							.build();

					mapper.insertLandslide(dto);
					log.info("산사태 데이터 저장 완료: {} {}", sggNm, frcstNm);
				} catch (Exception e) {
					log.error("산사태 DB 저장 에러: {}", e.getMessage());
				}
			}).then();
		});
	}

	// =================================================
	// 기상 특보 (ex. 호우특보)
	// =================================================
	@Override
	public Mono<Void> fetchAndSaveWeatherWarning(String warningType) {
		return Mono.defer(() -> {
			// 1. 전북 지역 특보구역 코드 리스트 (보내준 이미지 기반)
			List<String> jeonbukAreaCodes = java.util.Arrays.asList("L1060100", "L1060200", "L1060300", "L1060400",
					"L1060500", "L1060600", "L1060700", "L1060800", "L1060900", "L1061000", "L1061100", "L1061200",
					"L1061300", "L1061400");

			java.time.LocalDate now = java.time.LocalDate.now();
			String toTmFc = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
			String fromTmFc = now.minusDays(5).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

			java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
					.fromHttpUrl("https://apis.data.go.kr/1360000/WthrWrnInfoService/getPwnCd")
					.queryParam("serviceKey", serviceKey).queryParam("pageNo", "1").queryParam("numOfRows", "100")
					.queryParam("dataType", "JSON").queryParam("fromTmFc", fromTmFc).queryParam("toTmFc", toTmFc)
					.queryParam("warningType", warningType).build(true).toUri();

			log.info("🚀 [전북 필터링] 기간: {} ~ {}, 대상 코드: L1060100 ~ L1061400", fromTmFc, toTmFc);

			return publicDataWebClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class).flatMapMany(node -> {
				JsonNode items = node.path("response").path("body").path("items").path("item");
				if (items.isMissingNode() || items.isNull())
					return Flux.empty();
				return items.isArray() ? Flux.fromIterable(items) : Flux.just(items);
			})
					// ⭐ 핵심 수정: areaCode가 전북 코드 리스트에 포함되는지 확인
					.filter(item -> {
						String code = item.path("areaCode").asText("");
						return jeonbukAreaCodes.contains(code);
					}).publishOn(reactor.core.scheduler.Schedulers.boundedElastic()).flatMap(item -> {
						try {
							DisasterAccidentDTO dto = DisasterAccidentDTO.builder().tmFc(item.path("tmFc").asText())
									.tmSeq(item.path("tmSeq").asInt()).areaCode(item.path("areaCode").asText())
									.warnVar(item.path("warnVar").asInt()).stnId(item.path("stnId").asText())
									.areaName(item.path("areaName").asText())
									.warnStress(item.path("warnStress").asInt())
									.startTime(item.path("startTime").asText()).endTime(item.path("endTime").asText())
									.type(Integer.parseInt(warningType)).build();

							mapper.insertKmaWeather(dto);
							log.info("✅ 전북 지역 저장 완료: {} ({})", dto.getAreaName(), dto.getAreaCode());
							return Mono.just(dto);
						} catch (Exception e) {
							log.error("❌ 저장 실패: {}", e.getMessage());
							return Mono.empty();
						}
					}).then();
		});
	}

	@Override
	public List<DisasterAccidentDTO> getWeatherListByType(int type) {
		// 매퍼를 호출해서 DB에서 리스트를 가져와
		return mapper.selectKmaWeatherByType(type);
	}
}
