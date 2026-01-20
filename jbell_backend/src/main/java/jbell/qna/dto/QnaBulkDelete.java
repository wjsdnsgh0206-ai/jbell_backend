package jbell.qna.dto;

import java.util.List;

import lombok.Data;

@Data
public class QnaBulkDelete {

	private List<Integer> qnaId;			// 문의 고유 식별자 (PK)
}
