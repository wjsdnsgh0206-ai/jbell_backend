package jbell.disaster.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import jbell.disaster.dto.PredictionInfoResponse;

@Mapper
public interface WeatherWarningMapper {
	
	// 최신 발표 시각 조회 추가
    String selectLatestPrsntnTm();
    
    
    /**
     * 기상 특보 데이터 일괄 저장 (중복 시 업데이트)
     */
    int insertWeatherWarnings(List<PredictionInfoResponse> warnings);
    
    /**
     * DB에 저장된 기상 특보 목록 조회 (최신순)
     */
    List<PredictionInfoResponse> selectWeatherWarningList();
    
    /**
     * 특정 날짜 범위나 개수로 조회하고 싶을 때 사용
     */
    List<PredictionInfoResponse> selectLatestWarnings(int limit);
}