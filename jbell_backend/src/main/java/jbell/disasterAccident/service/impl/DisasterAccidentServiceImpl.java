package jbell.disasterAccident.service.impl;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import jbell.disasterAccident.dto.DisasterAccidentDTO;
import jbell.disasterAccident.mapper.DisasterAccidentMapper;
import jbell.disasterAccident.service.DisasterAccident;
import jbell.exception.CustomException;
import jbell.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DisasterAccidentServiceImpl implements DisasterAccident {

	private final WebClient publicDataWebClient;
	private final WebClient apihubDataWebClient;
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
	// ⏰ 통합 스케줄러 (5분 주기)
	// =================================================
	@Scheduled(cron = "0 0/5 * * * ?") // 5분마다 실행
	public void autoSyncAllDisasterData() {
		log.info("⏰ [Scheduler] 5분 주기 재난 데이터 통합 동기화 시작: {}", LocalDateTime.now());

		// 1. 산불 위험 예보 수집
		fetchAndSaveForestFireRiskData().subscribe(
			null, e -> log.error("❌ 산불 예보 스케줄링 실패: {}", e.getMessage()),
			() -> log.info("✅ 산불 예보 동기화 완료")
		);

		// 2. 산사태 예보 수집
		fetchAndSaveLandslide().subscribe(
			null, e -> log.error("❌ 산사태 스케줄링 실패: {}", e.getMessage()),
			() -> log.info("✅ 산사태 동기화 완료")
		);

		// 3. 지진 데이터 수집 (최근 1일치 자동 설정)
		fetchAndSaveEarthquake(null).subscribe(
			null, e -> log.error("❌ 지진 스케줄링 실패: {}", e.getMessage()),
			() -> log.info("✅ 지진 동기화 완료")
		);

		// 4. 기상 특보 (호우특보 등 주요 1, 2번 타입)
		fetchAndSaveWeatherWarning("1").subscribe(); // 강풍 등
		fetchAndSaveWeatherWarning("2").subscribe(  // 호우
			null, e -> log.error("❌ 기상 특보 스케줄링 실패: {}", e.getMessage()),
			() -> log.info("✅ 기상 특보 동기화 완료")
		);
		
		// 5. 태풍 (현재 연도)
		String currentYear = String.valueOf(LocalDateTime.now().getYear());
		fetchAndSaveTyphoon(currentYear).subscribe(
			null, e -> log.error("❌ 태풍 스케줄링 실패: {}", e.getMessage()),
			() -> log.info("✅ 태풍 동기화 완료")
		);
		
		// autoSyncAllDisasterData 메서드 내부에 추가
		// 예: 마령교(4001605) 수위 데이터 동기화
		fetchAndSaveWaterLevel("4001605").subscribe(
		    null, 
		    e -> log.error("❌ 댐 수위 스케줄링 실패: {}", e.getMessage()),
		    () -> log.info("✅ 댐 수위 동기화 완료")
		);
	}

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
					String dateStr = item.path("analdate").asText().replaceAll("[^0-9]", "").substring(0, 10);
					long virtualId = Long.parseLong(dateStr);
					DisasterAccidentDTO dto = DisasterAccidentDTO.builder().fireId(virtualId)
							.fireDamageArea(item.path("meanavg").asDouble())
							.fireLocVillage(item.path("doname").asText())
							.fireStartTime(item.path("analdate").asText() + ":00:00")
							.fireEndTime(item.path("analdate").asText() + ":00:00")
							.fireCause("산불위험예보").build();
					mapper.insertForestFire(dto);
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
	@Override
	public Mono<Void> fetchAndSaveForestFireRiskData() { 
	    return Mono.defer(() -> {
	        java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
	                .fromHttpUrl("http://apis.data.go.kr/1400377/forestPoint/forestPointListSidoSearch")
	                .queryParam("ServiceKey", serviceKey).queryParam("pageNo", "1").queryParam("numOfRows", "20")
	                .queryParam("_type", "json").queryParam("excludeForecast", "0").build(true).toUri();

	        return publicDataWebClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class).flatMapIterable(node -> {
	            JsonNode items = node.path("response").path("body").path("items").path("item");
	            if (items.isMissingNode()) return java.util.Collections.emptyList();
	            return items.isArray() ? (Iterable<JsonNode>) items::elements : java.util.Collections.singletonList(items);
	        })
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
	                        .minIndex(item.path("mini").asDouble()).build();
	                mapper.insertForestFireRisk(dto); 
	            } catch (Exception e) {
	                log.error("산불 예보 DB 저장 에러: {}", e.getMessage());
	            }
	        }).then();
	    });
	}

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
	        String tm2 = LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
	        String tm1 = (startDate != null && !startDate.isEmpty()) ? startDate 
	                     : LocalDateTime.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

	        uriBuilder.queryParam("tm1", tm1).queryParam("tm2", tm2)
	                  .queryParam("authKey", apiHubKey).queryParam("help", "1");
	        return uriBuilder.build();
	    })
	    .retrieve().bodyToMono(byte[].class)
	    .map(bytes -> new String(bytes, Charset.forName("EUC-KR")))
	    .flatMap(data -> {
	        if (data == null || data.trim().isEmpty()) return Mono.empty();
	        String[] lines = data.split("\n");
	        for (String line : lines) {
	            String trimmed = line.trim();
	            if (trimmed.startsWith("#") || trimmed.isEmpty() || trimmed.contains("TP")) continue;
	            try {
	                String[] parts = trimmed.split("\\s+");
	                if (!"3".equals(parts[0])) continue;
	                StringBuilder locBuilder = new StringBuilder();
	                for (int i = 7; i < parts.length; i++) {
	                    if (parts[i].contains("=")) break;
	                    locBuilder.append(parts[i]).append(" ");
	                }
	                String fullLocInfo = locBuilder.toString().trim();
	                String[] locDetails = fullLocInfo.split(",");
	                DisasterAccidentDTO dto = DisasterAccidentDTO.builder()
	                        .tmFc(parts[1]).seq(Long.parseLong(parts[2])).mt(Double.parseDouble(parts[4]))
	                        .lat(Double.parseDouble(parts[5])).lon(Double.parseDouble(parts[6]))
	                        .loc(locDetails[0]).rem(locDetails.length > 2 ? locDetails[2] : "").build();
	                mapper.insertEarthquake(dto);
	            } catch (Exception e) {
	                log.error("지진 파싱 에러: {}", e.getMessage());
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
	// 태풍 및 경로
	// =================================================
	@Override
	@Transactional
	public Mono<Void> fetchAndSaveTyphoon(String year) {
		return apihubDataWebClient.get()
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
									return Mono.empty();
								}
							}).then();
				}).onErrorResume(e -> Mono.empty());
	}
	
	@Override
	public List<DisasterAccidentDTO> getTyphoonList() {
	    return mapper.selectTyphoonList();
	}
	
	
	

	private Mono<Void> fetchAndSaveTyphoonTrack(String yy, String typ) {
		return apihubDataWebClient.get()
				.uri(uriBuilder -> uriBuilder.path("/typ01/url/typ_data.php").queryParam("YY", yy)
						.queryParam("mode", "1").queryParam("authKey", apiHubKey).build())
				.retrieve().bodyToMono(String.class).timeout(java.time.Duration.ofSeconds(30)).doOnNext(data -> {
					String[] lines = data.split("\n");
					for (String line : lines) {
						if (line.startsWith("#") || line.trim().isEmpty() || line.contains("FT")) continue;
						String[] cols = line.trim().split("\\s+");
						if (cols[0].equals("0")) {
							try {
								DisasterAccidentDTO track = DisasterAccidentDTO.builder()
										.typhoonYear(Integer.parseInt(cols[1])).typhoonNo(Integer.parseInt(cols[2]))
										.typhoonReportNo(Integer.parseInt(cols[3])).typhoonAnalysisDatetime(cols[5])
										.typhoonLat(Double.parseDouble(cols[7])).typhoonLon(Double.parseDouble(cols[8]))
										.typhoonMoveSpeed(Double.parseDouble(cols[10])).typhoonCentralPressure(Integer.parseInt(cols[11]))
										.typhoonMaxWindSpeed(Double.parseDouble(cols[12])).typhoonRadius15ms(Integer.parseInt(cols[13]))
										.typhoonLocation(cols[cols.length - 1]).build();
								mapper.insertTyphoonTrack(track);
							} catch (Exception e) { log.error("태풍 경로 에러"); }
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
	                .fromHttpUrl("http://apis.data.go.kr/1400000/forecastIssueService/forecastIssueList")
	                .queryParam("serviceKey", serviceKey).queryParam("numOfRows", "50").queryParam("_type", "json").build(true).toUri();

	        return publicDataWebClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class).flatMapIterable(node -> {
	            JsonNode items = node.path("response").path("body").path("items").path("item");
	            if (items.isMissingNode()) return java.util.Collections.emptyList();
	            return items.isArray() ? (Iterable<JsonNode>) items::elements : java.util.Collections.singletonList(items);
	        })
	        .filter(item -> {
	            String insttNm = item.path("ocrnFrcstIssuInsttNm").asText("");
	            return insttNm.contains("전북") || insttNm.contains("전라북도");
	        })
	        .doOnNext(item -> {
	            try {
	                DisasterAccidentDTO dto = DisasterAccidentDTO.builder()
	                        .lnldFrcstNm(item.path("frcstIssuKindNm").asText())
	                        .sggNm(item.path("ocrnFrcstIssuInsttNm").asText())
	                        .predcAnlsDt(item.path("frstFrcstIssuDt").asText()).build();
	                if (dto.getSggNm() != null && !dto.getSggNm().isEmpty()) mapper.insertLandslide(dto);
	            } catch (Exception e) { log.error("산사태 매핑 에러"); }
	        }).then();
	    });
	}

	@Override
	public List<DisasterAccidentDTO> getLandslideList() {
	    return mapper.selectLandslideList();
	}

	// =================================================
	// 기상 특보
	// =================================================
	@Override
	public Mono<Void> fetchAndSaveWeatherWarning(String warningType) {
		return Mono.defer(() -> {
			List<String> jeonbukAreaCodes = java.util.Arrays.asList("L1060100", "L1060200", "L1060300", "L1060400",
					"L1060500", "L1060600", "L1060700", "L1060800", "L1060900", "L1061000", "L1061100", "L1061200",
					"L1061300", "L1061400");
			String toTmFc = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
			String fromTmFc = java.time.LocalDate.now().minusDays(5).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

			java.net.URI uri = org.springframework.web.util.UriComponentsBuilder
					.fromHttpUrl("https://apis.data.go.kr/1360000/WthrWrnInfoService/getPwnCd")
					.queryParam("serviceKey", serviceKey).queryParam("pageNo", "1").queryParam("numOfRows", "100")
					.queryParam("dataType", "JSON").queryParam("fromTmFc", fromTmFc).queryParam("toTmFc", toTmFc)
					.queryParam("warningType", warningType).build(true).toUri();

			return publicDataWebClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class).flatMapMany(node -> {
				JsonNode items = node.path("response").path("body").path("items").path("item");
				if (items.isMissingNode() || items.isNull()) return Flux.empty();
				return items.isArray() ? Flux.fromIterable(items) : Flux.just(items);
			})
			.filter(item -> jeonbukAreaCodes.contains(item.path("areaCode").asText("")))
			.publishOn(reactor.core.scheduler.Schedulers.boundedElastic()).flatMap(item -> {
				try {
					DisasterAccidentDTO dto = DisasterAccidentDTO.builder().tmFc(item.path("tmFc").asText())
							.tmSeq(item.path("tmSeq").asInt()).areaCode(item.path("areaCode").asText())
							.warnVar(item.path("warnVar").asInt()).stnId(item.path("stnId").asText())
							.areaName(item.path("areaName").asText()).warnStress(item.path("warnStress").asInt())
							.startTime(item.path("startTime").asText()).endTime(item.path("endTime").asText())
							.type(Integer.parseInt(warningType)).build();
					mapper.insertKmaWeather(dto);
					return Mono.just(dto);
				} catch (Exception e) { return Mono.empty(); }
			}).then();
		});
	}

	@Override
	public List<DisasterAccidentDTO> getWeatherListByType(int type) {
		return mapper.selectKmaWeatherByType(type);
	}
	
	
	
	// =================================================
    // 댐 & 하천 수위 수집
    // =================================================
	@Override
	public Mono<Void> fetchAndSaveWaterLevel(String obscd) { // 여기서 obscd는 사실 안 써도 됨 (전체 basin=4를 가져오니까)
	    return Mono.defer(() -> {
	        return publicDataWebClient.get()
	            .uri(uriBuilder -> uriBuilder
	                .scheme("http")
	                .host("www.wamis.go.kr")
	                .port(8080)
	                .path("/wamis/openapi/wkw/wl_dubwlobs") // 네가 말한 그 주소!
	                .queryParam("basin", "4")              // 전북 지역 고정
	                .build())
	            .retrieve()
	            .bodyToMono(JsonNode.class)
	            .flatMapIterable(node -> {
	                // WAMIS API는 보통 'list'라는 키 안에 배열이 들어있어
	                JsonNode list = node.path("list");
	                return list.isArray() ? (Iterable<JsonNode>) list::elements : java.util.Collections.emptyList();
	            })
	            .doOnNext(item -> {
	                try {
	                    // API 응답 필드명을 소문자로 정확히 매칭하자!
	                    DisasterAccidentDTO dto = DisasterAccidentDTO.builder()
	                        .obsCd(item.path("obscd").asText())    // 관측소 코드
	                        .obsNm(item.path("obsnm").asText())    // 관측소 이름
	                        .bbsnNm(item.path("bbsnnm").asText())  // 하천명
	                        .mngOrg(item.path("mngorg").asText())  // 관리기관
	                        // 만약 이 API에서 수위와 시간을 준다면 아래 필드를 쓸 거야
	                        // (필드명이 다를 수 있으니 로그로 꼭 확인해봐!)
	                        .obsTime(item.path("ymdhm").asText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))))
	                        .waterLevel(item.path("wl").asDouble(0.0)) 
	                        .build();

	                    log.info("📥 [전북 수위] 파싱 중: {} ({})", dto.getObsNm(), dto.getObsCd());

	                    // 데이터가 유효하면 DB에 저장 (Duplicate Key Update 로직 작동)
	                    if (dto.getObsCd() != null && !dto.getObsCd().isEmpty()) {
	                        mapper.insertWaterLevel(dto);
	                    }
	                } catch (Exception e) {
	                    log.error("❌ 데이터 파싱/저장 중 에러: {}", e.getMessage());
	                }
	            }).then();
	    });
	}
	@Override
	public List<DisasterAccidentDTO> getWaterLevelList() {
	    return mapper.selectWaterLevelList();
	}
    
	// 재난 발생 관리 상태 변경
	@Override
    @Transactional
    public Mono<Void> updateDisasterStatus(DisasterAccidentDTO disasterAccidentDTO) {
        String status = disasterAccidentDTO.isVisible() ? "Y" : "N";
        
        return Mono.fromRunnable(() -> {
            for (String compositeId : disasterAccidentDTO.getIds()) {
                try {
                    String[] parts = compositeId.split("_");
                    String type = parts[0]; // FIRE, EQK, WTH

                    switch (type) {
                        case "FIRE": // FIRE_{fireId}_{idx}
                            long fireId = Long.parseLong(parts[1]);
                            mapper.updateForestFireStatus(fireId, status);
                            break;
                        case "EQK": // EQK_{seq}_{idx}
                            long seq = Long.parseLong(parts[1]);
                            mapper.updateEarthquakeStatus(seq, status);
                            break;
                        case "WTH": // WTH_{type}_{tmSeq}_{stnId}_{idx}
                            // parts[1]: type, parts[2]: tmSeq, parts[3]: stnId
                            int tmSeq = Integer.parseInt(parts[2]);
                            String stnId = parts[3];
                            mapper.updateKmaWeatherStatus(tmSeq, stnId, status);
                            break;
                        default:
                            log.warn("Unknown Disaster Type ID: {}", compositeId);
                    }
                } catch (Exception e) {
                    log.error("Failed to update status for ID: {}", compositeId, e);
                    // 하나 실패해도 나머지는 진행하거나, 여기서 CustomException 던져서 롤백 가능
                    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); 
                }
            }
        });
    }
}