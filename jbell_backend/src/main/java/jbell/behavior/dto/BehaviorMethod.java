package jbell.behavior.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BehaviorMethod {
	
	@JsonProperty("safety_cate_nm1")
	private String safetyCateNm1;
    
    @JsonProperty("safety_cate_nm2")
	private String safetyCateNm2;
	
	@JsonProperty("safety_cate_nm3")
	private String safetyCateNm3;
    
    @JsonProperty("actRmks")
    private String sgg;
}
