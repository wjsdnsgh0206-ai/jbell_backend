package jbell.qna.dto;

import lombok.Data;

@Data
public class QnaCreate{

	/**
     * 문의 카테고리 ID (FK: code_item.code_item_id)
     */
	private String qnaCategoryId;
	
	private String title;					// 문의 제목
	private String content;					// 문의 본문 내용 (DB Column: question_content)
	private String userId;					// 작성자 식별자 (FK: user.user_id) - 세션에서 주입
    
    /**
     * 노출 여부 (DB Column: visible_yn)
     * Type: char(1)
     * 허용 값: "Y" 또는 "N"
     */
	private String isVisible;
}
