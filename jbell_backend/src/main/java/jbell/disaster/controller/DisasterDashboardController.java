package jbell.disaster.controller;

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
}









