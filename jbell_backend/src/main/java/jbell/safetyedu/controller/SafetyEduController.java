package jbell.safetyedu.controller;

import jbell.safetyedu.dto.RequestDto;
import jbell.safetyedu.dto.ResponseDto;
import jbell.safetyedu.service.SafetyEduService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 시민안전교육 관리 컨트롤러
 * Base Path 변경: /api
 * Resource Path: /safety-edu
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SafetyEduController {

    private final SafetyEduService safetyEduService;

    /**
     * 목록 조회
     * GET /api/safety-edu
     */
    @GetMapping("/safety-edu")
    public ResponseEntity<Page<ResponseDto>> getSafetyEduList(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", required = false, defaultValue = "all") String searchType,
            @RequestParam(value = "isPublic", required = false, defaultValue = "all") String isPublic
    ) {
        Page<ResponseDto> result = safetyEduService.getSafetyEduList(pageable, keyword, searchType, isPublic);
        return ResponseEntity.ok(result);
    }

    /**
     * 상세 조회
     * GET /api/safety-edu/{id}
     */
    @GetMapping("/safety-edu/{id}")
    public ResponseEntity<ResponseDto> getSafetyEduDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(safetyEduService.getSafetyEduDetail(id));
    }

    /**
     * 등록
     * POST /api/safety-edu
     */
    @PostMapping("/safety-edu")
    public ResponseEntity<Long> createSafetyEdu(@RequestBody RequestDto requestDto) {
        return ResponseEntity.ok(safetyEduService.createSafetyEdu(requestDto));
    }

    /**
     * 수정
     * PUT /api/safety-edu/{id}
     */
    @PutMapping("/safety-edu/{id}")
    public ResponseEntity<Long> updateSafetyEdu(
            @PathVariable("id") Long id, // ("id") 추가
            @RequestBody RequestDto requestDto
    ) {
        return ResponseEntity.ok(safetyEduService.updateSafetyEdu(id, requestDto));
    }

    /**
     * 삭제
     * DELETE /api/safety-edu/{id}
     */
    @DeleteMapping("/safety-edu/{id}")
    public ResponseEntity<Void> deleteSafetyEdu(@PathVariable("id") Long id) { // ("id") 추가
        safetyEduService.deleteSafetyEdu(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 일괄 삭제
     * POST /api/safety-edu/batch-delete
     */
    @PostMapping("/safety-edu/batch-delete")
    public ResponseEntity<Void> deleteSafetyEdus(@RequestBody List<Long> ids) {
        safetyEduService.deleteSafetyEdus(ids);
        return ResponseEntity.ok().build();
    }

    /**
     * 노출 상태 변경
     * PATCH /api/safety-edu/visibility
     */
    @PatchMapping("/safety-edu/visibility")
    public ResponseEntity<Void> updateVisibility(
            @RequestBody List<Long> ids, 
            @RequestParam(value = "isPublic") Boolean isPublic
    ) {
        safetyEduService.updateVisibility(ids, isPublic);
        return ResponseEntity.ok().build();
    }
}