package jbell.facility.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDTO {
    // DB 컬럼 매핑
    private Long fcltId;       // fclt_id (자동증가지만 API ID와 매핑 필요시 사용)
    private String fcltNm;     // fclt_nm
    private String fcltSeCd;   // fclt_se_cd (대피소 코드)
    private String ctpvNm;     // ctpv_nm (시도)
    private String sggNm;      // sgg_nm (시군구)
    private String roadNmAddr; // road_nm_addr
    private Double lat;        // lat
    private Double lot;        // lot
    private String opnYn;      // opn_yn
    private String useYn;      // use_yn
    private String regDt;      // 날짜
}
