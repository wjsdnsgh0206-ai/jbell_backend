package jbell;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; 

@EnableScheduling // 매시 5분주기 api 호출하기 위한 어노테이션 추가.
@MapperScan("jbell.**.mapper")
@SpringBootApplication
public class JbellBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JbellBackendApplication.class, args);
	}

}