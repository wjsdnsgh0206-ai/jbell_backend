package jbell.disasterAccident.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import jbell.disasterAccident.dto.DisasterAccidentDTO;
import reactor.core.publisher.Mono;

@Transactional
public interface DisasterAccident {
	
	// 산불
	public Mono<Void> fetchAndSaveForestFireRisk();
	List<DisasterAccidentDTO> getForestFireList(); // 산불 목록 조회 서비스
	
	// 산불 위험 지수
    public Mono<Void> fetchAndSaveForestFireRiskData(); // 수집
    List<DisasterAccidentDTO> getForestFireRiskList();   // 조회
    
	// 지진
	public Mono<Void> fetchAndSaveEarthquake(String startDate);
	List<DisasterAccidentDTO> getEarthquakeList(); // 지진 목록 조회
	
    // 태풍
	Mono<Void> fetchAndSaveTyphoon(String year);
    List<DisasterAccidentDTO> getTyphoonList();
    
    // 태풍
    

    // 호우홍수
    
    
    // 산사태
    Mono<Void> fetchAndSaveLandslide();
    List<DisasterAccidentDTO> getLandslideList();
    
    // 한파
    Mono<Void> fetchAndSaveWeatherWarning(String warningType); // 기상특보 (ex. 호우특보, 한파특보, 태풍특보)
    List<DisasterAccidentDTO> getWeatherListByType(int type); // 이거 추가!
    
    
    // 댐 및 하천 수위 수집 및 조회
    Mono<Void> fetchAndSaveWaterLevel(String obscd);
    List<DisasterAccidentDTO> getWaterLevelList();
    
    // 재난 발생 관리 상태 변경
    Mono<Void> updateDisasterStatus(DisasterAccidentDTO disasterAccidentDTO);
    
}