package jbell.qna.dto;

import lombok.Data;

@Data
public class QnaAnswerCreate {

	private int qnaId;					// 연결된 문의글의 식별자 (FK)
	private String content;				// 답변 본문 내용
	private String userId;				// 답변을 작성한 관리자 ID (FK)
}
