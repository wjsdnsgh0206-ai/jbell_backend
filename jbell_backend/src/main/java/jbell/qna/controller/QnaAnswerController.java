package jbell.qna.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jbell.qna.dto.QnaAnswerCreate;
import jbell.qna.dto.QnaAnswerDetail;
import jbell.qna.dto.QnaAnswerUpdate;
import jbell.qna.service.QnaAnswerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QnaAnswerController {
	
	private final QnaAnswerService qnaAnswerService;

    // 답변 등록
    @PostMapping("/qna/answers")
    public ResponseEntity<Long> createAnswer(@RequestBody QnaAnswerCreate createDto) {
        Long answerId = qnaAnswerService.createAnswer(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(answerId);
    }

    // 문의에 대한 답변 조회
    @GetMapping("/qna/{qnaId}/answers")
    public ResponseEntity<QnaAnswerDetail> getAnswer(@PathVariable("qnaId") Long qnaId) {
        QnaAnswerDetail answerDetail = qnaAnswerService.getAnswer(qnaId);
        return ResponseEntity.ok(answerDetail);
    }

    // 답변 수정
    @PatchMapping("/qna/answers/{qnaAnswerId}")
    public ResponseEntity<Void> updateAnswer(
    		@PathVariable("qnaAnswerId") Long qnaAnswerId,
            @RequestBody QnaAnswerUpdate updateDto) {
        // PathVariable의 ID를 DTO에 주입하여 일관성 유지
        updateDto.setQnaAnswerId(qnaAnswerId);
        qnaAnswerService.updateAnswer(updateDto);
        return ResponseEntity.ok().build();
    }

    // 답변 삭제
    @DeleteMapping("/qna/answers/{qnaAnswerId}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable("qnaAnswerId") Long qnaAnswerId) {
        qnaAnswerService.deleteAnswer(qnaAnswerId);
        return ResponseEntity.ok().build();
    }

}
