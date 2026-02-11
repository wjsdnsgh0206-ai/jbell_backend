package jbell.disasterAccident.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.disasterAccident.dto.DisasterAccidentDTO;

@Mapper
public interface DisasterAccidentMapper {


	// 태풍
	int insertTyphoonInfo(DisasterAccidentDTO dto);
	List<DisasterAccidentDTO> selectTyphoonList();
	
	
	

	int insertTyphoonTrack(DisasterAccidentDTO dto);

	// 산사태
	int insertLandslide(DisasterAccidentDTO dto);
	List<DisasterAccidentDTO> selectLandslideList(); // 산사태 조회
	
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
	
	
	// 댐 및 하천 수위
    int insertWaterLevel(DisasterAccidentDTO dto);
    List<DisasterAccidentDTO> selectWaterLevelList();

    // 상태 변경 메서드
    int updateForestFireStatus(@Param("id") Long id, @Param("status") String status);
    int updateEarthquakeStatus(@Param("seq") Long seq, @Param("status") String status);
	// 파라미터가 id 하나로 단순해집니다.
    int updateKmaWeatherStatus(@Param("id") Long id, @Param("status") String status);
}
