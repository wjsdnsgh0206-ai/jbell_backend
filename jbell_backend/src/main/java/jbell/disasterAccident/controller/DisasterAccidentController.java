package jbell.disasterAccident.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jbell.common.response.ApiResponse;
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
        return disasterAccidentService.fetchAndSaveLandslide()
                .then(Mono.just(ApiResponse.success("산사태 예보 데이터 수집 완료")));
    }
}