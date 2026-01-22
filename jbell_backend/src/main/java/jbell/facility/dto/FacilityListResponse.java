package jbell.facility.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityListResponse {

    @JsonProperty("totalCount")
    private long totalCount; // 검색된 총 데이터 개수

    @JsonProperty("items")
    private List<FacilityDTO> items; // 제공해주신 FacilityDTO 리스트
}