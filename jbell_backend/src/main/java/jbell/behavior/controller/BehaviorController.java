package jbell.behavior.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import jbell.behavior.service.BehaviorService;
import jbell.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/behavior-method")
@RequiredArgsConstructor
public class BehaviorController {
	
	private final BehaviorService behaviorService;
	
	@PostMapping("/admin/synctest")
    public Mono<ApiResponse<JsonNode>> syncSafetyData(@RequestParam(name="safetyCategory") String safetyCategory){
			/*
			return behaviorService.getBehaviorInfo(safetyCategory)
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
			*/
		behaviorService.getBehaviorInfo(safetyCategory);
		return null;
	}
}
