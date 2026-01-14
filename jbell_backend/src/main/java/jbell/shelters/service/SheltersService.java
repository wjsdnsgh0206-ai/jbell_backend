package jbell.shelters.service;

import jbell.shelters.eunm.ApiType;

public interface SheltersService {
    /**
     * 모든 재난 안전 API 데이터를 통합 동기화합니다.
     */
    void syncAllShelters();

    /**
     * 개별 API 주소를 통해 데이터를 수집하고 DB에 업서트합니다.
     * @param apiUrl 호출할 API URL
     * @param serviceKey API 인증키
     * @param fcltSeCd 해당 대피소의 구분 코드
     */
    void syncOneApi(String apiUrl, String serviceKey, ApiType type);
}
