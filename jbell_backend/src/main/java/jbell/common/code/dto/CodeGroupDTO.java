package jbell.common.code.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeGroupDTO {
    private String groupCode;    // code_group_id
    private String groupName;    // code_group_name
    private String desc;         // code_desc
    private Integer order;       // sort_order
    private boolean visible;     // visible_yn ('Y'이면 true)
    private LocalDateTime date;  // created_at
}