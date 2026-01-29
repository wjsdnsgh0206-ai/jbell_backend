package jbell.disasterAccident.service;

import reactor.core.publisher.Mono;

public interface DisasterAccident {
	
	public Mono<Void> fetchAndSaveForestFireRisk();
	public Mono<Void> fetchAndSaveEarthquake(String startDate);
    Mono<Void> fetchAndSaveTyphoon(String year);
    Mono<Void> fetchAndSaveLandslide();
}