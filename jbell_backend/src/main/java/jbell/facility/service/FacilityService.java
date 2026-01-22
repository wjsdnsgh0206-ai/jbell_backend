package jbell.facility.service;

import java.util.List;

import jbell.facility.dto.FacilityDTO;
import jbell.facility.dto.FacilityListRequest;
import jbell.facility.dto.FacilityListResponse;
import jbell.facility.eunm.ApiType;
import reactor.core.publisher.Mono;

public interface FacilityService {
    Mono<FacilityDTO> getFacilityDetail(Long fcltId); // 상세
    Mono<Void> insertFacility(FacilityDTO dto);       // 등록
    Mono<Void> updateFacility(FacilityDTO dto);       // 수정
    Mono<Void> deleteFacilities(List<Long> ids);      // 삭제
	
	// 파라미터를 객체(Request)로 변경하고, 리턴 타입을 명확한 DTO(Response)로 변경
    Mono<FacilityListResponse> getFacilityListData(FacilityListRequest request);
	
	
	
	
    /**
     * 모든 재난 안전 API 데이터를 통합 동기화합니다.
     */
    void syncAllFacility();

    /**
     * 개별 API 주소를 통해 데이터를 수집하고 DB에 업서트합니다.
     * @param apiUrl 호출할 API URL
     * @param serviceKey API 인증키
     * @param fcltSeCd 해당 대피소의 구분 코드
     */
    void syncOneApi(String apiUrl, String serviceKey, ApiType type);
}
