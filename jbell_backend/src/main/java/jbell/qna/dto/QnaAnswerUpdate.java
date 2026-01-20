package jbell.qna.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QnaAnswerUpdate {

	private Long qnaAnswerId;    // 답변 고유 시별자 (보통 path variable로도 가능)
	private String content;		// 수정할 답변 본문 내용
}
