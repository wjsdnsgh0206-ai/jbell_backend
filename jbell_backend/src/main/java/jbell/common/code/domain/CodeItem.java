package jbell.common.code.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CodeItem {
    private String codeItemId;      // 상세 코드 (PK)
    private String codeGroupId;     // 그룹 코드 (FK)
    private String codeItemName;    // 상세 코드 명
    private String description;     // 상세 설명
    private LocalDateTime createdAt; // 등록일시
    private LocalDateTime updatedAt; // 수정일시
    private Integer sortOrder;       // 출력 순서
    private String visibleYn;        // 사용 여부 (Y/N)
}