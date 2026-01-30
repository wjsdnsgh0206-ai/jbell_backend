package jbell.disaster.service;

import java.util.List;

import jbell.disaster.dto.PredictionInfoResponse;

public interface  DisasterService {

    // 리스트 조회
	public List<PredictionInfoResponse> getSavedDisasterMessages(PredictionInfoResponse searchParams);
    // 검색 조건에 맞는 전체 데이터 개수 (페이지네이션용)
    public int getTotalCount(PredictionInfoResponse searchParams);
    
    // 상세 조회
    public PredictionInfoResponse getDisasterDetail(Long sn);
    // 일괄 노출 변경
    public void updateDisasterVisibility(List<Long> sns, String visibleYn);

    // 일괄 삭제 (논리 삭제)
    public void deleteDisasters(List<Long> sns);
    
    public void saveDisaster(PredictionInfoResponse dto);

    public void modifyDisaster(PredictionInfoResponse dto);
    
    
    
    // ===================== 기상 특보 =======================
    // 기상특보 리스트 (검색 조건 적용)
    public List<PredictionInfoResponse> getSavedWeatherWarnings(PredictionInfoResponse searchParams);

    public int getWeatherTotalCount(PredictionInfoResponse searchParams);
    
    
    
    // 기상특보 리스트
    public List<PredictionInfoResponse> getSavedWeatherWarnings();

    public PredictionInfoResponse getWeatherDetail(String key);

    public void saveWeather(PredictionInfoResponse dto);

    public void modifyWeather(PredictionInfoResponse dto);

    public void updateWeatherVisibility(List<String> keys, String visibleYn);

    public void deleteWeatherWarnings(List<String> keys);
    

}
