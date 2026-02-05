package jbell.safetypolicy.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jbell.common.response.ApiResponse;
import jbell.common.response.PageResponse;
import jbell.safetypolicy.dto.SafetyPolicyDTO;
import jbell.safetypolicy.service.SafetyPolicyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/safetyPolicy")
@RequiredArgsConstructor
public class SafetyPolicyController {

    private final SafetyPolicyService safetyPolicyService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SafetyPolicyDTO>>> getSafetyPolicyList(
            // [수정] name 속성을 명시하여 컴파일 후에도 파라미터 이름을 인식할 수 있게 함
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "visibleYn", required = false) String visibleYn
    ) {
        var result = safetyPolicyService.getSafetyPolicyList(page, size, keyword, visibleYn);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SafetyPolicyDTO>> getSafetyPolicyDetail(@PathVariable(name = "id") Long id) { // [수정] PathVariable에도 name 추가 권장
        return ResponseEntity.ok(ApiResponse.success(safetyPolicyService.getSafetyPolicyDetail(id)));
    }
    
    // [수정] 노출 여부 일괄 변경 (Map 사용)
    @PatchMapping("/visibility")
    public ResponseEntity<ApiResponse<Void>> updateVisibility(@RequestBody Map<String, Object> params) {
        // 1. JSON 배열은 List<Integer>로 들어올 수 있으므로 Long으로 안전하게 변환
        List<Long> ids = ((List<?>) params.get("ids")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(Collectors.toList());
        
        String visibleYn = (String) params.get("visibleYn");

        safetyPolicyService.updateVisibility(ids, visibleYn);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // [추가] 정책 삭제 (일괄/단건 공용)
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteSafetyPolicies(@RequestBody Map<String, Object> params) {
        List<Long> ids = ((List<?>) params.get("ids")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(Collectors.toList());

        safetyPolicyService.deleteSafetyPolicies(ids);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    // [추가] 등록/수정 (기존 DTO 활용)
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createSafetyPolicy(@RequestBody SafetyPolicyDTO safetyPolicyDTO) {
        // TODO: 로그인된 사용자 ID 세팅 필요 (예: dto.setUserId(principal.getName()))
        Long id = safetyPolicyService.createSafetyPolicy(safetyPolicyDTO);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Long>> updateSafetyPolicy(@PathVariable(name = "id") Long id, @RequestBody SafetyPolicyDTO safetyPolicyDTO) {
    	safetyPolicyDTO.setContentId(id);
        safetyPolicyService.updateSafetyPolicy(safetyPolicyDTO);
        return ResponseEntity.ok(ApiResponse.success(id));
    }
}