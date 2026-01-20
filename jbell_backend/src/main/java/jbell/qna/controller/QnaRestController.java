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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class QnaRestController {
	
	private final QnaService qnaService;

	/**
     * 관리자용 QnA 목록 조회 API
     * URL: GET /api/admin/qnalist
     * 결과: 모든 사용자의 문의 내역 (최신순)
     */
    @GetMapping("/qnalist")
    public ResponseEntity<List<QnaList>> getQnaList() {
        List<QnaList> qnaList = qnaService.getQnaList();
        return ResponseEntity.ok(qnaList);
    }
	
    /**
     * QnA 등록 API
     * URL: POST /api/admin/qnaadd
     * 로직: 세션에서 사용자 ID를 가져와 DTO에 설정 후 등록
     */
    @PostMapping("/qnaadd")
    public ResponseEntity<Void> createQna(@RequestBody QnaCreate qnaCreate, HttpSession session) {
        
        // 1. 세션 체크 (로그인 여부 확인)
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            log.warn("QnA 등록 실패: 로그인되지 않은 사용자 접근");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 작성자 ID 주입 (FK: user 테이블 참조 무결성 보장)
        qnaCreate.setUserId(userId);
        
        // 3. 서비스 호출
        qnaService.createQna(qnaCreate);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * QnA 상세 조회 API
     * URL: GET /api/admin/qnadetail?qnaId=3
     */
    @GetMapping("/qnadetail")
    public ResponseEntity<QnaDetail> getQnaDetail(@RequestParam("qnaId") Long qnaId) {
        QnaDetail qnaDetail = qnaService.getQnaDetail(qnaId);
        return ResponseEntity.ok(qnaDetail);
    }
    
    /**
     * QnA 수정 API (보안 적용 완료)
     * URL: PUT /api/admin/qnaupdate?qnaId={id}
     * - 테스트용 파라미터 제거됨
     * - 오직 세션(HttpSession)에 저장된 사용자 ID로만 권한을 검증함
     * - 테스트 코드 : QnA 수정 테스트용 코드.txt
     */
    @PutMapping("/qnaupdate")
    public ResponseEntity<Void> updateQna(
            @RequestParam("qnaId") Long qnaId,
            @RequestBody QnaUpdate qnaUpdate,
            HttpSession session) {
        
        // 1. 세션 체크 (로그인 여부 확인)
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            // 로그인하지 않은 사용자는 접근 불가
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 2. 서비스 호출 (세션 ID 전달)
            qnaService.updateQna(qnaId, userId, qnaUpdate);
            return ResponseEntity.ok().build();
            
        } catch (IllegalStateException e) {
            // 작성자가 아니거나, 답변완료 상태라 수정 불가한 경우
            log.warn("QnA 수정 거부: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            
        } catch (IllegalArgumentException e) {
            // 잘못된 qnaId 요청
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * QnA 삭제 API (단건 및 다건 통합)
     * URL: DELETE /api/admin/qnadelete
     * Body: { "qnaIds": [1, 2, 3] }
     */
    @DeleteMapping("/qnadelete") // @PostMapping 대신 DeleteMapping 권장 (RESTful)
    public ResponseEntity<Void> deleteQna(
            @RequestBody QnaBulkDelete qnaBulkDelete,
            HttpSession session) {
        
        // 1. 세션 체크 (관리자 권한 확인 로직은 생략되었으나 필요 시 추가)
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        

        // 2. 유효성 검사
        if (qnaBulkDelete.getQnaIds() == null || qnaBulkDelete.getQnaIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 3. 서비스 호출
        qnaService.deleteQna(qnaBulkDelete.getQnaIds());
        
        return ResponseEntity.ok().build();
    }
}
