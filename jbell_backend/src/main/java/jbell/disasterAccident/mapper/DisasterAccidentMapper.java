package jbell.disasterAccident.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import jbell.disasterAccident.dto.DisasterAccidentDTO;

@Mapper
public interface DisasterAccidentMapper {


	int insertTyphoonInfo(DisasterAccidentDTO dto);

	int insertTyphoonTrack(DisasterAccidentDTO dto);

	int insertLandslide(DisasterAccidentDTO dto);

	// 산불
	void insertForestFire(DisasterAccidentDTO dto);
	List<DisasterAccidentDTO> selectForestFireList();

	// 📌 산불 위험 예보 (새로 추가)
	void insertForestFireRisk(DisasterAccidentDTO dto); // 예보 데이터 저장
	List<DisasterAccidentDTO> selectForestFireRiskList(); // 예보 데이터 조회
	
	// 기상특보 (ex. 호우특보, 한파특보, 태풍특보)
	int insertKmaWeather(DisasterAccidentDTO dto);
	List<DisasterAccidentDTO> selectKmaWeatherByType(int type);
	
	// 지진
	int insertEarthquake(DisasterAccidentDTO dto);
	List<DisasterAccidentDTO> selectEarthquakeList();
//	int insertEarthquakeBatch(List<DisasterAccidentDTO> earthquakeList);
}
