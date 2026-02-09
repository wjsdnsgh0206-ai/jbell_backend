package jbell.disasterAccident.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisasterAccidentDTO {
	// 산불 (forest_fire_information)
	private Long fireId; // fire_id 매핑
	private Double fireDamageArea; // fire_damage_area 매핑
	private String fireEndTime; // fire_end_time 매핑 (String으로 받아 MyBatis가 DATETIME으로 변환)
	private String fireCause; // fire_cause 매핑
	private String fireLocVillage; // fire_loc_village 매핑
	private String fireStartTime; // fire_start_time 매핑
	// 산불 위험 예보용 필드 추가
	private String analDate; // 데이터기준일자
	private String doName; // 지역명 (전국)
	private Double avgIndex; // 평균지수 (meanavg)
	private Double maxIndex; // 최댓값 (maxi)
	private Double minIndex; // 최솟값 (mini)
	private Long area; // 면적
	private String fireExposeYn; // 노출 여부 (Y/N)

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

	// 기상특보 (kma_weather_report) (ex. 한파특보, 태풍특보..)
//		private String tmFc; // 발표시각 (이미 지진 테이블에 있어서 주석처리함.)
	private Integer tmSeq; // 발표번호
	private String areaCode; // 지역코드
	private Integer warnVar; // 특보종류 (2:호우, 3:한파, 7:태풍 등)
	private String stnId; // 지점코드
	private String areaName; // 지역명
	private Integer warnStress; // 특보강도
	private String startTime; // 특보발효시각
	private String endTime; // 특보종료시각
	private int type;
	
	
	
	// 댐 & 하천 수위 (wkw_wl_hrdata)
	// http://www.wamis.go.kr:8080/wamis/openapi/wkw/wl_hrdata?obscd=4001605&startdt=20251225
    private String obsNm;       // 관측소명 (obsnm)
    private String obsCd;       // 관측소코드 (obscd)
    private String bbsnNm;      // 하천명 (bbsnnm)
    private String mngOrg;      // 관리기관 (mngorg)
    private Double waterLevel;  // 현재수위 (wl) - API 리스트에는 없지만 상세 데이터에 포함됨
    private String obsTime;     // 관측시각 (ymdhm)
    
    // 재난 발생 관리 상태 변경
    private List<String> ids; // 프론트에서 보낸 복합 ID 목록 (예: ["FIRE_123", "WTH_3_10_108"])
    private boolean isVisible; // true: 노출(Y), false: 비노출(N)
    
}