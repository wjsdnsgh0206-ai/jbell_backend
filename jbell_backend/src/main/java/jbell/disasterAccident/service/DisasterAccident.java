package jbell.disasterAccident.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import jbell.disasterAccident.dto.DisasterAccidentDTO;
import reactor.core.publisher.Mono;

@Transactional
public interface DisasterAccident {
	
	public Mono<Void> fetchAndSaveForestFireRisk();
	public Mono<Void> fetchAndSaveEarthquake(String startDate);
    Mono<Void> fetchAndSaveTyphoon(String year);
    Mono<Void> fetchAndSaveLandslide();
    Mono<Void> fetchAndSaveWeatherWarning(String warningType); // 기상특보 (ex. 호우특보, 한파특보, 태풍특보)
    List<DisasterAccidentDTO> getWeatherListByType(int type); // 이거 추가!
}