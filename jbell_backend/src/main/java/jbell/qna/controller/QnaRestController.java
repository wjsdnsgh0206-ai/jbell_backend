package jbell.qna.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jbell.qna.dto.QnaCreate;
import jbell.qna.dto.QnaDetail;
import jbell.qna.dto.QnaList;
import jbell.qna.dto.QnaUpdate;
import jbell.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class QnaRestController {
	
	private final QnaService qnaService;

	/**
     * QnA 목록 조회 API
     * URL: GET /api/user/qnalist?userId=user1
     */
    @GetMapping("/qnalist")
    public ResponseEntity<List<QnaList>> getQnaList(@RequestParam("userId") String userId) {
        List<QnaList> qnaList = qnaService.getQnaList(userId);
        return ResponseEntity.ok(qnaList);
    }
	
    /**
     * QnA 등록 API
     * URL: POST /api/user/qna
     */
    @PostMapping("/qnaadd")
    public ResponseEntity<Void> createQna(@RequestBody QnaCreate qnaCreate) {
        qnaService.createQna(qnaCreate);
        return ResponseEntity.ok().build();
    }
    
    /**
     * QnA 상세 조회 API
     * URL: GET /api/user/qnadetail?qnaId=1
     */
    @GetMapping("/qnadetail")
    public ResponseEntity<QnaDetail> getQnaDetail(@RequestParam("qnaId") int qnaId) {
        QnaDetail qnaDetail = qnaService.getQnaDetail(qnaId);
        return ResponseEntity.ok(qnaDetail);
    }
    
    /**
     * QnA 수정 API (보안 강화됨)
     * URL: PUT /api/user/qnaupdate?qnaId=1
     * * [보안 변경점]
     * userId를 파라미터로 받지 않고, 서버의 HttpSession에서 직접 추출합니다.
     * 이를 통해 타인의 ID로 위변조하여 요청하는 공격을 차단합니다.
     */
    @PutMapping("/qnaupdate")
    public ResponseEntity<Void> updateQna(
            @RequestParam("qnaId") int qnaId,
            @RequestBody QnaUpdate qnaUpdate,
            HttpSession session) {
        
        String sessionUserId = (String) session.getAttribute("userId");
        
        if (sessionUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            qnaService.updateQna(qnaId, sessionUserId, qnaUpdate);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.warn("QnA 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
