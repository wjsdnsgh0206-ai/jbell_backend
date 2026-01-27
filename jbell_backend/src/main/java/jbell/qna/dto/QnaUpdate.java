package jbell.qna.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QnaUpdate {

	private String qnaCategoryId;			// 문의 카테고리 ID
	private String title;					// 문의 제목
	private String content;					// 문의 본문 내용
	private String isVisible;				// 노출 여부
}
