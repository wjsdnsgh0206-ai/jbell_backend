package jbell.behaviorMethod.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
// @RequiredArgsConstructor를 쓰면 final 필드에 대한 생성자를 자동 생성해줍니다.
// 하지만 WebClient에 @Qualifier가 필요하므로 직접 생성자를 유지하거나, 
// 아래처럼 Lombok과 직접 생성자를 조합하지 말고 기존 방식을 유지하는 게 낫습니다.
public class BehaviorMethodService {
	
	private final ObjectMapper objectMapper;
    private final BehaviorMethodMapper behaviorMethodMapper;
    private final WebClient safetyDataWebClient;

    /*
    // 서비스키 늘어날 시 MAP 활용 (예시: 키 관리 맵 활용)
    private String getServiceKey(String apiPath) {
        return Map.of(
            API_PATH_NATURAL, naturalServiceKey,
            API_PATH_SOCIAL, socialServiceKey,
            API_PATH_LIFE, lifeServiceKey
        ).getOrDefault(apiPath, naturalServiceKey);
    }
    */
    @Value("${safetydata.behaviorMethod.natural.servicekey}")
    private String naturalServiceKey;

    @Value("${safetydata.behaviorMethod.social.servicekey}")
    private String socialServiceKey;

    @Value("${safetydata.behaviorMethod.life.servicekey}")
    private String lifeServiceKey;
    
    @Value("${external.api.timeout}")
    private int timeout;

    // 대분류별 카테고리 코드 정의 (ENUM 대신 List 사용)
    private static final List<String> NATURAL_CODES = List.of("01001", "01002", "01003", "01006", "01011", "01014");
    private static final List<String> SOCIAL_CODES = List.of("02007", "02011", "02012", "02013", "02019");
    private static final List<String> LIFE_CODES = List.of("03002", "03003", "03005", "03006", "03013", "03014");

    // 각 API 서비스 ID (공공데이터포털 상세 명세 확인 필요, 일반적으로 연번으로 부여됨)
    private static final String API_PATH_NATURAL = "/DSSP-IF-20588"; // 행정안전부_자연재난국민행동요령 ID
    private static final String API_PATH_SOCIAL = "/DSSP-IF-20589"; // 행정안전부_사회재난국민행동요령 ID
    private static final String API_PATH_LIFE = "/DSSP-IF-20590";   // 행정안전부_생활안전국민행동요령 ID

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

    // ================== Core Logic (공통 로직) ==================

    /**
     * 공통 실행 로직: 코드 리스트를 순회하며 fetchSafetyData 호출
     */
    private Mono<Map<String, String>> executeSync(List<String> codes, String apiPath, String serviceKey) { // 서비스 키까지 파라미터로 받도록 확장
        Map<String, String> resultMap = new ConcurrentHashMap<>();
        
        return Flux.fromIterable(codes)
                   .delayElements(Duration.ofMillis(500)) // API 부하 방지
                   .concatMap(code -> fetchAndSave(code, apiPath, serviceKey, resultMap))
                   .then(Mono.just(resultMap));
    }

    /**
     * 실제 API 호출 및 DB 저장 로직
     */
    private Mono<List<BehaviorMethod>> fetchAndSave(String categoryCode, String apiPath, String serviceKey, Map<String, String> resultMap) {
        return safetyDataWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(apiPath)
                        .queryParam("serviceKey", serviceKey) // 파라미터로 받은 전용 키 적용
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 300)
                        .queryParam("safety_cate", categoryCode)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeout))
                .map(json -> {
                    // 1. JSON 파싱 로직 (기존과 동일)
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
                    // 2. DB 저장 로직 (기존 유지)
                    return Mono.fromCallable(() -> {
                        if (behaviorMethodList.isEmpty()) return behaviorMethodList;

                        log.info("DB 저장 시작: {}건", behaviorMethodList.size());
                        
                        // API 코드(01001 등)로 실제 DB 저장용 ContentType 조회 (매퍼 로직 활용)
                        String apiCateCode = behaviorMethodList.get(0).getSafetyCate2();
                        String realContentType = behaviorMethodMapper.findCodeItemIdByDescription(apiCateCode);
                        
                        if (realContentType == null) realContentType = "BEHAVIOR_METHOD";
                        
                        // 해당 타입 기존 데이터 숨김 처리
                        behaviorMethodMapper.updateBehaviorMethodContents(realContentType);
                        // 신규 데이터 저장
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
                    return Mono.empty(); // 에러 발생 시에도 전체 흐름 중단 없이 다음 카테고리 진행
                })
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(5)));
    }
	
	// 행동요령 조회 서비스 로직
	public List<BehaviorMethodContentVO> getBehaviorList(String contentType) {
        // 필요하다면 여기서 데이터 가공 로직 추가 (예: body의 줄바꿈 처리 등)
        return behaviorMethodMapper.selectBehaviorMethodList(contentType);
    }
}
