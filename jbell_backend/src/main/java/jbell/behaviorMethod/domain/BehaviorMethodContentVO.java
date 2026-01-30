package jbell.behaviorMethod.domain;

import java.util.List;

import lombok.Data;

@Data
public class BehaviorMethodContentVO {
    private Long contentId;         // content_id
    private String title;           // title (예: 태풍 예보시 행동요령)
    private String body;            // body (행동요령 상세 내용)
    private String contentType;     // content_type (예: NATURAL_TYPHOON)
    private String contentTypeName; // 코드명 (예: 태풍)
    private String groupName;       // 그룹명 (예: 자연재난) 
    private String contentLink;     // content_link (이미지 또는 영상 URL)
    private String ordering;        // ordering (정렬 순서)
    private String visibleYn;       // visible_yn (노출 여부)
    private String lastSyncYn;      // 최신 동기화 여부 (Y/N)
    private String regType;         // 등록 방식 (API / MANUAL)
    private List<Long> fileIds; // 프론트에서 보내주는 업로드된 파일 ID 목록
}