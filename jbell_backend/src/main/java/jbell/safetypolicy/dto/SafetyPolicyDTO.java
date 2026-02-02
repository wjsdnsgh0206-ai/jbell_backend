package jbell.safetypolicy.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 목록 조회(부분 조회)와 상세 조회(전체 조회)를 모두 소화하기 위해
 * Record 대신 Class + @Data 패턴을 사용합니다.
 */
@Data
public class SafetyPolicyDTO {
    private Long contentId;
    private String title;
    private String body;
    private String source;      // 출처 기관명
    private String contentLink; // 원문 링크
    private String visibleYn;
    private String regType;     // A: 관리자 등록, S: 시스템 동기화
    private LocalDateTime createdAt;
    private String userId;      // 작성자
    private List<Long> fileIds;
}