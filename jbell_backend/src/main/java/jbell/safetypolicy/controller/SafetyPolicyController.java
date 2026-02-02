package jbell.safetypolicy.controller;

import jbell.safetypolicy.service.SafetyPolicyService;
import jbell.common.response.ApiResponse;
import jbell.common.response.PageResponse;
import jbell.safetypolicy.dto.SafetyPolicyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}