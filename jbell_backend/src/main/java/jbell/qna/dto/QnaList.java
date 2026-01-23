package jbell.qna.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QnaList {

	private Long qnaId;             // 번호 (inquiry.inquiry_id)
    private String status;          // 상태 (inquiry.answer_status)
    private String categoryName;    // 문의유형 (code_item.code_item_name)
    private String title;           // 제목 (inquiry.title)
    private String userName;        // 작성자 이름 (user.user_name)
    private String userId;          // 작성자 ID (상세 조회용, user.user_id)
    private LocalDateTime createdAt;// 등록일 (inquiry.created_at)
}
