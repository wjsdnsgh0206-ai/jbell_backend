package jbell.disaster.mapper;

import jbell.disaster.dto.PredictionInfoResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface DisasterMapper {
    
	// 재난 문자 (disaster_text_history) ============================
    List<PredictionInfoResponse> selectDisasterList(PredictionInfoResponse searchParams);
    int selectDisasterTotalCount(PredictionInfoResponse searchParams);

    PredictionInfoResponse selectDisasterDetail(Long id);
    
    int updateDisasterVisibility(@Param("visibleYn") String visibleYn, @Param("ids") List<Long> ids);
    int deleteDisasterLogical(@Param("list") List<Long> ids); 

    // 재난 문자 등록/수정
    int insertDisaster(PredictionInfoResponse disaster);
    int updateDisaster(PredictionInfoResponse disaster);

    // 중복 방지를 위해 DB의 가장 큰 SN 값을 가져오는 메서드
    Long selectMaxSn();

    

    // 기상 특보 (weather_warning_info)  ============================
    List<PredictionInfoResponse> selectWeatherList(PredictionInfoResponse searchParams);
    // 기상특보 총 검색 결과
    int selectWeatherTotalCount(PredictionInfoResponse searchParams);
    
    
    
    List<PredictionInfoResponse> selectWeatherList();
    PredictionInfoResponse selectWeatherDetail(@Param("prsntnSn") String key);
    int insertWeather(PredictionInfoResponse weather);
    int updateWeather(PredictionInfoResponse weather);
//    int updateWeatherVisibility(@Param("list") List<String> keys, @Param("visibleYn") String visibleYn);
    
    int updateWeatherVisibility(
    	    @Param("ids") List<String> ids,
    	    @Param("visibleYn") String visibleYn
    	);
    
    
    int deleteWeatherLogical(@Param("list") List<String> keys);
}