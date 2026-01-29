package jbell.safetyedu.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 시민안전교육 상세 및 목록 조회를 위한 응답 DTO
 * 근거: AdminSafetyEduData.js의 데이터 구조
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ResponseDto {

    // 콘텐츠 ID (DB: content.content_id)
    private Long id;

    // 관리번호
    private String mgmtId;

    // 등록 방식
    private String regType;

    // 정렬 순서 (DB: content.ordering)
    private Integer orderNo;

    // 시설명/교육명 (DB: content.title)
    private String title;

    // 출처 기관
    private String source;

    // 출처 URL (DB: content.content_link)
    private String sourceUrl;

    // 노출 여부 (DB: content.visible_yn -> Boolean 변환)
    private Boolean isPublic;

    // 등록자 (DB: user.user_name 또는 content.user_id)
    private String author;

    // 등록 일시 (DB: content.created_at) - 포맷팅은 프론트 또는 JSON 설정에서 처리
    private LocalDateTime createdAt;

    // 수정 일시 (DB: content.last_update_date)
    private LocalDateTime updatedAt;

    // 내용 요약
    private String summary;

    // 하단 공지사항
    private String footerNotice;

    // 문의처 정보
    private String contact;

    // 교육 세부 내용
    private List<SectionDto> sections;

    // 관련 사이트 링크
    private List<LinkDto> links;

    // RequestDto의 내부 클래스 재사용 또는 별도 정의
    // (여기서는 구조적 명확성을 위해 동일한 구조를 가짐을 명시)
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class SectionDto {
        private Object id; // 프론트 데이터에서 숫자형/문자형 혼용 가능성 고려
        private String subTitle;
        private List<ItemDto> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ItemDto {
        private Object id;
        private String type;
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LinkDto {
        private Object id;
        private String label;
        private String url;
    }
}
