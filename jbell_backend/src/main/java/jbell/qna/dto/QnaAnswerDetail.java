package jbell.qna.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QnaAnswerDetail {

	private int qnaAnswerId;			// 답변 고유 식별자
	private int qnaId;					// 연결된 문의글의 식별자 (FK)
	private String content;				// 답변 본문 내용
	private LocalDateTime createdAt;	// 답변 등록 일시
	private String userId;				// 답변을 작성한 관리자 ID (FK)
}
