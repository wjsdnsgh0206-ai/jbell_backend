package jbell.behaviorMethod.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BehaviorMethod {
	

	// 재난구분1 (자연재난/사회재난/생활안전)
	@JsonProperty("safety_cate_nm1")
	private String safetyCateNm1;
    
	// 재난구분2 (태풍, 홍수, ...)
    @JsonProperty("safety_cate_nm2")
	private String safetyCateNm2;
	
    // 행동구분 (태풍 발생 전 행동요령, 홍수 예.경보시 대피요령, ...)
	@JsonProperty("safety_cate_nm3")
	private String safetyCateNm3;
    
	// 행동요령 본문
    @JsonProperty("actRmks")
    private String body;
    
    // 카테고리 코드 예: "01001" -> 중분류: 세부 재난/안전 유형 (태풍, 홍수, 지진,  ...)
    @JsonProperty("safety_cate2")
    private String safetyCate2;
    
    // 카테고리 코드 예: "01002001" -> 소분류: 진행 단계별 행동 지침 (전/중/후)
    @JsonProperty("safety_cate3")
    private String safetyCate3;

    // 카테고리 코드 예: "01002001001" -> 상황별/장소별 행동 지침
    @JsonProperty("safety_cate4")
    private String safetyCate4;
    
    // DB에 저장할 때 쓸 가공된 순서값 (API에는 없지만 우리가 만듦)
    private int ordering;
    
    // 영상 링크 필드 추가
    @JsonProperty("contentsUrl")
    private String contentsUrl;
}
