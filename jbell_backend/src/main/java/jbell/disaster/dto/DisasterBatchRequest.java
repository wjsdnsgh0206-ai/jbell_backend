package jbell.disaster.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class DisasterBatchRequest {
    private List<Long> ids;      // Long 타입의 sn 또는 String 타입의 key 리스트
    private String visibleYn;    // 'Y' 또는 'N'
}