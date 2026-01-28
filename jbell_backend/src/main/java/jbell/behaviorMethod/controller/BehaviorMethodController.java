package jbell.behaviorMethod.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jbell.behaviorMethod.domain.BehaviorMethodContentVO;
import jbell.behaviorMethod.service.BehaviorMethodService;
import jbell.common.response.ApiResponse;
import jbell.exception.CustomException;
import jbell.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/behaviorMethod")
@RequiredArgsConstructor
@Slf4j
public class BehaviorMethodController {
	
	private final BehaviorMethodService behaviorMethodService;

	@PostMapping("/admin/sync")
	public Mono<ApiResponse<Map<String, String>>> syncSafetyData(){
		
		
		return behaviorMethodService.safetyDataSave()
							  .map(ApiResponse::success)  // 성공 시 표준 응답으로 래핑 (ApiResponse.success(data)와 동일)
			                  .onErrorResume(e -> {
			                    log.error("Error occurred: {}", e.getMessage());
			                    throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
			                  })
			                  .defaultIfEmpty(ApiResponse.error(
			                        ErrorCode.NOT_FOUND.code(),
			                        ErrorCode.NOT_FOUND.message()
			                  ));
	}
	
	/**
     * 행동요령 조회 API
     * @param contentType 재난 유형 (예: NATURAL_TYPHOON, NATURAL_EARTHQUAKE)
     * @return 행동요령 리스트
     */
    @GetMapping("/list")
    public List<BehaviorMethodContentVO> getBehaviorList(@RequestParam("contentType") String contentType) {
        return behaviorMethodService.getBehaviorList(contentType);
    }
}
