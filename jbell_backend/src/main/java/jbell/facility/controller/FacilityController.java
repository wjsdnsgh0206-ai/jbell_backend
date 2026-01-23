package jbell.facility.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jbell.common.response.ApiResponse;
import jbell.exception.ErrorCode;
import jbell.facility.dto.FacilityDTO;
import jbell.facility.dto.FacilityListRequest;
import jbell.facility.dto.FacilityListResponse;
import jbell.facility.service.FacilityService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/facility")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    /**
     * 1. 안전 시설물 목록 조회
     * (위도/경도/수용인원이 포함된 FacilityDTO 리스트를 반환합니다.)
     */
    @GetMapping("/list")
    public Mono<ApiResponse<FacilityListResponse>> getList(@Valid FacilityListRequest request) {
        return facilityService.getFacilityListData(request)
                .map(ApiResponse::success)
                .onErrorResume(e -> Mono.just(ApiResponse.error(
                        ErrorCode.EXTERNAL_API_ERROR.code(),
                        ErrorCode.EXTERNAL_API_ERROR.message()
                )))
                .defaultIfEmpty(ApiResponse.error(
                        ErrorCode.NOT_FOUND.code(),
                        ErrorCode.NOT_FOUND.message()
                ));
    }

    /**
     * 2. 상세 조회
     */
    @GetMapping("/{fcltId}")
    public Mono<ApiResponse<FacilityDTO>> getDetail(@PathVariable("fcltId") Long fcltId) { // ("fcltId") 추가
        return facilityService.getFacilityDetail(fcltId)
                .map(ApiResponse::success)
                .defaultIfEmpty(ApiResponse.error(404, "해당 시설 정보를 찾을 수 없습니다."));
    }

    /**
     * 3. 시설 신규 등록 (lat, lot, fcltCapacity 포함)
     */
    @PostMapping("/add")
    public Mono<ApiResponse<Void>> addFacility(@RequestBody FacilityDTO facilityDTO) {
        return facilityService.insertFacility(facilityDTO)
                .then(Mono.just(ApiResponse.success(null))); // 인자를 하나만 전달
    }

    /**
     * 4. 시설 정보 수정
     */
    @PutMapping("/update")
    public Mono<ApiResponse<Void>> updateFacility(@RequestBody FacilityDTO facilityDTO) {
        return facilityService.updateFacility(facilityDTO)
                .then(Mono.just(ApiResponse.success(null)));
    }

    /**
     * 5. 시설 삭제 (일괄 삭제 지원)
     */
    @DeleteMapping("/delete")
    public Mono<ApiResponse<Void>> deleteFacilities(@RequestBody List<Long> fcltIds) {
        return facilityService.deleteFacilities(fcltIds)
                .then(Mono.just(ApiResponse.success(null)));
    }

    /**
     * 6. 외부 API 데이터 동기화
     * (수정사항: 반환 타입을 ApiResponse<String>으로 변경하여 프론트 응답 규격을 통일)
     */
    @GetMapping("/sync")
    public Mono<ApiResponse<String>> sync() {
        return Mono.fromRunnable(() -> facilityService.syncAllFacility())
                   .subscribeOn(Schedulers.boundedElastic())
                   .thenReturn(ApiResponse.success("동기화 작업이 백그라운드에서 시작되었습니다. 결과는 서버 로그를 확인하세요."))
                   .onErrorReturn(ApiResponse.error(500, "동기화 요청 중 서버 오류가 발생했습니다."));
    }
}