package jbell.common.code.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeItemDTO {
    private String groupCode;    // 상위 그룹 ID (FK)
    private String subCode;      // 상세 코드 ID (PK)
    private String subName;      // 상세 코드 명
    private String desc;         // 상세 설명
    private Integer order;       // 출력 순서
    private boolean visible;     // 사용 여부
    private LocalDateTime date;  // 등록일시
}