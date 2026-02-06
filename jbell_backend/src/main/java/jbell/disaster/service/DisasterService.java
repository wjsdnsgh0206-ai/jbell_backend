package jbell.disaster.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import jbell.disaster.dto.PredictionInfoResponse;
import jbell.disaster.mapper.DisasterMapper;

public interface DisasterService {

	// 리스트 조회
	public List<PredictionInfoResponse> getSavedDisasterMessages(PredictionInfoResponse searchParams);

	// 검색 조건에 맞는 전체 데이터 개수 (페이지네이션용)
	int getTotalCount(PredictionInfoResponse searchParams);

	List<PredictionInfoResponse> getDisasterMessages(PredictionInfoResponse searchParams);

	// 상세 조회
	public PredictionInfoResponse getDisasterDetail(Long id);

	// 재난 문자 일괄 노출 변경
	public boolean updateDisasterVisibility(String visibleYn, List<Long> ids);

	// 일괄 삭제 (sns -> ids로 명칭 변경)
	void deleteDisasters(List<Long> ids);

	void saveDisaster(PredictionInfoResponse dto);

	void modifyDisaster(PredictionInfoResponse dto);

	// ===================== 기상 특보 =======================
	// 기상특보 리스트 (검색 조건 적용)
	public List<PredictionInfoResponse> getSavedWeatherWarnings(PredictionInfoResponse searchParams);

	public int getWeatherTotalCount(PredictionInfoResponse searchParams);

	// 기상특보 리스트
	public List<PredictionInfoResponse> getSavedWeatherWarnings();

	public PredictionInfoResponse getWeatherDetail(String key);

	public void saveWeather(PredictionInfoResponse dto);

	public void modifyWeather(PredictionInfoResponse dto);

	// 기상특보 노출여부 일괄 변경
	public boolean updateWeatherVisibility(List<String> ids, String visibleYn);

	public void deleteWeatherWarnings(List<String> keys);

}
