package jbell.safetyedu.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {
    // PK: content_id
    private Long contentId;

    // 제목
    private String title;

    // 콘텐츠 타입 (FK: code_item.code_item_id -> 'SAFETY_EDU')
    private String contentType;

    // 노출 여부 ('Y' or 'N')
    private String visibleYn;

    // 본문 (JSON 데이터 저장)
    private String body;

    // 링크 (출처 URL 등)
    private String contentLink;

    // 정렬 순서
    private Integer ordering;

    // 작성자 ID (FK: user.user_id -> 'ADMIN_MASTER')
    private String userId;

    // 등록 일시
    private LocalDateTime createdAt;

    // 수정 일시
    private LocalDateTime lastUpdateDate;
}