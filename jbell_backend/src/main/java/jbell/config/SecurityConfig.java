package jbell.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private static String[] RERMIT_REQUEST_URI = {
		 "/api/**"
		,"/oauth2/login"
	};
	
	private final CorsConfigurationSource corsConfigurationSource;
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// csrf, formLogin, httpBasic 비활성화
			.csrf(csrf -> csrf.disable())
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			// 세션비활성화
			.sessionManagement(session -> session
											.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			// CORS 설정
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			// 주소
			.authorizeHttpRequests(auth -> auth
											.requestMatchers(RERMIT_REQUEST_URI).permitAll()
											.anyRequest().authenticated()
			);
		return http.build();
			
	}
	/**
	 * 정적 리소스 제외 설정
	 * 설정된 주소는 Security Filter를 거치지 않는다.
	 */
	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()
						   .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
						   .requestMatchers("/favicon.*", "/resources/**", "/error");
	}
}














