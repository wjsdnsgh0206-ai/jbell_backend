package jbell.facility.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jbell.facility.service.FacilityService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/facility")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService sheltersService;
    
    // 30개씩 조회 하고 검색된 총 개수 카운트, 정렬 기능 조건 검색기능
    @GetMapping("/list")
    public Mono<Map<String, Object>> getList(
            @RequestParam(value = "ctpvNm", required = false) String ctpvNm,
            @RequestParam(value = "sggNm", required = false) String sggNm,
            @RequestParam(value = "fcltNm", required = false) String fcltNm,
            @RequestParam(value = "roadNmAddr", required = false) String roadNmAddr, // 추가
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortKey", defaultValue = "reg_dt") String sortKey,
            @RequestParam(value = "sortOrder", defaultValue = "DESC") String sortOrder) {
        
        // 서비스 호출 시 roadNmAddr 파라미터를 포함하여 전달합니다.
        return sheltersService.getFacilityListData(ctpvNm, sggNm, fcltNm, roadNmAddr, page, sortKey, sortOrder);
    }
    
    // 업데이트 및 등록
    @GetMapping("/sync")
    public Mono<String> sync() {
        // 작업을 시작만 시키고 바로 응답을 보냄
        return Mono.fromRunnable(() -> sheltersService.syncAllFacility())
                   .subscribeOn(Schedulers.boundedElastic())
                   .thenReturn("동기화 작업이 백그라운드에서 시작되었습니다. 결과는 서버 로그를 확인하세요.")
                   .onErrorReturn("동기화 요청 중 오류가 발생했습니다.");
    }
}
