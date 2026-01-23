package jbell.qna.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QnaDetail {

	private Long qnaId;						// 문의 고유 식별자{inquiry_id} (PK)
	private String qnaCategoryId;			// 문의 카테고리 ID{question_type_id}
	private String categoryName;            // 카테고리명 (code_item.code_item_name)
	private String title;					// 문의 제목{title}
	private String content;					// 문의 본문 내용{question_content}
	private String status;   				// 답변 상태{answer_status}(답변대기 / 답변처리중 / 답변완료)
	private LocalDateTime createdAt;		// 문의 등록 일시{created_at}
	private String userId;					// 작성자 식별자{user_id}
	private String userName;                // user.user_name
	

	private Long qnaAnswerId;				// answer_id (PK)
    private String answerContent;           // 답변 내용 (inquiry_answer.answer_content)
    private LocalDateTime answerCreatedAt;  // 답변 등록 일시 (inquiry_answer.created_at)
    
    private String answerUserId;   // 답변자 ID (inquiry_answer.user_id)
    private String answerUserName; // 답변자 이름 (user.user_name)
}
