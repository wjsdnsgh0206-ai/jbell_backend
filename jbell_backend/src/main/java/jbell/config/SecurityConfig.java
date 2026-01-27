package jbell.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import jbell.security.jwt.JwtAuthenticationFilter;
import jbell.security.jwt.JwtTokenProvider;
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
	private final JwtTokenProvider jwtTokenProvider;
	
	// 비밀번호를 암호화하고 비교할 때 사용할 도구 등록
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .cors(cors -> cors.configurationSource(corsConfigurationSource))
	        .authorizeHttpRequests(auth -> auth
	        		.requestMatchers("/api/facility/sync").permitAll() // ★ 이 줄 추가
	                .requestMatchers("/api/facility/list").permitAll()
	            // 1. 관리자 전용 API (반드시 ROLE_ 접두사를 제외한 등급명 작성)
	            .requestMatchers("/api/admin/**").hasRole("ADMIN") 
	            // 2. 로그인, 회원가입 등 인증이 필요 없는 경로
	            .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
	            // 3. 나머지는 로그인(인증)만 되어 있으면 허용
	            .anyRequest().authenticated()
	        )
	        .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
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














