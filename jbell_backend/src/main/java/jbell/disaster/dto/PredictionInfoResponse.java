package jbell.disaster.dto;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // MyBatis 조회를 위한 기본 생성자 (핵심!)
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionInfoResponse {


	// 재난문자이력관리 필드 ===================
	@JsonProperty("id")
	private Long id;

	@JsonProperty("SN")
    private Long sn;

    @JsonProperty("MSG_CN")
    private String msgCn;

    @JsonProperty("RCPTN_RGN_NM")
    private String rcptnRgnNm;

    @JsonProperty("CRT_DT")
    private String crtDt;

    @JsonProperty("EMRG_STEP_NM")
    private String emrgStepNm;

    @JsonProperty("DST_SE_NM")
    private String dstType; // 변수명은 네 마음대로 해도 되지만 @JsonProperty는 API랑 똑같이!

    @JsonProperty("REG_YMD")
    private String regYmd;
 
	
    

	private String visibleYn;

	// 검색 페이지 이동 필드
	private int page = 1; // 현재 페이지 번호
	private int limit = 10; // 페이지당 데이터 수
	private int offset; // 시작 지점 (자동 계산용)

	public int getOffset() {
		return (this.page - 1) * this.limit;
	}
	
	
	
	// 기상청 실시간 특보 필드 ===================
	@JsonProperty("PRSNTN_SN") 
	private Integer prsntnSn;

	@JsonProperty("TTL")
	private String ttl;

	@JsonProperty("PRSNTN_TM")
	private String prsntnTm;

	@JsonProperty("RLVT_ZONE")
	private String rlvtZone;

	@JsonProperty("SPNE_FRMNT_PRCON_CN") 
	private String content;

	@JsonProperty("TIME_TXT")
	private String timeTxt;

	@JsonProperty("MAAS_OBNT_DT")
	private String maasObntDt;
	
	
	// ===================

	private String lvl; // 기상 수준 (주의, 위험 등)

	// [추가] 검색 및 페이징을 위한 필드
	private String startDate; // XML의 #{startDate}와 매핑
	private String endDate; // XML의 #{endDate}와 매핑
	private String keyword;
	// [추가] 검색 필터용 필드 - XML의 <if test="newsType"> 등과 매치됨
	private String newsType;
	private String region;
	private String level;

}