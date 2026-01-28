package jbell;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("jbell.**.mapper")
@SpringBootApplication
public class JbellBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JbellBackendApplication.class, args);
	}

}
