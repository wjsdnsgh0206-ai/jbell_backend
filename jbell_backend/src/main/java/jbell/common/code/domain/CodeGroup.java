package jbell.common.code.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CodeGroup {
    private String codeGroupId;      // 코드그룹 코드 (PK)
    private String codeGroupName;    // 코드그룹 명
    private String codeDesc;         // 코드그룹 설명
    private LocalDateTime createdAt; // 등록일시
    private LocalDateTime updatedAt; // 수정일시
    private Integer sortOrder;       // 출력 순서
    private String visibleYn;        // 사용 여부 (Y/N)
}