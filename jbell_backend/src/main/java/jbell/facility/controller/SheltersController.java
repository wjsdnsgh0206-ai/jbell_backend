package jbell.facility.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jbell.facility.service.SheltersService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/shelters")
@RequiredArgsConstructor
public class SheltersController {

    private final SheltersService sheltersService;

    @GetMapping("/sync")
    public Mono<String> sync() {
        // 작업을 시작만 시키고 바로 응답을 보냄
        return Mono.fromRunnable(() -> sheltersService.syncAllShelters())
                   .subscribeOn(Schedulers.boundedElastic())
                   .thenReturn("동기화 작업이 백그라운드에서 시작되었습니다. 결과는 서버 로그를 확인하세요.")
                   .onErrorReturn("동기화 요청 중 오류가 발생했습니다.");
    }
}
