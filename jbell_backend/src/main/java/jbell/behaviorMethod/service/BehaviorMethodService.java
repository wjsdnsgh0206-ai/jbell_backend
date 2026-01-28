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
    private final BehaviorMethodMapper behaviorMethodMapper; // mapper 생성자 주입
    private final WebClient safetyDataWebClient;

	// 재난안전데이터공유플랫폼 행정안전부_자연재난국민행동요령 서비스키
	@Value("${safetydata.behaviorMethod.natural.servicekey}")
	private String safetyDataServiceKey;
	
	// timeout 설정값은 application.properties에 있음.(ex.15000)
	@Value("${external.api.timeout}")
	private int timeout;
	
	// 생성자 주입
	public BehaviorMethodService(@Qualifier("safetyDataWebClient") WebClient safetyDataWebClient
						  ,@Qualifier("objectMapper") ObjectMapper objectMapper
						  ,BehaviorMethodMapper behaviorMethodMapper) {
		this.safetyDataWebClient = safetyDataWebClient;
		this.objectMapper = objectMapper;
		this.behaviorMethodMapper = behaviorMethodMapper;
	}
	
	public Mono<Map<String, String>> safetyDataSave(){
		Map<String, String> resultMap = new ConcurrentHashMap<>();
		
		// 현재 DB에 'BEHAVIOR_METHOD_NATURAL'만 등록되어 있으므로 ontent_type으로 저장한다고 가정.
        // 만약 카테고리별로 code_item_id가 다르다면 fetchSafetyData에 파라미터로 넘겨야 함.
        String targetContentType = "BEHAVIOR_METHOD_NATURAL";
		
		return Flux.range(1, 6)
				   .delayElements(Duration.ofMillis(500))
				   .concatMap(index -> fetchSafetyData(index-1, resultMap, targetContentType))
				   .then(Mono.just(resultMap));
	}
	
	public Mono<List<BehaviorMethod>> fetchSafetyData(int index, Map<String, String> resultMap, String targetContentType) {
		var cateList = List.of("01001", "01002", "01003", "01006", "01011", "01014"); // 태풍, 홍수, 호우, 한파, 지진, 산사태
		String category = cateList.get(index); 
		log.info("category : {}", category);
		
		// 행동요령_자연재난국민행동요령 API 요청 후 DB 동기화
        return safetyDataWebClient.get()
			            		.uri(uriBuilder -> uriBuilder
			            				.path("/DSSP-IF-20588")
			            				.queryParam("serviceKey", safetyDataServiceKey)
			            				.queryParam("pageNo", 1)
			            				.queryParam("numOfRows", 200) // 행동요령 totalCount 최대 110개까지 있는 걸 확인함. 넉넉히 200개.
			            				.queryParam("safety_cate", category)
			            				.build())
			            		.retrieve()
			            		.bodyToMono(JsonNode.class)
			            		.timeout(Duration.ofMillis(timeout))
			            		.map(json -> {

			            			// 1. 반환할 리스트를 미리 선언 (추론을 돕기 위해 빈 리스트 등으로 초기화)
			            		    List<BehaviorMethod> behaviorMethodList = List.of();
			            		    
			            		    JsonNode header = json.get("header");
			            		    if (header != null) {
			            		        String resultCode = header.has("resultCode") ? header.get("resultCode").asText() : "N/A";
			            		        if (!"00".equals(resultCode)) {
			            		            // 에러 시 예외를 던지면 이 경로는 반환 타입 추론에서 제외됨
			            		            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
			            		        }
			            		    }
			            		    
			            		    // 2. 데이터 추출 로직
			            		    if (json.has("body") && !json.get("body").isNull()) {
			            		        JsonNode body = json.get("body");
			            		        behaviorMethodList = objectMapper.convertValue(
				            					body, 
				            					new TypeReference<List<BehaviorMethod>>() {});
			            		        
			            		        for (BehaviorMethod item : behaviorMethodList) {
			            		        	// safetyCate 필드를 받을 변수 선언
			            		        	String cate2 = item.getSafetyCate2(); 
				            				String cate3 = item.getSafetyCate3(); // 소분류 (전, 중, 후)
				            				String cate4 = item.getSafetyCate4(); // 세부 분류 (정렬 순서)
				            				
			            		            String finalOrderCode;
			            		            
			            		            if ((cate3 == null || cate3.isEmpty()) && (cate4 == null || cate4.isEmpty())) {
			            		                // 영상 데이터처럼 3, 4번 카테고리가 없는 경우: cate2 + "000"
			            		                finalOrderCode = (cate2 != null) ? cate2 + "000" : "0";
			            		            } else {
			            		                // 일반 행동요령: cate4 우선, 없으면 cate3
			            		                finalOrderCode = (cate4 != null && !cate4.isEmpty()) ? cate4 : cate3;
			            		            }
			            		            
			            		            try {
			            		                // 숫자로 변환하여 세팅
			            		                item.setOrdering(Integer.parseInt(finalOrderCode));
			            		            } catch (NumberFormatException e) {
			            		                // 만약 숫자가 아닌 값이 올 경우를 대비한 기본값 설정
			            		                log.warn("Ordering 변환 실패 (값: {}), 기본값 0으로 설정", finalOrderCode);
			            		                item.setOrdering(0); 
			            		            }
			            		            
			            		            // 2. Title 및 Body 방어 로직 
			            		            // 영상 데이터는 safety_cate_nm3이 null인 경우가 많으므로 actRmks 활용
			            		            if (item.getSafetyCateNm3() == null || item.getSafetyCateNm3().isEmpty()) {
			            		                if (item.getBody() != null && !item.getBody().isEmpty()) {
			            		                    item.setSafetyCateNm3(item.getBody()); // 영상 제목을 타이틀로
			            		                } else {
			            		                    item.setSafetyCateNm3(item.getSafetyCateNm2() + " 행동요령 관련 자료");
			            		                }
			            		            }
			            		            
			            		            // Link(URL) 추가 로직
			            		            // API에서 준 contentsUrl이 있고 비어있지 않다면 DTO의 contentsUrl 필드에 저장
			            		            String contentsUrl = item.getContentsUrl();
			            		            if (contentsUrl != null && !contentsUrl.isEmpty()) {
			            		                item.setContentsUrl(contentsUrl); 
			            		            }
			            		        }
			            		    } else {
			            		        log.warn("API 응답에 body가 null이거나 존재하지 않습니다.");
			            		        // body가 없을 경우 빈 리스트 반환을 위해 위에서 초기화한 list(List.of())를 그대로 사용
			            		    }
			            			
			            		    return behaviorMethodList;
			            		})
			            		.flatMap(behaviorMethodList -> {
			            			// 비동기 흐름 안에서 동기 방식인 MyBatis를 실행
			            			return Mono.fromCallable(() -> {
			            				log.info("DB 저장 시작: {}건", behaviorMethodList.size());
			            				/* db 작업공간 */
			            				
			            				// 1. 리스트의 첫 번째 항목에서 API 카테고리 코드(예: 01001)를 추출
			            		        String apiCateCode = behaviorMethodList.get(0).getSafetyCate2();
			            		        
			            		        // 2. DB에서 해당 코드와 매핑된 code_item_id(예: NATURAL_TYPHOON) 조회
			            		        String realContentType = behaviorMethodMapper.findCodeItemIdByDescription(apiCateCode);
			            		        
			            		        // 만약 매핑된 코드가 없다면 기본값 사용
			            		        if (realContentType == null) realContentType = "BEHAVIOR_METHOD_NATURAL";
			            		        
			            		        log.info("DB 저장 시작: {}건 -> 카테고리: {}", behaviorMethodList.size(), realContentType);
			            		       
			            		        // 3. 찾은 realContentType으로 저장
			            		        behaviorMethodMapper.insertBehaviorMethodContents(behaviorMethodList, realContentType);
			            		        
			            		        return behaviorMethodList;
			            			})
			            			// 이 작업을 전용 스레드 풀로 보냄 (Event Loop 보호)
			            			.subscribeOn(Schedulers.boundedElastic());
			            		})
			            		.doOnSuccess(firstResponse -> {
			            			log.info("Successfully fetched page {}", firstResponse);
			            			log.info("카테고리 저장 완료");
			            	        resultMap.put(category, "저장완료 (" + firstResponse.size() + "건)");
			            		})
//			            		.doOnError(error -> log.error("Error fetching", error.getMessage()))
			            		.onErrorResume(CustomException.class, e -> {
								    log.error("WebClient error : Status={}",  e.getMessage());
								    resultMap.put(category, "저장실패");
			            			return Mono.empty(); // 에러나도 다음 카테고리 실행
			            		})
			            		.retryWhen(Retry.backoff(2, Duration.ofSeconds(2))  // 재시도 횟수 축소 (3->2)
			            				.maxBackoff(Duration.ofSeconds(5))
			            				.doBeforeRetry(retrySignal -> log.warn("Retrying - attempt {}", retrySignal.totalRetries() + 1)));
	}
	
	// 행동요령 조회 서비스 로직
	public List<BehaviorMethodContentVO> getBehaviorList(String contentType) {
        // 필요하다면 여기서 데이터 가공 로직 추가 (예: body의 줄바꿈 처리 등)
        return behaviorMethodMapper.selectBehaviorMethodList(contentType);
    }
}
