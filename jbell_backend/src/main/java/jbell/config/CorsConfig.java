package jbell.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
	
	private static final List<String> AllOW_ORIGINS = List.of(
			"http://localhost:5173"
			,"https://localhost:5173"
			,"http://jbell.cloud"
			,"https://jbell.cloud"
	);
	
	
	private static final List<String> AllOW_METHOD = List.of(
			"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
	);
			
	
	
	@Bean
	@Primary
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedOrigins(AllOW_ORIGINS);
		corsConfiguration.setAllowedMethods(AllOW_METHOD);
		corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
		corsConfiguration.setAllowCredentials(true);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration);
		
		return source;
	}
}
