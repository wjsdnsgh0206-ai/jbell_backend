package jbell.qna.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QnaDetail {

	private int qnaId;						// 문의 고유 식별자 (PK)
	private String qnaCategoryId;			// 문의 카테고리 ID
	private String title;					// 문의 제목
	private String content;					// 문의 본문 내용
	private String status;   				// 답변 상태(답변대기 / 답변처리중 / 답변완료)
	private String isVisible;				// 노출 여부
	private LocalDateTime createdAt;		// 문의 등록 일시
	private String userId;					// 작성자 식별자
	
	private String categoryName;            // 카테고리명 (code_item 테이블)
    private String answerContent;           // 답변 내용 (inquiry_answer 테이블)
    private LocalDateTime answerCreatedAt;  // 답변 등록 일시 (inquiry_answer 테이블)
}
