package jbell.qna.dto;

import lombok.Data;

@Data
public class QnaCreate{

	private String qnaCategoryId;			// 문의 카테고리 ID (FK: code_item)
	private String title;					// 문의 제목
	private String content;					// 문의 본문 내용 (DB Column: question_content)
	private String userId;					// 작성자 식별자 (FK: user)
    
    /**
     * 노출 여부
     * DB Column Type: char(1)
     * 허용 값: "Y" or "N"
     */
	private String isVisible;
}
