package jbell.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jbell.common.interceptor.LogInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer{
	
	private final LogInterceptor logInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(logInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns("/favicon.*")
				.excludePathPatterns("/error");
		
		WebMvcConfigurer.super.addInterceptors(registry);
	}
}
