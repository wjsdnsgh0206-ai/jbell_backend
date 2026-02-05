package jbell.disaster.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;
import jbell.common.response.ApiResponse;
import jbell.disaster.dto.DisasterBatchRequest;
import jbell.disaster.dto.DisasterExternApiRequest;
import jbell.disaster.dto.PredictionInfoResponse;
import jbell.disaster.service.DisasterService;
import jbell.exception.ErrorCode;
import jbell.externapi.dto.PublicDataResponse;
import jbell.externapi.dto.SafetyDataResponse;
import jbell.externapi.service.IntegrationService;
import jbell.externapi.service.PublicDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/disaster/dashboard")
public class DisasterDashboardController {
	
	private final IntegrationService integrationService;
	private final PublicDataService publicDataService;
	private final DisasterService disasterService;
	
	// =============== 재난 문자 ===============
	// 1. 재난문자 리스트 (기존 유지)
	@GetMapping("/disasterMessages")
	public ResponseEntity<?> getDisasterMessages(PredictionInfoResponse searchParams) {
	    Map<String, Object> response = new HashMap<>();
	    response.put("list", disasterService.getSavedDisasterMessages(searchParams));
	    response.put("totalCount", disasterService.getTotalCount(searchParams));
	    
	    return ResponseEntity.ok(response);
	}
	
	// 2. 재난문자 상세 조회
	@GetMapping("/disasterMessages/{id}")
	public ResponseEntity<?> getDisasterDetail(@PathVariable("id") Long id) {
	    return ResponseEntity.ok(disasterService.getDisasterDetail(id));
	}


//    
//    // 3. 일괄 노출/비노출 설정 (id 리스트 기준)
//    @PostMapping("/disasterMessages/visibility")
//    public ResponseEntity<String> updateVisibility(@RequestBody DisasterBatchRequest request) {
//        disasterService.updateDisasterVisibility(request.getIds(), request.getVisibleYn());
//        return ResponseEntity.ok("상태가 변경되었습니다.");
//    }
//    
    
    
    // 4. 재난문자 일괄 삭제 (논리 삭제)
    @PostMapping("/disasterMessages/delete")
    public ResponseEntity<String> deleteDisasters(@RequestBody List<Long> ids) { 
        disasterService.deleteDisasters(ids); 
        return ResponseEntity.ok("성공적으로 삭제되었습니다.");
    }
    
   
    
    
    
    // 5. 재난문자 수동 등록
    @PostMapping("/disasterMessages")
    public ResponseEntity<String> createDisaster(@RequestBody @Valid PredictionInfoResponse dto) {
    	disasterService.saveDisaster(dto);
        return ResponseEntity.ok("성공적으로 등록되었습니다.");
    }

    // 6. 재난문자 수정
    @PutMapping("/disasterMessages/{id}")
    public ResponseEntity<String> updateDisaster(@PathVariable("id") Long id, @RequestBody PredictionInfoResponse dto) {
        dto.setId(id); // DTO에 id 세팅 (sn은 null일 수 있으므로 id가 기준이 됨)
        disasterService.modifyDisaster(dto);
        return ResponseEntity.ok("성공적으로 수정되었습니다.");
    }
    
    
    // 재난문자 노출여부 설정
    @PostMapping("/updateMessageVisibility")
    public ResponseEntity<?> updateMessageVisibility(
            @RequestBody DisasterBatchRequest request) {

        log.info("노출 변경 요청 ids={}, visibleYn={}",
                 request.getIds(), request.getVisibleYn());

        boolean result =
            disasterService.updateDisasterVisibility(
                request.getVisibleYn(),
                request.getIds()
            );

        return ResponseEntity.ok(result);
    }

 // =============== 기상 특보 ===============
 // 기상특보 리스트 조회 (검색 파라미터 적용)
 @GetMapping("/weatherWarnings")
 public ResponseEntity<Map<String, Object>> getSavedWeatherWarnings(
         @ModelAttribute PredictionInfoResponse searchParams) {
     
     // ⭐ 프론트에서 limit이나 page를 안 보냈을 때를 위한 방어 코드
     if (searchParams.getLimit() <= 0) {
         searchParams.setLimit(100); // 기본 100개씩 조회
     }
     if (searchParams.getPage() <= 0) {
         searchParams.setPage(1);
     }
     
     Map<String, Object> result = new HashMap<>();
     
     // 1. 리스트 데이터 조회
     List<PredictionInfoResponse> list = disasterService.getSavedWeatherWarnings(searchParams);
     // 2. 전체 개수 조회
     int totalCount = disasterService.getWeatherTotalCount(searchParams);
     
     result.put("list", list);
     result.put("totalCount", totalCount);
     
     return ResponseEntity.ok(result);
 }
    
    // 1. 기상특보 상세 조회
    @GetMapping("/weatherWarnings/{key}")
    public ResponseEntity<PredictionInfoResponse> getWeatherDetail(@PathVariable("key") String key) {
        return ResponseEntity.ok(disasterService.getWeatherDetail(key));
    }
    
    
    
//    @GetMapping("/weatherWarnings")
//    public ResponseEntity<Map<String, Object>> getSavedWeatherWarnings(
//            @ModelAttribute PredictionInfoResponse searchParams) {
//        
//        // 프론트에서 limit을 안 보냈을 때를 대비해 기본값 강제 세팅
//        if (searchParams.getLimit() <= 10) { 
//            searchParams.setLimit(100); 
//        }
//
//        Map<String, Object> result = new HashMap<>();
//        // ... 이하 로직 동일
//    }
//    
    
    

    // 2. 기상특보 수동 등록
    @PostMapping("/weatherWarnings")
    public ResponseEntity<String> createWeather(@RequestBody PredictionInfoResponse dto) {
    	disasterService.saveWeather(dto);
        return ResponseEntity.ok("기상 특보가 등록되었습니다.");
    }

    // 3. 기상특보 수정
    @PutMapping("/weatherWarnings/{key}")
    public ResponseEntity<String> updateWeather(@PathVariable("key") String key, @RequestBody PredictionInfoResponse dto) {
        dto.setPrsntnSn(Integer.parseInt(key)); // 여기서 키를 세팅함
        disasterService.modifyWeather(dto);
        return ResponseEntity.ok("기상 특보가 수정되었습니다.");
    }

    // 4. 기상특보 일괄 노출 변경
    @PostMapping("/weatherWarnings/visibility")
    public ResponseEntity<String> updateWeatherVisibility(
            @RequestBody DisasterBatchRequest request) {

        log.info("기상특보 노출 변경 요청 ids={}, visibleYn={}",
                 request.getIds(), request.getVisibleYn());

        // DisasterBatchRequest.getIds() → List<Long> 라면 변환
        List<String> ids = request.getIds().stream()
                                  .map(String::valueOf)
                                  .toList();

        boolean result = disasterService.updateWeatherVisibility(
        		ids,
                request.getVisibleYn()
        );
        
//        boolean updateWeatherVisibility( List<String> ids, String visibleYn);

        if (!result) {
            return ResponseEntity.badRequest().body("노출 상태 변경 실패");
        }

        return ResponseEntity.ok("노출 상태가 변경되었습니다.");
    }


    // 5. 기상특보 일괄 삭제 (논리 삭제)
    @PostMapping("/weatherWarnings/delete")
    public ResponseEntity<String> deleteWeatherWarnings(@RequestBody List<String> keys) {
    	if (keys == null || keys.isEmpty()) {
            return ResponseEntity.badRequest().body("처리할 대상이 없습니다.");
        }
        disasterService.deleteWeatherWarnings(keys);
        return ResponseEntity.ok("선택한 항목이 비노출 처리되었습니다.");
    }
    
	
	
	
	
	
	
	
	
	
	// ========================================================================   api요청 ======================================================================================
	
	
		
	/**
	 * 산사태 정보 api 요청
	 * 주소 /api/disaster/dashboard/predictionInfo
	 * 요청정보 json
	  {
	  		"pageNo" : "1",
	  		"numOfRows" : "10",
	  		"type" : "xml", // 기본값 json
	  		"sgg" : "전라북도"
	  }
	 */
	@PostMapping("/predictionInfo")
	public Mono<ApiResponse<PublicDataResponse<PredictionInfoResponse>>> getLandslidePredictionInfo(@Valid @RequestBody DisasterExternApiRequest request){
		
		return publicDataService.getLandslidePredictionInfo(request, PredictionInfoResponse.class)
								.map(ApiResponse::success)  // 성공 시 표준 응답으로 래핑
				                .onErrorResume(e -> {
				                    log.error("Error occurred: {}", e.getMessage());
				                    return Mono.just(ApiResponse.error(
				                            ErrorCode.EXTERNAL_API_ERROR.code(),
				                            ErrorCode.EXTERNAL_API_ERROR.message()
				                    ));
				                })
				                .defaultIfEmpty(ApiResponse.error(
				                        ErrorCode.NOT_FOUND.code(),
				                        ErrorCode.NOT_FOUND.message()
				                ));
	}
	
	/**
	 * 통합정보(날씨 정보, 지진특보, 진도정보)api 요청
	 * 주소 /api/disaster/dashboard/integrationInfo
	 * 요청정보 json
	  {
	 		"weather": {
	 	        "lat" : "35.8350848",
	 	        "lon" : "127.123456",
	 	        "units" : "metric",
	 	        "lang" : "kr"
	 	    },
	 	    "earthquake": {
	 	        "orderTy" : "xml",
	 	        "eqArCd" : "A16"
	 	    }
	  }
	 */
	@PostMapping("/integrationInfo")
	public Mono<ApiResponse<JsonNode>> getIntegrationInfo(@RequestBody JsonNode request) {
		
		return integrationService.getIntegrationInfo(request)
								 .map(ApiResponse::success)  // 성공 시 표준 응답으로 래핑
				                 .onErrorResume(e -> {
				                    log.error("Error occurred: {}", e.getMessage());
				                    return Mono.just(ApiResponse.error(
				                            ErrorCode.EXTERNAL_API_ERROR.code(),
				                            ErrorCode.EXTERNAL_API_ERROR.message()
				                    ));
				                 })
				                 .defaultIfEmpty(ApiResponse.error(
				                        ErrorCode.NOT_FOUND.code(),
				                        ErrorCode.NOT_FOUND.message()
				                 ));
	}
	
	
	
	
	
	// =============================================================
	/**
	 * 재난문자이력 api 요청
	 * 주소 /api/disaster/dashboard/disasterMessageInfo
	 * 요청정보 json
	  {
	  		"pageNo" : "1",
	  		"numOfRows" : "30",
	  		"type" : "json", 
	  		"crtDt" : 
	  		"rgnNm" : "전북"
	  }
	 */
	// =============================================================
	/**
	 * 재난문자이력 api 요청 및 저장
	 * 주소 /api/disaster/dashboard/disasterMessageInfo
	 * * 이제 @RequestBody가 없으므로 Postman의 Params 탭에 값을 넣어 호출해!
	 */
	@PostMapping("/disasterMessageInfo")
	public Mono<ApiResponse<SafetyDataResponse<PredictionInfoResponse>>> getDisasterMessageInfo(@Valid DisasterExternApiRequest request) {	    // @RequestBody를 제거해서 이제 URL 파라미터(?pageNo=1&...)를 자동으로 인식함
	    log.info("재난문자 수집 요청 파라미터: {}", request);
	    
	    return publicDataService.getAndSaveDisasterMessages(request) 
	            .map(ApiResponse::success)
	            .onErrorResume(e -> {
	                log.error("재난문자 수집 에러 발생: {}", e.getMessage());
	                return Mono.just(ApiResponse.error(
	                        ErrorCode.EXTERNAL_API_ERROR.code(),
	                        ErrorCode.EXTERNAL_API_ERROR.message()
	                ));
	            })
	            .defaultIfEmpty(ApiResponse.error(
	                    ErrorCode.NOT_FOUND.code(),
	                    ErrorCode.NOT_FOUND.message()
	            ));
	}
	
	
	// =============================================================
    /**
     * 기상 특보 api 요청 및 저장
     * 주소 /api/disaster/dashboard/weatherWarningInfo
     * 요청정보 json
      {
            "pageNo" : "1",
            "numOfRows" : "50",
            "inqDt" : "20260128"
      }
     */
    @PostMapping("/weatherWarningInfo")
    public Mono<ApiResponse<SafetyDataResponse<PredictionInfoResponse>>> collectWeatherWarnings(@Valid @RequestBody DisasterExternApiRequest request){
        return publicDataService.getAndSaveWeatherWarnings(request) 
                                .map(ApiResponse::success)
                                .onErrorResume(e -> {
                                    log.error("기상 특보 수집 에러: {}", e.getMessage());
                                    return Mono.just(ApiResponse.error(
                                            ErrorCode.EXTERNAL_API_ERROR.code(),
                                            ErrorCode.EXTERNAL_API_ERROR.message()
                                    ));
                                });
    } 
}









