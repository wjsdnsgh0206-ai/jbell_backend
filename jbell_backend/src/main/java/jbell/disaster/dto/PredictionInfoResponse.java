package jbell.disaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "item")
public record PredictionInfoResponse(
    
    @JsonProperty("lndslFrcstNm")
    @JacksonXmlProperty(localName = "lndslFrcstNm")
    String lndslFrcstNm,
    
    @JsonProperty("prctnInfoAnlssDt")
    @JacksonXmlProperty(localName = "prctnInfoAnlssDt")
    String prctnInfoAnlssDt,
    
    @JsonProperty("sgg")
    @JacksonXmlProperty(localName = "sgg")
    String sgg
) {}
