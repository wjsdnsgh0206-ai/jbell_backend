package jbell.qna.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jbell.qna.dto.QnaCreate;
import jbell.qna.dto.QnaDetail;
import jbell.qna.dto.QnaList;
import jbell.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api") // api.js의 경로와 일치시킴
@RequiredArgsConstructor
@Slf4j
public class QnaUserRestController {

    private final QnaService qnaService;

    /**
     * 사용자 1:1 문의 등록 API
     * URL: POST /api/qna/add
     * api.js: qnaService.createQna
     */
    @PostMapping("/qna/add")
    public ResponseEntity<Void> createQna(
            @RequestBody QnaCreate qnaCreate,
            HttpSession session) {
    	
    	log.info("문의 등록 요청 데이터: {}", qnaCreate);
        
        // 1. 세션에서 로그인된 사용자 ID 가져오기 (보안)
        // (실제 프로젝트 환경에 맞춰 세션 키 "userId" 또는 "loginUser" 등을 확인하세요)
        String userId = (String) session.getAttribute("userId");
        
        // 테스트용: 세션이 없으면 DTO에 담겨온 ID 사용 (실무에서는 401 에러 리턴 권장)
        if (userId != null) {
            qnaCreate.setUserId(userId);
        }

        if (qnaCreate.getUserId() == null || qnaCreate.getUserId().isEmpty()) {
            log.warn("작성자 ID 누락으로 인한 등록 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 서비스 호출
        qnaService.createQna(qnaCreate);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 사용자 1:1 문의 목록 조회 API
     * URL: GET /api/qna/list
     * 설명: 비밀글 여부(isVisible)가 포함된 목록을 반환
     */
    @GetMapping("/qna/list")
    public ResponseEntity<List<QnaList>> getQnaList() {
        List<QnaList> list = qnaService.getQnaList();
        log.info("조회된 문의 개수: {}", list != null ? list.size() : 0);
        return ResponseEntity.ok(list);
    }

    /**
     * 사용자 1:1 문의 상세 조회 API
     * URL: GET /api/qna/detail/{qnaId}
     * api.js: qnaService.getPublicQnaDetail
     */
    @GetMapping("/qna/detail/{qnaId}")
    public ResponseEntity<QnaDetail> getQnaDetail(@PathVariable("qnaId") Long qnaId) {
        QnaDetail detail = qnaService.getQnaDetail(qnaId);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }
}