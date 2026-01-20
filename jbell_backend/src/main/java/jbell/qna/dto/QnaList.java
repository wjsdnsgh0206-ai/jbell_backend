package jbell.qna.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QnaList {

	private int qnaId;                      // 문의 ID (inquiry_id)
    private String qnaCategoryId;           // 카테고리 ID (question_type_id)
    private String categoryName;            // 카테고리 명칭 (code_item_name) - 추가됨
    private String title;                   // 제목 (title)
    private String status;                  // 답변 상태 (answer_status)
    private String userId;                  // 작성자 ID (user_id)
    private LocalDateTime createdAt;        // 등록일시 (created_at)
}
