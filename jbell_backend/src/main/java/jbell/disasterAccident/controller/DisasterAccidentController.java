package jbell.disasterAccident.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jbell.common.response.ApiResponse;
import jbell.disasterAccident.dto.DisasterAccidentDTO;
import jbell.disasterAccident.service.DisasterAccident;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/disaster/fetch")
@RequiredArgsConstructor
public class DisasterAccidentController {

	private final DisasterAccident disasterAccidentService;

	// 1. 산불 정보 수집 (날짜 기반)
	@PostMapping("/forest-fire")
	public Mono<Void> fetchForestFireRisk() {
		return disasterAccidentService.fetchAndSaveForestFireRisk();
	}

	// 2. 지진 정보 수집
	@PostMapping("/earthquake")
	public Mono<Void> fetchEarthquake(@RequestParam(required = false) String startDate) {
		// startDate 예: "202601010000"
		return disasterAccidentService.fetchAndSaveEarthquake(startDate);
	}

	// 3. 태풍 정보 수집 (연도 기반)
	@PostMapping("/typhoon")
	public Mono<ApiResponse<String>> fetchTyphoon(@RequestParam("year") String year) {
		return disasterAccidentService.fetchAndSaveTyphoon(year)
				.then(Mono.just(ApiResponse.success(year + "년 태풍 데이터 수집 완료")));
	}

	// 4. 산사태 정보 수집
	@PostMapping("/landslide")
	public Mono<ApiResponse<String>> fetchLandslide() {
		return disasterAccidentService.fetchAndSaveLandslide().then(Mono.just(ApiResponse.success("산사태 예보 데이터 수집 완료")));
	}

	// 5. 기상특보(ex. 호우특보, 태풍특보) 정보 수집
	@PostMapping("/weather-warning")
	public Mono<ApiResponse<String>> fetchWeatherWarning(@RequestParam("type") String type) {
		return disasterAccidentService.fetchAndSaveWeatherWarning(type)
				.then(Mono.just(ApiResponse.success(type + " 타입 특보 데이터 수집 완료")));
	}

	// 5-1. 외부 API 호출 및 DB 저장 (수집용)
	@GetMapping("/weather-warning")
	public Mono<ApiResponse<String>> fetchWeatherWarningGet(@RequestParam("type") String type) {
		return disasterAccidentService.fetchAndSaveWeatherWarning(type)
				.then(Mono.just(ApiResponse.success(type + " 타입 특보 데이터 수집 완료")));
	}

	// 5-2. DB 데이터 조회 (사용자 화면용)
	@GetMapping("/weather-list")
	public ApiResponse<List<DisasterAccidentDTO>> getWeatherList(@RequestParam("type") int type) {
		List<DisasterAccidentDTO> list = disasterAccidentService.getWeatherListByType(type);
		return ApiResponse.success(list);
	}
	


}
