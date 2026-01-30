package jbell.config; // 프로젝트 패키지 구조에 맞게 설정하세요.

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync // 여기서 비동기 기능을 활성화합니다.
public class AsyncConfig {
    // 나중에 스레드 풀(Thread Pool) 설정이 필요하면 여기에 추가합니다.
}