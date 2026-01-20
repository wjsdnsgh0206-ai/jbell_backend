package jbell.qna.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QnaAnswer {

	private Long qnaAnswerId;       // answer_id (PK)
    private String content;         // answer_content
    private LocalDateTime createdAt; // created_at
    private LocalDateTime updatedAt; // updated_at (보완: 수정 일시)
    private String deletedYn;       // deleted_yn (보완: 논리적 삭제 여부)
    private String userId;          // user_id (FK)
    private Long qnaId;             // inquiry_id (FK)
}
