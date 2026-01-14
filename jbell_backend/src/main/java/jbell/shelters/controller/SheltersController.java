package jbell.shelters.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jbell.shelters.service.SheltersService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shelters")
@RequiredArgsConstructor
public class SheltersController {

    private final SheltersService sheltersService;

    @GetMapping("/sync")
    public String sync() {
        // 대용량 작업이므로 비동기 처리를 권장하지만, 일단 기본 실행 형태입니다.
        sheltersService.syncAllShelters();
        return "동기화 프로세스가 시작되었습니다. 로그를 확인하세요.";
    }
}
