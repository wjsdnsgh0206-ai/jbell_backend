package jbell.qna.dto;

import lombok.Data;
import java.time.LocalDateTime;
@Data

public class Qna {

	private int qnaId;						// 문의 고유 식별자 (PK)
	private String qnaCategoryId;			// 문의 카테고리 ID
	private String title;					// 문의 제목
	private String content;					// 문의 본문 내용
	private String status;   				// 답변 상태(답변대기 / 답변처리중 / 답변완료)
	private String isVisible;				// 노출 여부
	private LocalDateTime createdAt;		// 문의 등록 일시
	private String userId;					// 작성자 식별자
}
