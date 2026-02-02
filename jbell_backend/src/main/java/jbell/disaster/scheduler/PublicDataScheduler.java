package jbell.disaster.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jbell.disaster.dto.DisasterExternApiRequest;
import jbell.externapi.service.PublicDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicDataScheduler {

    private final PublicDataService publicDataService;

    /**
     * 테스트를 위해 1분(60000ms) 단위로 실행
     */
    @Scheduled(fixedDelay = 60000) 
    public void collectDisasterMessages() {
        log.info("▶ [스케줄러] 1분 단위 재난 문자 자동 수집 시작");

        DisasterExternApiRequest request = new DisasterExternApiRequest();
        request.setPageNo(1);
        request.setNumOfRows(10); // 테스트니까 10개씩만 가져와보자
        request.setRgnNm("전북");

        publicDataService.getAndSaveDisasterMessages(request)
            .subscribe(
                result -> {
                    int count = (result.getBody() != null) ? result.getBody().size() : 0;
                    log.info("▷ [스케줄러] 수집 완료: {}건 처리됨", count);
                },
                error -> log.error("▷ [스케줄러] 에러: {}", error.getMessage())
            );
    }

    // 2. 기상특보 수집 (매 30분마다 - 필요하다면 추가!)
    @Scheduled(cron = "0 30 * * * *")
    public void collectWeatherWarnings() {
        log.info("▶ [스케줄러] 기상 특보 자동 수집 시작");
        DisasterExternApiRequest request = new DisasterExternApiRequest();
        request.setPageNo(1);
        request.setNumOfRows(50);
        // 날짜 계산 로직이 필요할 수 있음
        publicDataService.getAndSaveWeatherWarnings(request).subscribe();
    }
}