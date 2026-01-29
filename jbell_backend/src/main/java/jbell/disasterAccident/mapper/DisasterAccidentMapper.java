package jbell.disasterAccident.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import jbell.disasterAccident.dto.DisasterAccidentDTO;

@Mapper
public interface DisasterAccidentMapper {
	int insertEarthquake(DisasterAccidentDTO dto);

	int insertTyphoonInfo(DisasterAccidentDTO dto);

	int insertTyphoonTrack(DisasterAccidentDTO dto);

	int insertLandslide(DisasterAccidentDTO dto);

	void insertForestFire(DisasterAccidentDTO dto);

	// 기상특보 (ex. 호우특보, 한파특보, 태풍특보)
	int insertKmaWeather(DisasterAccidentDTO dto);
	List<DisasterAccidentDTO> selectKmaWeatherByType(int type);
}
