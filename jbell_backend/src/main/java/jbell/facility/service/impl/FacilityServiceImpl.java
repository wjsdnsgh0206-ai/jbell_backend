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

import com.fasterxml.jackson.databind.ObjectMapper;

import jbell.common.dto.SafetyDataResponse;
import jbell.facility.dto.FacilityDTO;
import jbell.facility.dto.FacilityListRequest;
import jbell.facility.dto.FacilityListResponse;
import jbell.facility.eunm.ApiType;
import jbell.facility.mapper.FacilityMapper;
import jbell.facility.service.FacilityService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class FacilityServiceImpl implements FacilityService {

    private final FacilityMapper sheltersMapper;
    private final WebClient safetyDataWebClient;
    private final ObjectMapper objectMapper;

    public FacilityServiceImpl(FacilityMapper sheltersMapper, 
                               @Qualifier("safetyDataWebClient") WebClient safetyDataWebClient,
                               @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.sheltersMapper = sheltersMapper;
        this.safetyDataWebClient = safetyDataWebClient;
        this.objectMapper = objectMapper;
    }

    // 상세 조회
    @Override
    public Mono<FacilityDTO> getFacilityDetail(Long fcltId) {
        return Mono.fromCallable(() -> sheltersMapper.getFacilityById(fcltId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // 신규 등록
    @Override
    public Mono<Void> insertFacility(FacilityDTO dto) {
        return Mono.fromRunnable(() -> sheltersMapper.insertFacility(dto))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    // 정보 수정
    @Override
    public Mono<Void> updateFacility(FacilityDTO dto) {
        return Mono.fromRunnable(() -> sheltersMapper.updateFacility(dto))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    // 일괄 삭제
    @Override
    public Mono<Void> deleteFacilities(List<Long> ids) {
        return Mono.fromRunnable(() -> {
            if (ids != null && !ids.isEmpty()) {
                sheltersMapper.deleteFacilities(ids);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    // 검색 및 리스트 조회
    @Override
    public Mono<FacilityListResponse> getFacilityListData(FacilityListRequest request) {
        int limit = request.getSize();
        int offset = (request.getPage() - 1) * limit;

        return Mono.fromCallable(() -> {
            List<FacilityDTO> list = sheltersMapper.getFacilityList(
                request.getCtpvNm(), 
                request.getSggNm(), 
                request.getFcltNm(), 
                request.getRoadNmAddr(),
                request.getFcltSeCd(),
                request.getUserLat(), // Request 객체에서 꺼내서 전달
                request.getUserLot(), // Request 객체에서 꺼내서 전달
                offset, 
                limit, 
                request.getSortKey(), 
                request.getSortOrder()
            );
            
            // Count 쿼리에도 필터 조건을 동일하게 적용하는 것이 좋습니다.
            int totalCount = sheltersMapper.getFacilityCount(
                request.getCtpvNm(), 
                request.getSggNm(), 
                request.getFcltNm(), 
                request.getRoadNmAddr()
            );
            
            return FacilityListResponse.builder()
                    .items(list)
                    .totalCount(totalCount)
                    .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    
    @Value("${VITE_API_SHELTER_TEMPORARY_HOUSING_KEY}") private String imsiKey;
    @Value("${VITE_API_SHELTER_HEAT_KEY}") private String heatKey;
    @Value("${VITE_API_SHELTER_COLD_WAVE}") private String coldKey;
    @Value("${VITE_API_SHELTER_EARTHQUAKE1}") private String quakeKey;
    @Value("${VITE_API_SHELTER_CIVIL_DEFENSE_NUCLEAR}") private String nuclearKey;
    @Value("${VITE_API_SHELTER_CIVIL_DEFENSE_DISASTER}") private String civilKey;


    @Override
    public void syncAllFacility() {
        log.info("▶▶▶ 대피소 통합 동기화 프로세스 시작");
        Flux.fromIterable(Arrays.asList(ApiType.values()))
            .flatMap(this::syncOneApiReactive)
            .subscribe(
                null,
                error -> log.error("!!! 동기화 중 치명적 오류: {}", error.getMessage()),
                () -> log.info("▶▶▶ 모든 대피소 데이터 동기화 완료")
            );
    }

    private Mono<Void> syncOneApiReactive(ApiType type) {
        return recursiveFetch(type, 1);
    }

    private Mono<Void> recursiveFetch(ApiType type, int pageNo) {
        return fetchPage(type, pageNo)
            .flatMap(response -> {
                List<Map<String, Object>> items = response.getBody();
                Mono<Void> saveProcess = processAndSave(items, type, pageNo);
                
                if (items != null && items.size() >= 1000) {
                    return saveProcess.then(recursiveFetch(type, pageNo + 1));
                } else {
                    return saveProcess;
                }
            })
            .onErrorResume(e -> {
                log.error("[{}] {}페이지 호출 실패: {}", type.apiId, pageNo, e.getMessage());
                return Mono.empty();
            });
    }

    private Mono<SafetyDataResponse> fetchPage(ApiType type, int pageNo) {
        String key = getServiceKeyByType(type);
        return safetyDataWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/{apiId}")
                .queryParam("serviceKey", key)
                .queryParam("numOfRows", 1000)
                .queryParam("pageNo", pageNo)
                .queryParam("returnType", "json")
                .build(type.apiId))
            .retrieve()
            .bodyToMono(String.class)
            .map(responseString -> {
                try {
                    return objectMapper.readValue(responseString, SafetyDataResponse.class);
                } catch (Exception e) {
                    log.error("[{}] {}페이지 파싱 실패", type.apiId, pageNo);
                    return new SafetyDataResponse(); 
                }
            })
            .timeout(Duration.ofSeconds(30))
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)))
            .doOnNext(res -> log.info("[{}] {}페이지 수신 완료", type.apiId, pageNo));
    }

    private Mono<Void> processAndSave(List<Map<String, Object>> items, ApiType type, int pageNo) {
        if (items == null || items.isEmpty()) return Mono.empty();

        return Mono.fromCallable(() -> {
            // 1. DTO 변환 및 전북 지역 필터링
            List<FacilityDTO> dtoList = items.stream()
                .map(item -> {
                    // ApiType에 정의된 필드 외에 공통 필드도 확인 (방어적 코드)
                    String fullAddr = safeString(item.get(type.addrField));
                    if(fullAddr.isEmpty()) fullAddr = safeString(item.get("FCLT_ADDR_RONA"));
                    if(fullAddr.isEmpty()) fullAddr = safeString(item.get("ROAD_NM_ADDR"));
                    if(fullAddr.isEmpty()) fullAddr = safeString(item.get("DTL_ADRES"));
                    
                    return new Object[]{item, fullAddr};
                })
                .filter(obj -> {
                    String addr = (String) obj[1];
                    return addr.contains("전북") || addr.contains("전라북도");
                })
                .map(obj -> convertToDTO((Map<String, Object>) obj[0], (String) obj[1], type))
                .collect(Collectors.toList());

            // 2. DB 저장 (이 부분이 확실히 실행되어야 함)
            if (!dtoList.isEmpty()) {
                try {
                    sheltersMapper.upsertFacility(dtoList);
                    log.info("[{}] {}페이지: 전북 데이터 {}건 저장 완료", type.apiId, pageNo, dtoList.size());
                } catch (Exception e) {
                    log.error("[{}] 저장 중 오류 발생: {}", type.apiId, e.getMessage());
                }
            } else {
                log.debug("[{}] {}페이지: 전북 지역 데이터 없음", type.apiId, pageNo);
            }
            return true; // Callable의 리턴값
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    private FacilityDTO convertToDTO(Map<String, Object> item, String fullAddr, ApiType type) {
        String[] addrParts = fullAddr.split(" ");
        Double lat, lon;
        
        if ("DMS".equals(type.latField)) {
        	lat = calculateDegree(item.get("LAT_PROVIN"), item.get("LAT_MIN"), item.get("LAT_SEC"));
            lon = calculateDegree(item.get("LOT_PROVIN"), item.get("LOT_MIN"), item.get("LOT_SEC"));
        } else {
            lat = safeDouble(item.get(type.latField));
            lon = safeDouble(item.get(type.lonField));
        }

        return FacilityDTO.builder()
            .fcltNm(safeString(item.get(type.nameField)))
            .fcltSeCd(type.fcltSeCd) // ApiType의 apiId가 DB의 code_item_id(DSSP-IF-...)와 일치해야 함
            .ctpvNm(addrParts.length > 0 ? addrParts[0] : "")
            .sggNm(addrParts.length > 1 ? addrParts[1] : "")
            .roadNmAddr(fullAddr)
            .lat(lat)
            .lot(lon)
            .opnYn("N".equals(safeString(item.get("OPN_YN"))) ? "N" : "Y")
            .useYn("Y")
            // ApiType에서 지정한 필드명으로 안전하게 가져오기
            .fcltArea(safeInt(item.get(type.areaField)))
            .fcltCapacity(safeInt(item.get(type.capacityField)))
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

    private Integer safeInt(Object obj) {
        try { return obj == null ? 0 : Integer.parseInt(String.valueOf(obj)); }
        catch (Exception e) { return 0; }
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