package jbell.facility.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // 기본 생성자
@AllArgsConstructor // 전체 인자 생성자 (Builder 사용 시 필수)
@Builder            // 빌더 패턴 활성화
public class FacilityDTO {
    private Long fcltId;
    private String fcltNm;
    private String fcltSeCd;
    private String ctpvNm;
    private String sggNm;
    private String roadNmAddr;
    
    // 추가된 컬럼
    private Double lat; 
    private Double lot;
    
    private String opnYn;
    private String useYn;
    private Integer fcltArea;
    private Integer fcltCapacity; // 컬럼명 변경 대응 (rcvCapacity -> fcltCapacity)
    private LocalDateTime regDt;
}