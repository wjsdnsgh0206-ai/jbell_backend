package jbell.disaster.dto;

import java.time.LocalDateTime;

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
//
//    // 산사태 정보 필드
//    @JsonProperty("lndslFrcstNm")
//    private String lndslFrcstNm;
//
//    @JsonProperty("prctnInfoAnlssDt")
//    private String prctnInfoAnlssDt;
//
//    @JsonProperty("sgg")
//    private String sgg;
	
	private String visibleYn;
	
	// 검색 페이지 이동 필드 
	private int page = 1;      // 현재 페이지 번호
    private int limit = 10;    // 페이지당 데이터 수
    private int offset;        // 시작 지점 (자동 계산용)

    public int getOffset() {
        return (this.page - 1) * this.limit;
    }
	
	

    // 재난문자이력관리 필드
    @JsonProperty("sn") 
    private Integer sn;

    @JsonProperty("crtDt") 
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime crtDt;

    @JsonProperty("msgCn")
    private String msgCn;

    @JsonProperty("rcptnRgnNm") 
    private String rcptnRgnNm;

    @JsonProperty("emrgStepNm")
    private String emrgStepNm;

    @JsonProperty("dstType")
    private String dstType;
    
    
    // 기상청 실시간 특보 필드

        @JsonProperty("PRSNTN_SN") // ⭐ API 응답이 대문자일 확률이 매우 높음!
        private Integer prsntnSn;

        @JsonProperty("TTL")
        private String ttl;

        @JsonProperty("PRSNTN_TM")
        private String prsntnTm;

        @JsonProperty("RLVT_ZONE")
        private String rlvtZone;

        @JsonProperty("SPNE_FRMNT_PRCON_CN") // ⭐ 아까 명세서에 있던 긴 이름!
        private String content;

        @JsonProperty("TIME_TXT")
        private String timeTxt;

        @JsonProperty("MAAS_OBNT_DT")
        private String maasObntDt;

        
        private String lvl;        // 기상 수준 (주의, 위험 등)
        
     // [추가] 검색 및 페이징을 위한 필드
        private String startDate;  // XML의 #{startDate}와 매핑
        private String endDate;    // XML의 #{endDate}와 매핑
        private String keyword;
     // [추가] 검색 필터용 필드 - XML의 <if test="newsType"> 등과 매치됨
        private String newsType;  
        private String region;
        private String level;

}