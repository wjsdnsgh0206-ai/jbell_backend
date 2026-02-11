package jbell.disaster.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    // 재난문자 5분단위 스케줄링 
    @Scheduled(cron = "0 0/5 * * * *")
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

    // 기상특보 5분단위 스케줄링
    @Scheduled(cron = "0 0/5 * * * *")
    public void collectWeatherWarnings() {
    	// 1주일 전 날짜
    	LocalDate daysAgo = LocalDate.now().minusDays(7);
    	DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String inqDt = daysAgo.format(customFormatter);
        
        log.info("▶ [스케줄러] 기상 특보 자동 수집 시작");
        DisasterExternApiRequest request = new DisasterExternApiRequest();
        request.setPageNo(1);
        request.setNumOfRows(50);
        request.setInqDt(inqDt);
        // 날짜 계산 로직이 필요할 수 있음
        publicDataService.getAndSaveWeatherWarnings(request).subscribe();
    }
}