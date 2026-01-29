package jbell.behaviorMethod.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jbell.behaviorMethod.domain.BehaviorMethodContentVO;
import jbell.behaviorMethod.dto.BehaviorMethod;
import jbell.behaviorMethod.mapper.BehaviorMethodMapper;
import jbell.exception.CustomException;
import jbell.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class BehaviorMethodService {
    
    private final ObjectMapper objectMapper;
    private final BehaviorMethodMapper behaviorMethodMapper;
    private final WebClient safetyDataWebClient;

    @Value("${safetydata.behaviorMethod.natural.servicekey}")
    private String naturalServiceKey;

    @Value("${safetydata.behaviorMethod.social.servicekey}")
    private String socialServiceKey;

    @Value("${safetydata.behaviorMethod.life.servicekey}")
    private String lifeServiceKey;
     
    @Value("${external.api.timeout}")
    private int timeout;

    // 대분류별 카테고리 코드 정의
    private static final List<String> NATURAL_CODES = List.of("01001", "01002", "01003", "01006", "01011", "01014");
    private static final List<String> SOCIAL_CODES = List.of("02007", "02011", "02012", "02013", "02019");
    private static final List<String> LIFE_CODES = List.of("03002", "03003", "03005", "03006", "03013", "03014");

    // 각 API 서비스 ID
    private static final String API_PATH_NATURAL = "/DSSP-IF-20588";
    private static final String API_PATH_SOCIAL = "/DSSP-IF-20589";
    private static final String API_PATH_LIFE = "/DSSP-IF-20590";

    public BehaviorMethodService(@Qualifier("safetyDataWebClient") WebClient safetyDataWebClient,
                                 @Qualifier("objectMapper") ObjectMapper objectMapper,
                                 BehaviorMethodMapper behaviorMethodMapper) {
        this.safetyDataWebClient = safetyDataWebClient;
        this.objectMapper = objectMapper;
        this.behaviorMethodMapper = behaviorMethodMapper;
    }
     
    // ================== Public Methods (Controller 연결) ==================

    public Mono<Map<String, String>> syncNatural() {
        return executeSync(NATURAL_CODES, API_PATH_NATURAL, naturalServiceKey);
    }

    public Mono<Map<String, String>> syncSocial() {
        return executeSync(SOCIAL_CODES, API_PATH_SOCIAL, socialServiceKey);
    }

    public Mono<Map<String, String>> syncLife() {
        return executeSync(LIFE_CODES, API_PATH_LIFE, lifeServiceKey);
    }
    
    /**
     * 행동요령 목록 조회 (관리자/사용자 공용)
     * @param contentType 재난유형
     * @param visibleYn 노출여부 (null이면 전체)
     * @param onlyLatest 최신데이터만 보기 (Y/N) - 관리자용 필터
     */
    public List<BehaviorMethodContentVO> getBehaviorList(String contentType, String visibleYn, String onlyLatest) {
        // Mapper의 파라미터 3개와 맞춰줍니다.
        return behaviorMethodMapper.selectBehaviorMethodList(contentType, visibleYn, onlyLatest);
    }
    
    // (삭제됨: syncBehaviorMethods 메서드는 아래 fetchAndSave 로직으로 통합되었으므로 삭제합니다.)

    // ================== Core Logic (공통 로직) ==================

    private Mono<Map<String, String>> executeSync(List<String> codes, String apiPath, String serviceKey) {
        Map<String, String> resultMap = new ConcurrentHashMap<>();
        
        return Flux.fromIterable(codes)
                   .delayElements(Duration.ofMillis(500)) 
                   .concatMap(code -> fetchAndSave(code, apiPath, serviceKey, resultMap))
                   .then(Mono.just(resultMap));
    }

    private Mono<List<BehaviorMethod>> fetchAndSave(String categoryCode, String apiPath, String serviceKey, Map<String, String> resultMap) {
        return safetyDataWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(apiPath)
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 300)
                        .queryParam("safety_cate", categoryCode)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeout))
                .map(json -> {
                    // --- 1. JSON 파싱 및 데이터 가공 (기존 로직 유지) ---
                    List<BehaviorMethod> behaviorMethodList = List.of();
                    
                    JsonNode header = json.get("header");
                    if (header != null && !"00".equals(header.path("resultCode").asText(""))) {
                         throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
                    }

                    if (json.has("body") && !json.get("body").isNull()) {
                        behaviorMethodList = objectMapper.convertValue(json.get("body"), new TypeReference<List<BehaviorMethod>>() {});
                        
                        // 데이터 가공 (Ordering, Title 등)
                        for (BehaviorMethod item : behaviorMethodList) {
                            String cate2 = item.getSafetyCate2(); 
                            String cate3 = item.getSafetyCate3(); 
                            String cate4 = item.getSafetyCate4(); 
                            
                            String finalOrderCode;
                            if ((cate3 == null || cate3.isEmpty()) && (cate4 == null || cate4.isEmpty())) {
                                finalOrderCode = (cate2 != null) ? cate2 + "000" : "0";
                            } else {
                                finalOrderCode = (cate4 != null && !cate4.isEmpty()) ? cate4 : cate3;
                            }
                            
                            try {
                                item.setOrdering(Integer.parseInt(finalOrderCode));
                            } catch (NumberFormatException e) {
                                item.setOrdering(0); 
                            }
                            
                            if (item.getSafetyCateNm3() == null || item.getSafetyCateNm3().isEmpty()) {
                                if (item.getBody() != null && !item.getBody().isEmpty()) {
                                    item.setSafetyCateNm3(item.getBody());
                                } else {
                                    item.setSafetyCateNm3(item.getSafetyCateNm2() + " 행동요령 관련 자료(본문 null)");
                                }
                            }
                            
                            if (item.getContentsUrl() != null && !item.getContentsUrl().isEmpty()) {
                                item.setContentsUrl(item.getContentsUrl()); 
                            }
                        }
                    }
                    return behaviorMethodList;
                })
                .flatMap(behaviorMethodList -> {
                    // --- 2. DB 저장 로직 (★수정됨★) ---
                    return Mono.fromCallable(() -> {
                        if (behaviorMethodList.isEmpty()) return behaviorMethodList;

                        log.info("DB 저장 시작: {}건", behaviorMethodList.size());
                        
                        // 실제 DB 저장용 ContentType 코드 조회
                        String apiCateCode = behaviorMethodList.get(0).getSafetyCate2();
                        String realContentType = behaviorMethodMapper.findCodeItemIdByDescription(apiCateCode);
                        
                        if (realContentType == null) realContentType = "BEHAVIOR_METHOD";
                        
                        // 1. [동기화 전처리] 기존 API 데이터(API)의 최신 여부를 'N'으로 변경 (수동 데이터는 보존)
                        behaviorMethodMapper.updateOldSyncDataToN(realContentType);
                        
                        // 2. [신규 등록] 배치 Insert (Mapper XML에서 last_sync_yn='Y', reg_type='API'로 처리됨)
                        behaviorMethodMapper.insertBehaviorMethodContents(behaviorMethodList, realContentType);
                        
                        return behaviorMethodList;
                    })
                    .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(list -> {
                    String msg = list.isEmpty() ? "데이터 없음" : "저장완료 (" + list.size() + "건)";
                    resultMap.put(categoryCode, msg);
                    log.info("Category {} : {}", categoryCode, msg);
                })
                .onErrorResume(e -> {
                    log.error("Error processing category {}: {}", categoryCode, e.getMessage());
                    resultMap.put(categoryCode, "실패: " + e.getMessage());
                    return Mono.empty();
                })
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(5)));
    }
    
    /**
     * 과거 동기화 데이터 삭제 (API 데이터 중 last_sync_yn = 'N' 인 것들)
     */
    public void deleteOldSyncData() {
        behaviorMethodMapper.deleteOldSyncData();
    }
    
    /**
     * 행동요령 단건 상세 조회
     */
    public BehaviorMethodContentVO getBehaviorDetail(Long contentId) {
        return behaviorMethodMapper.selectBehaviorMethodDetail(contentId);
    }

    /**
     * 행동요령 데이터 수정
     */
    @Transactional
    public void updateBehaviorMethod(BehaviorMethodContentVO updateData) {
        int result = behaviorMethodMapper.updateBehaviorMethod(updateData);
        if (result == 0) {
            throw new CustomException(ErrorCode.NOT_FOUND); // 업데이트 된 행이 없으면 에러 처리
        }
    }
}