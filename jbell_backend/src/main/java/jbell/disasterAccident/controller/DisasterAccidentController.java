package jbell.disasterAccident.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	@GetMapping("/forest-fire-list")
	public ApiResponse<List<DisasterAccidentDTO>> getForestFireList() {
	    // DB에서 목록을 가져옴
	    List<DisasterAccidentDTO> list = disasterAccidentService.getForestFireList();
	    
	    // ApiResponse 형식을 맞춰서 리턴 (그래야 프론트에서 200 OK로 인식함)
	    return ApiResponse.success(list);
	}
	
	
	// 2 산불 위험 예보 데이터 수집 (새로 만든 예보 전용 테이블로 저장)
	// [POST] 관리자 페이지: "수집하기" 버튼 클릭 시 실행
		// [POST] 관리자 페이지: "수집하기" 버튼 클릭 시 실행
		@PostMapping("/forest-fire-risk/fetch")
		public Mono<ApiResponse<String>> fetchForestFireRiskData() { // 중복 방지를 위해 이름 변경!
			return disasterAccidentService.fetchAndSaveForestFireRiskData()
					.then(Mono.just(ApiResponse.success("최신 산불 예보 데이터가 DB에 저장되었습니다.")));
		}

		// [GET] 사용자/관리자 화면: DB에 저장된 데이터 조회
		@GetMapping("/forest-fire-risk-list")
		public ApiResponse<List<DisasterAccidentDTO>> getForestFireRiskList() {
			List<DisasterAccidentDTO> list = disasterAccidentService.getForestFireRiskList();
			return ApiResponse.success(list);
		}
		
		
	// 3. 지진 정보 수집
	@PostMapping("/earthquake")
	// 📌 아래처럼 @RequestParam 안에 이름을 확실하게 명시해줘!
	public Mono<Void> fetchEarthquake(@RequestParam(value = "startDate", required = false) String startDate) {
	    return disasterAccidentService.fetchAndSaveEarthquake(startDate);
	}
	// 3-1. 지진 정보 조회 (최근 한 달 데이터)
	@GetMapping("/earthquake-list")
	public ApiResponse<List<DisasterAccidentDTO>> getEarthquakeList() {
	    List<DisasterAccidentDTO> list = disasterAccidentService.getEarthquakeList();
	    return ApiResponse.success(list);
	}

	// 4. 태풍 정보 수집 (연도 기반) - 관리자용
	@PostMapping("/typhoon")
	public Mono<ApiResponse<String>> fetchTyphoon(@RequestParam("year") String year) {
	    return disasterAccidentService.fetchAndSaveTyphoon(year)
	            .then(Mono.just(ApiResponse.success(year + "년 태풍 데이터 수집 완료")));
	}

	// 4-1. 태풍 정보 조회 (사용자 화면용)
	@GetMapping("/typhoon-list")
	public ApiResponse<List<DisasterAccidentDTO>> getTyphoonList() {
	    List<DisasterAccidentDTO> list = disasterAccidentService.getTyphoonList();
	    return ApiResponse.success(list);
	}

	// 5. 산사태 정보 수집 (관리자용)
	@PostMapping("/landslide")
	public Mono<ApiResponse<String>> fetchLandslide() {
	    return disasterAccidentService.fetchAndSaveLandslide()
	            .then(Mono.just(ApiResponse.success("산사태 예보 데이터 수집 및 DB 저장 완료")));
	}

	// 5-1. 산사태 정보 조회 (사용자/관리자 화면용)
	@GetMapping("/landslide-list")
	public ApiResponse<List<DisasterAccidentDTO>> getLandslideList() {
	    List<DisasterAccidentDTO> list = disasterAccidentService.getLandslideList();
	    return ApiResponse.success(list);
	}

	// 6. 기상특보(ex. 호우특보, 태풍특보) 정보 수집
	@PostMapping("/weather-warning")
	public Mono<ApiResponse<String>> fetchWeatherWarning(@RequestParam("type") String type) {
		return disasterAccidentService.fetchAndSaveWeatherWarning(type)
				.then(Mono.just(ApiResponse.success(type + " 타입 특보 데이터 수집 완료")));
	}

	// 6-1. 외부 API 호출 및 DB 저장 (수집용)
	@GetMapping("/weather-warning")
	public Mono<ApiResponse<String>> fetchWeatherWarningGet(@RequestParam("type") String type) {
		return disasterAccidentService.fetchAndSaveWeatherWarning(type)
				.then(Mono.just(ApiResponse.success(type + " 타입 특보 데이터 수집 완료")));
	}

	// 6-2. DB 데이터 조회 (사용자 화면용)
	@GetMapping("/weather-list")
	public ApiResponse<List<DisasterAccidentDTO>> getWeatherList(@RequestParam("type") int type) {
		List<DisasterAccidentDTO> list = disasterAccidentService.getWeatherListByType(type);
		return ApiResponse.success(list);
	}
	
	// 7. 댐 및 하천 수위 정보 수집 (WAMIS API)
    // [POST] 관리자: 특정 관측소의 데이터를 DB에 동기화
    @PostMapping("/water-level/fetch")
    public Mono<ApiResponse<String>> fetchWaterLevel(@RequestParam(value = "obscd", defaultValue = "4001605") String obscd) {
        return disasterAccidentService.fetchAndSaveWaterLevel(obscd)
                .then(Mono.just(ApiResponse.success("관측소(" + obscd + ") 수위 데이터 수집 완료")));
    }

    
    
    // 7-1. 댐 및 하천 수위 정보 조회 (사용자 화면용)
    // [GET] DB에 저장된 최근 수위 데이터를 리스트로 반환
    @GetMapping("/water-level-list")
    public ApiResponse<List<DisasterAccidentDTO>> getWaterLevelList() {
        List<DisasterAccidentDTO> list = disasterAccidentService.getWaterLevelList();
        return ApiResponse.success(list);
    }
    
    // [추가] 재난 노출 상태 일괄 변경
    @PostMapping("/manage/status") // fetch가 아닌 관리 기능이므로 manage 경로 권장
    public Mono<ApiResponse<String>> updateStatus(@RequestBody DisasterAccidentDTO disasterAccidentDTO) {
        return disasterAccidentService.updateDisasterStatus(disasterAccidentDTO)
                .then(Mono.just(ApiResponse.success("상태가 성공적으로 변경되었습니다.")));
    }

}
