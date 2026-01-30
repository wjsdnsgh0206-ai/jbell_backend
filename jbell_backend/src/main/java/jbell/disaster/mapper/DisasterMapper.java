package jbell.disaster.mapper;

import jbell.disaster.dto.PredictionInfoResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DisasterMapper {
    
    // --- 재난 문자 (disaster_text_history) ---
	List<PredictionInfoResponse> selectDisasterList(PredictionInfoResponse searchParams);
    // 전체 카운트 쿼리와 연결
    int selectDisasterTotalCount(PredictionInfoResponse searchParams);
    PredictionInfoResponse selectDisasterDetail(Long sn);
    int updateDisasterVisibility(@Param("list") List<Long> sns, @Param("visibleYn") String visibleYn);
    int deleteDisasterLogical(@Param("list") List<Long> sns); // 논리 삭제 (visible_yn = 'N')
    // 재난 문자 등록
    int insertDisaster(PredictionInfoResponse disaster);
    // 재난 문자 수정 (메시지 내용, 긴급단계 등)
    int updateDisaster(PredictionInfoResponse disaster);
    
    // --- 기상 특보 (weather_warning_info) ---
    List<PredictionInfoResponse> selectWeatherList(PredictionInfoResponse searchParams);
    // 기상특보 총 검색 결과
    int selectWeatherTotalCount(PredictionInfoResponse searchParams);
    
    
    
    List<PredictionInfoResponse> selectWeatherList();
    PredictionInfoResponse selectWeatherDetail(@Param("prsntnSn") String key);
    int insertWeather(PredictionInfoResponse weather);
    int updateWeather(PredictionInfoResponse weather);
    int updateWeatherVisibility(@Param("list") List<String> keys, @Param("visibleYn") String visibleYn);
    int deleteWeatherLogical(@Param("list") List<String> keys);
}