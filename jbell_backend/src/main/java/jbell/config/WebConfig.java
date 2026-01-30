package jbell.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import jbell.common.interceptor.LogInterceptor;
import lombok.RequiredArgsConstructor;
@Configuration
@RequiredArgsConstructor
public class WebConfig  implements WebMvcConfigurer{
	
	private final LogInterceptor logInterceptor;
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	@Value("${file.path}")
	private String fileRealPath;
	

		
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(logInterceptor)
				.addPathPatterns("/**")
				.excludePathPatterns("/favicon.*")
				.excludePathPatterns("/error");
		
		WebMvcConfigurer.super.addInterceptors(registry);
	}
	
	
	/**
	 * 주소요청에 따른 외부정적파일 경로 설정
	 * 정적 리소스(이미지 등) 매핑 설정
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 브라우저에서 /uploads/ 로 시작하는 모든 요청을 가로채서
		registry.addResourceHandler("/uploads/**")
				// 실제 로컬 컴퓨터의 uploadDir 폴더 내 파일로 연결한다.
				.addResourceLocations("file:///" + uploadDir);
		
		String rootPath = getOSFilePath();
				
		registry.addResourceHandler("/api/attachment/**")
				.addResourceLocations(rootPath + fileRealPath + "/attachment/")
				.setCachePeriod(3600)
				.resourceChain(true)
				.addResolver(new PathResourceResolver());
		
		WebMvcConfigurer.super.addResourceHandlers(registry);
	}
	

	/**
	 * 운영체제별 경로 설정
	 * @return
	 */
	public String getOSFilePath() {
		String rootPath = "file:///";
		String os = System.getProperty("os.name").toLowerCase();
		
		if(os.contains("win")) {
			rootPath = "file:///c:";
		}else if(os.contains("linux")) {
			rootPath = "file://";
		}else if(os.contains("mac")) {			
			rootPath = "file://";
		}
		
		
		return rootPath;
	}

}
