package jbell.behaviorMethod.domain;

import lombok.Data;

@Data
public class BehaviorMethodContentVO {
    private Long contentId;       // content_id
    private String title;         // title (예: 태풍 예보시 행동요령)
    private String body;          // body (행동요령 상세 내용)
    private String contentType;   // content_type (예: NATURAL_TYPHOON)
    private String contentLink;   // content_link (이미지 또는 영상 URL)
    private String ordering;      // ordering (정렬 순서)
    private String visibleYn;     // visible_yn
}