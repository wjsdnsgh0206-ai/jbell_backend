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

	@PostMapping("/admin/sync/natural") // 자연재난 행동요령 동기화
	public Mono<ApiResponse<Map<String, String>>> syncNaturalBehaviorMethod(){
		return behaviorMethodService.syncNatural()
									.map(ApiResponse::success) // 에러 처리는 Service 또는 GlobalExceptionHandler에서 관행적으로 처리하거나 기존 유지
									.onErrorResume(e -> {
										log.error("Error occurred: {}", e.getMessage());
										throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
									})
									.defaultIfEmpty(ApiResponse.error(
										ErrorCode.NOT_FOUND.code(),
										ErrorCode.NOT_FOUND.message()
									));
	}

	@PostMapping("/admin/sync/social") // 사회재난 행동요령 동기화
	public Mono<ApiResponse<Map<String, String>>> syncSocialBehaviorMethod(){
		return behaviorMethodService.syncSocial()
									.map(ApiResponse::success)
									.onErrorResume(e -> {
										log.error("Error occurred: {}", e.getMessage());
										throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
									})
									.defaultIfEmpty(ApiResponse.error(
										ErrorCode.NOT_FOUND.code(),
										ErrorCode.NOT_FOUND.message()
									));
	}

	@PostMapping("/admin/sync/life") // 생활안전 행동요령 동기화
	public Mono<ApiResponse<Map<String, String>>> syncLifeBehaviorMethod(){
		return behaviorMethodService.syncLife()
									.map(ApiResponse::success) 
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
     * 행동요령 조회 API (사용자/관리자 공용)
     * @param contentType 재난 유형 (Null이면 전체 조회), (예: NATURAL_TYPHOON, NATURAL_EARTHQUAKE)
     * @param visibleYn 노출 여부 (Null이면 전체 조회, 'Y'면 노출만 조회)
     */
    @GetMapping("/list")
    public List<BehaviorMethodContentVO> getBehaviorList(
        @RequestParam(value = "contentType", required = false) String contentType,
        @RequestParam(value = "visibleYn", required = false) String visibleYn
    ) {
        // 1. 관리자 페이지에서 호출 시: contentType 없이 호출됨 -> 전체 조회
        // 2. 사용자 페이지에서 호출 시: contentType="NATURAL_TYPHOON" -> 특정 유형만 조회
        return behaviorMethodService.getBehaviorList(contentType, visibleYn);
    }
    
}
