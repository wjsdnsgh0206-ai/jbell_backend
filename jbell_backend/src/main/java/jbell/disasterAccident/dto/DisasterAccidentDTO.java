package jbell.disasterAccident.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisasterAccidentDTO {
    // 산불 (forest_fire_information)
	private Long fireId;             // fire_id 매핑
    private Double fireDamageArea;   // fire_damage_area 매핑
    private String fireEndTime;      // fire_end_time 매핑 (String으로 받아 MyBatis가 DATETIME으로 변환)
    private String fireCause;        // fire_cause 매핑
    private String fireLocVillage;   // fire_loc_village 매핑
    private String fireStartTime;    // fire_start_time 매핑
 // 산불 위험 예보용 필드 추가
    private String analDate;   // 데이터기준일자
    private String doName;     // 지역명 (전국)
    private Double avgIndex;   // 평균지수 (meanavg)
    private Double maxIndex;   // 최댓값 (maxi)
    private Double minIndex;   // 최솟값 (mini)
    private Long area;         // 면적

    // 지진 (earthquake_event)
    private Long seq;
    private Double lat;
    private Double lon;
    private String loc;
    private String rem;
    private String tmFc;
    private Double mt;
    private Double msc;
    private Integer intensity;

    // 태풍 (typhoon_info & typhoon_track)
    private Integer typhoonYear;
    private Integer typhoonNo;
    private String typhoonName;
    private String typhoonNameDesc;
    private String typhoonActiveYn;
    private String typhoonFirstLocation;
    
    private String typhoonAnalysisDatetime;
    private Double typhoonLat;
    private Double typhoonLon;
    private Double typhoonMoveSpeed;
    private Integer typhoonCentralPressure;
    private Double typhoonMaxWindSpeed;
    private Integer typhoonRadius15ms;
    private Integer typhoonRadius25ms;
    private String typhoonLocation;
    private Integer typhoonReportNo;

    // 산사태 (lnld_forecast_info)
    private String lnldFrcstNm;
    private String sggNm;
    private String predcAnlsDt;
}