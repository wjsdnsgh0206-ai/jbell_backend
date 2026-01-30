package jbell.disasterAccident.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import jbell.disasterAccident.dto.DisasterAccidentDTO;
import reactor.core.publisher.Mono;

@Transactional
public interface DisasterAccident {
	
	public Mono<Void> fetchAndSaveForestFireRisk();
	List<DisasterAccidentDTO> getForestFireList(); // [추가] 산불 목록 조회 서비스
	
	// 📌 [신규 추가] 산불 위험 예보 정보 (API 수집 및 조회ㅁ
    
	// 기존 산불 발생 정보와 별개로 아래 두 개 확인
    public Mono<Void> fetchAndSaveForestFireRiskData(); // 수집
    List<DisasterAccidentDTO> getForestFireRiskList();   // 조회
    
	// 지진
	public Mono<Void> fetchAndSaveEarthquake(String startDate);
	List<DisasterAccidentDTO> getEarthquakeList(); // 지진 목록 조회
	
    Mono<Void> fetchAndSaveTyphoon(String year);
    
    // 산사태
    Mono<Void> fetchAndSaveLandslide();
    List<DisasterAccidentDTO> getLandslideList();
    
    Mono<Void> fetchAndSaveWeatherWarning(String warningType); // 기상특보 (ex. 호우특보, 한파특보, 태풍특보)
    List<DisasterAccidentDTO> getWeatherListByType(int type); // 이거 추가!
}