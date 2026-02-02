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
import jbell.common.service.FileService;
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
    
	// 필드 선언부
    private final ObjectMapper objectMapper;
    private final BehaviorMethodMapper behaviorMethodMapper;
    private final WebClient safetyDataWebClient;
    private final FileService fileService;

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

    // 생성자 수정
    public BehaviorMethodService(@Qualifier("safetyDataWebClient") WebClient safetyDataWebClient,
                                 @Qualifier("objectMapper") ObjectMapper objectMapper,
                                 BehaviorMethodMapper behaviorMethodMapper,
                                 FileService fileService) {
        this.safetyDataWebClient = safetyDataWebClient;
        this.objectMapper = objectMapper;
        this.behaviorMethodMapper = behaviorMethodMapper;
        this.fileService = fileService;
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
        // 1. 본문 데이터 수정
        int result = behaviorMethodMapper.updateBehaviorMethod(updateData);
        
        // 2. ★ 파일 연결 (contentId와 파일 ID들을 연결)
        if (updateData.getFileIds() != null && !updateData.getFileIds().isEmpty()) {
            fileService.linkFilesToContent(updateData.getContentId(), updateData.getFileIds());
        }
        
        if (result == 0) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
    }
    
    /**
     * 행동요령 등록
     */
    @Transactional
    public Long registerBehaviorMethod(BehaviorMethodContentVO vo) {
        // 1. 프론트에서 넘어온 숫자 코드(예: 01011)를 실제 DB PK인 code_item_id로 변환
        // vo.getContentType() 에 "01011"이 들어있음
        String realCodeItemId = behaviorMethodMapper.findCodeItemIdByDescription(vo.getContentType());
        
        if (realCodeItemId == null || realCodeItemId.isEmpty()) {
            // 만약 매칭되는 코드가 없다면 예외 처리 (혹은 기본값 설정)
            log.error("부적절한 재난 유형 코드: {}", vo.getContentType());
            throw new CustomException(ErrorCode.BAD_REQUEST); 
        }
        
        // 2. 변환된 ID를 다시 세팅 (이제 "01011" 대신 "EARTHQUAKE"가 들어감)
        vo.setContentType(realCodeItemId);
        
        // 3. 행동요령 본문 내용 등록
        behaviorMethodMapper.insertManualBehaviorMethod(vo);
        
        return vo.getContentId();
    }
    
    /**
     * 행동요령 삭제 (단건 및 다건 일괄 삭제)
     * @param ids 삭제할 콘텐츠 ID 리스트
     */
    @Transactional
    public void deleteBehaviorMethods(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        // 1. 행동요령 본문 데이터 삭제
        int result = behaviorMethodMapper.deleteBehaviorMethods(ids);
        
        // 2. (선택사항) 해당 콘텐츠에 연결된 파일 매핑 정보도 삭제해야 한다면 추가
        // fileService.deleteLinksByContentIds(ids);
        
        if (result == 0) {
            log.warn("삭제 요청된 ID들 중 존재하지 않는 데이터가 있습니다: {}", ids);
        } else {
            log.info("행동요령 {}건 삭제 완료", result);
        }
    }
    
    /**
     * 행동요령 노출 상태 일괄 변경
     * @param ids 대상 ID 리스트
     * @param visibleYn 변경할 상태 ('Y' / 'N')
     */
    @Transactional
    public void updateVisibility(List<Long> ids, String visibleYn) {
        behaviorMethodMapper.updateVisibility(ids, visibleYn);
    }
}