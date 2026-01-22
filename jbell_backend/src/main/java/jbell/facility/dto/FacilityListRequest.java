package jbell.facility.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FacilityListRequest {
    // 검색 조건
    private String ctpvNm;     // 시도 명
    private String sggNm;      // 시군구 명
    private String fcltNm;     // 시설 명
    private String roadNmAddr; // 도로명 주소

    // 페이징 및 정렬 (기본값 설정)
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private int page = 1;
    
    private int size = 30;     // 30개씩 조회

    private String sortKey = "reg_dt";
    
    private String sortOrder = "DESC";
}