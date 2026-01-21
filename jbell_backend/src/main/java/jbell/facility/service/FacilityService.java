package jbell.facility.service;

import java.util.Map;

import jbell.facility.dto.FacilityDTO;
import jbell.facility.eunm.ApiType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FacilityService {
	
	
	Mono<Map<String, Object>> getFacilityListData(
		    String ctpvNm, String sggNm, String fcltNm, String roadNmAddr, // 추가
		    int page, String sortKey, String sortOrder
		);
	
	
	
	
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
