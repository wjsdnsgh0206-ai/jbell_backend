package jbell.qna.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jbell.qna.dto.QnaBulkDelete;
import jbell.qna.dto.QnaCreate;
import jbell.qna.dto.QnaDetail;
import jbell.qna.dto.QnaList;
import jbell.qna.dto.QnaUpdate;
import jbell.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class QnaRestController {
	
	private final QnaService qnaService;

	/**
     * 관리자용 QnA 목록 조회 API
     * URL: GET /api/admin/qnalist
     * 결과: 모든 사용자의 문의 내역 (최신순)
     */
    @GetMapping("/admin/qnalist")
    public ResponseEntity<List<QnaList>> getQnaList() {
        List<QnaList> qnaList = qnaService.getQnaList();
        return ResponseEntity.ok(qnaList);
    }
	 
    /**
     * QnA 상세 조회 API
     * URL: GET /api/admin/qnadetail?qnaId=3
     */
    @GetMapping("/admin/qnadetail")
    public ResponseEntity<QnaDetail> getQnaDetail(@RequestParam("qnaId") Long qnaId) {
        QnaDetail qnaDetail = qnaService.getQnaDetail(qnaId);
        return ResponseEntity.ok(qnaDetail);
    }
    
    /**
     * QnA 삭제 API (단건 및 다건 통합)
     * URL: DELETE /api/admin/qnadelete
     * Body: { "qnaIds": [1, 2, 3] }
     */
    @DeleteMapping("/admin/qnadelete") // @PostMapping 대신 DeleteMapping 권장 (RESTful)
    public ResponseEntity<Void> deleteQna(
            @RequestBody QnaBulkDelete qnaBulkDelete,
            HttpSession session) {
        
        // 1. 세션 체크 (관리자 권한 확인 로직은 생략되었으나 필요 시 추가)
    	String requestUserId = qnaBulkDelete.getUserId();
        log.info("삭제 요청자 ID: {}", requestUserId);
        

        // 2. 유효성 검사
        if (qnaBulkDelete.getQnaIds() == null || qnaBulkDelete.getQnaIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 3. 서비스 호출
        qnaService.deleteQna(qnaBulkDelete.getQnaIds());
        
        return ResponseEntity.ok().build();
    }
}
