package jbell.disaster.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.Valid;
import jbell.common.response.ApiResponse;
import jbell.disaster.dto.DisasterExternApiRequest;
import jbell.disaster.dto.PredictionInfoResponse;
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
	@PostMapping("/disasterMessageInfo")
	public Mono<ApiResponse<SafetyDataResponse<PredictionInfoResponse>>> getDisasterMessageInfo(@Valid @RequestBody DisasterExternApiRequest request){
	    
	    return publicDataService.getAndSaveDisasterMessages(request) 
	                            .map(ApiResponse::success)
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
	
	@GetMapping("/disasterMessages")
	public ResponseEntity<List<PredictionInfoResponse>> getDisasterMessages() {
	    // 서비스에서 DB 데이터를 가져오라고 시킴
	    List<PredictionInfoResponse> list = publicDataService.getSavedDisasterMessages();
	    return ResponseEntity.ok(list);
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
//    @PostMapping("/weatherWarningInfo")
//    public Mono<ApiResponse<SafetyDataResponse<PredictionInfoResponse>>> getWeatherWarningInfo(@Valid @RequestBody DisasterExternApiRequest request){
//        
//        // 서비스에서 7일치 데이터를 수집하고 저장한 뒤 결과를 리턴함
//        return publicDataService.getAndSaveWeatherWarnings(request) 
//                                .map(ApiResponse::success)
//                                .onErrorResume(e -> {
//                                    log.error("기상 특보 수집 에러: {}", e.getMessage());
//                                    return Mono.just(ApiResponse.error(
//                                            ErrorCode.EXTERNAL_API_ERROR.code(),
//                                            ErrorCode.EXTERNAL_API_ERROR.message()
//                                    ));
//                                })
//                                .defaultIfEmpty(ApiResponse.error(
//                                        ErrorCode.NOT_FOUND.code(),
//                                        ErrorCode.NOT_FOUND.message()
//                                ));
//    }

    /**
     * DB에 저장된 기상 특보 목록 가져오기
     * 주소 /api/disaster/dashboard/weatherWarnings
     */
//    @GetMapping("/weatherWarnings")
//    public ResponseEntity<List<PredictionInfoResponse>> getWeatherWarnings() {
//        // 서비스에서 DB에 저장된 기상 특보 데이터를 가져옴
//        List<PredictionInfoResponse> list = publicDataService.getSavedWeatherWarnings();
//        return ResponseEntity.ok(list);
//    }
    
 // 1. [데이터 수집용 POST] 
    // 기존의 fetchAndSaveWeatherWarning와 getWeatherWarningInfo(RequestBody 있는 버전)를 하나로 합침
	// 1. 데이터 수집/저장용 (POST)
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

    // 2. DB 데이터 조회용 (GET)
    @GetMapping("/weatherWarnings")
    public ResponseEntity<List<PredictionInfoResponse>> getSavedWeatherWarnings() {
        List<PredictionInfoResponse> list = publicDataService.getSavedWeatherWarnings();
        return ResponseEntity.ok(list);
    }
}









