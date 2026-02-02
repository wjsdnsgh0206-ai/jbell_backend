package jbell.behaviorMethod.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping; // 추가됨
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	// ========================================================================
	// [동기화 API] 관리자 전용
	// ========================================================================

	@PostMapping("/admin/sync/natural") // 자연재난 행동요령 동기화
	public Mono<ApiResponse<Map<String, String>>> syncNaturalBehaviorMethod(){
		return behaviorMethodService.syncNatural()
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
	 * [관리자용] 과거 동기화 데이터 일괄 삭제 (Cleanup)
	 * - 사용되지 않는(last_sync_yn='N') API 데이터를 삭제합니다.
	 */
	@DeleteMapping("/admin/cleanup")
	public ApiResponse<String> cleanupOldData() {
		behaviorMethodService.deleteOldSyncData();
		return ApiResponse.success("과거 동기화 데이터가 삭제되었습니다.");
	}
	
	// ========================================================================
    // [단건 상세 조회 & 수정]
    // ========================================================================

    /**
     * 행동요령 상세 조회 API
     * @param contentId 콘텐츠 ID
     */
    @GetMapping("/{contentId}")
    public ApiResponse<BehaviorMethodContentVO> getBehaviorDetail(@PathVariable("contentId") Long contentId) {
        BehaviorMethodContentVO detail = behaviorMethodService.getBehaviorDetail(contentId);
        if (detail == null) {
            // 데이터가 없으면 404 리턴 (프론트에서 처리 용이하게)
            return ApiResponse.error(ErrorCode.NOT_FOUND.code(), "데이터를 찾을 수 없습니다."); 
        }
        return ApiResponse.success(detail);
    }

    /**
     * 행동요령 수정 API (제목, 본문, 노출여부 등)
     * @param contentId 콘텐츠 ID
     * @param updateData 수정할 데이터 (JSON Body)
     */
    @PutMapping("/{contentId}")
    public ApiResponse<String> updateBehaviorMethod(
        @PathVariable("contentId") Long contentId,
        @RequestBody BehaviorMethodContentVO updateData
    ) {
        // PathVariable의 ID를 VO에 주입하여 안전하게 처리
        updateData.setContentId(contentId);
        behaviorMethodService.updateBehaviorMethod(updateData);
        return ApiResponse.success("성공적으로 수정되었습니다.");
    }
	
	// ========================================================================
	// [조회 API] 사용자/관리자 공용
	// ========================================================================
	
    /**
     * 행동요령 조회 API
     * @param contentType 재난 유형 (Null이면 전체 조회)
     * @param visibleYn 노출 여부 (Null이면 전체, 'Y'면 노출만)
     * @param onlyLatest [관리자용] 최신 데이터만 보기 여부 ('Y'면 최신 API + 수동등록 데이터만 조회)
     */
    @GetMapping("/list")
    public List<BehaviorMethodContentVO> getBehaviorList(
        @RequestParam(value = "contentType", required = false) String contentType,
        @RequestParam(value = "visibleYn", required = false) String visibleYn,
        @RequestParam(value = "onlyLatest", required = false) String onlyLatest // ★ 추가됨
    ) {
        // 1. 관리자 페이지: onlyLatest='Y'를 보내면 최신 데이터만 필터링
        // 2. 사용자 페이지: 파라미터 없이 호출 -> 전체 조회 (단, visibleYn='Y'는 프론트에서 보냄)
        return behaviorMethodService.getBehaviorList(contentType, visibleYn, onlyLatest);
    }
    
    /**
     * 행동요령 수동 등록 API
     * @param vo 등록할 데이터 (JSON Body)
     */
    @PostMapping
    public ApiResponse<Long> createBehaviorMethod(@RequestBody BehaviorMethodContentVO vo) {
        // regType을 MANUAL로 설정하여 API 동기화 데이터와 구분
        vo.setRegType("MANUAL");
        
        Long resultId = behaviorMethodService.registerBehaviorMethod(vo);
        
        if (resultId != null && resultId > 0) {
            return ApiResponse.success(resultId);
        } else {
            return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.code(), "등록에 실패하였습니다.");
        }
    }

    /**
     * 행동요령 삭제 API (단건 및 다건 일괄 삭제)
     * @param request 삭제할 ID 리스트가 담긴 Map (예: { "ids": [1, 2, 3] })
     */
    @DeleteMapping
    public ApiResponse<String> deleteBehaviorMethods(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.error(ErrorCode.BAD_REQUEST.code(), "삭제할 대상이 없습니다.");
        }
        
        behaviorMethodService.deleteBehaviorMethods(ids);
        return ApiResponse.success("성공적으로 삭제되었습니다.");
    }
    
    /**
     * 행동요령 노출 여부 일괄 변경 API
     * @param request 변경할 ID 리스트와 상태값이 담긴 Map 
     * (예: { "ids": [1, 2], "visibleYn": "Y" })
     */
    @PatchMapping("/visibility")
    public ApiResponse<String> updateVisibility(@RequestBody Map<String, Object> request) {
        List<Long> ids = (List<Long>) request.get("ids");
        String visibleYn = (String) request.get("visibleYn");

        if (ids == null || ids.isEmpty() || visibleYn == null) {
            return ApiResponse.error(ErrorCode.BAD_REQUEST.code(), "필수 파라미터가 누락되었습니다.");
        }

        behaviorMethodService.updateVisibility(ids, visibleYn);
        return ApiResponse.success("상태가 성공적으로 변경되었습니다.");
    }
}