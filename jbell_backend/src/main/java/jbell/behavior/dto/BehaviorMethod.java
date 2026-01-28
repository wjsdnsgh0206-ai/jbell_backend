package jbell.behavior.dto;

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

/*
"safety_cate1": "01", // 자연재난, 사회재난, 생활안전
"safety_cate_nm1": "자연재난" 
"safety_cate2": "01002", // 태풍, 홍수, 호우 등
"safety_cate_nm2": "홍수",
"safety_cate3": "01002003", // 특정 상황(전, 중, 후 등)
"safety_cate_nm3": "물이 밀려들 때는",
"safety_cate4": null, // 
"actRmks": null, // 본문
"contentsUrl": "http://mepv2.safekorea.go.kr/mdbs_html/images/mobileWeb/bTyphoon/bTyphoon_icon_08.png", // 첨부 자료


"safety_cate1": "01",
"safety_cate_nm1": "자연재난"
"safety_cate2": "01002",
"safety_cate_nm2": "홍수",
"safety_cate3": "01002003",
"safety_cate_nm3": "물이 밀려들 때는",
"safety_cate4": null,
"actRmks": "흐르는 물에 들어가지 맙시다.",
"contentsUrl": null,
*/