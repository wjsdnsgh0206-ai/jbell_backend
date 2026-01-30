package jbell.disaster.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor // MyBatis 조회를 위한 기본 생성자 (핵심!)
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionInfoResponse {

    // 산사태 정보 필드
    @JsonProperty("lndslFrcstNm")
    private String lndslFrcstNm;

    @JsonProperty("prctnInfoAnlssDt")
    private String prctnInfoAnlssDt;

    @JsonProperty("sgg")
    private String sgg;

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

}