////
//// ====== 재난문자이력관리 DTO ======
////service에서 api를 호출하여 가져온 데이터들을 dto에 담는다. 
//// DTO의 역할은 "데이터의 규격화"가 핵심이다.
//package jbell.disasterAccident.dto;
//
//import java.time.LocalDateTime;
//
//import com.fasterxml.jackson.annotation.JsonFormat;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import lombok.Data;
//
//// @Data : Lombok라이브러리 기능. Getter, Setter 등을 적지 않아도 자동으로 만들어줌. 
//// @JsonInclude : 데이터중에 null값은 json결과에 넣지 않는다.
//// @JsonIgnoreProperties : api응답 중, 내가 사용하지 않는 데이터는 무시한다.
//@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class DisasterMessageResponse {
//    
//	// @JsonProperty : 공공 api에서 받아오는 데이터의 이름을 하나로 통일.
//    @JsonProperty("sn")
//    private Integer sn; // 일련번호
//    
//    // DTO에서 @JsonFormat을 사용하여 날짜 데이터를 문자열로 변환해 전달하므로, 
//    // 프론트엔드에서는 추가적인 날짜 변환 라이브러리 없이도 간편하게 출력 가능함.
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss", timezone = "Asia/Seoul")
//    private LocalDateTime crtDt; // 생성일시
//    
//    @JsonProperty("msgCn")
//    private String msgCn; // 메시지내용
//    
//    @JsonProperty("rcptnRgnNm")
//    private String rcptnRgnNm; // 수신지역명
//    
//    @JsonProperty("emrgStepNm")
//    private String emrgStepNm; // 긴급단계명
//    
//    @JsonProperty("dstType")
//    private String dstType; // 시스템재난구분
//}