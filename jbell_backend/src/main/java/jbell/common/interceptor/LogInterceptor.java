package jbell.common.interceptor;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.StringJoiner;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LogInterceptor implements HandlerInterceptor{

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// Get /api /board?no=1&type=free
		Set<String> paramKey = request.getParameterMap().keySet();
		
		var param = new StringJoiner(", ");
		
		// no=1, type=free
		for(String key : paramKey) {
			param.add(key + ": " + request.getParameter(key));
		}
		
		String requestBody = null;
		if(request instanceof ContentCachingRequestWrapper) {
			
			ContentCachingRequestWrapper wrapperRequest = (ContentCachingRequestWrapper) request;
			
			byte[] content = wrapperRequest.getContentAsByteArray();
			
			if(content.length > 0) {
				try {
					requestBody = new String(content, StandardCharsets.UTF_8);
				}catch (Exception e) {
					log.error("Faild to log request body", e);
				}
			}
		}
		
		log.info("============================Access log Start==============================");
		log.info("PORT          ::::     {}", request.getLocalPort());
        log.info("SERVERNAME    ::::     {}", request.getServerName());
        log.info("HTTP METHOD   ::::     {}", request.getMethod());
        log.info("URI           ::::     {}", request.getRequestURI());
        log.info("CLIENT IP     ::::     {}", request.getRemoteAddr());
		if(param.length() >0) {
			log.info("PARAMETER    ::::    {}", param);
		}
		if(requestBody!= null) {
			log.info("REQUEST BODY    ::::    {}", requestBody.replaceAll("\\s+", ""));
		}
		log.info("============================Access log End==============================");
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
}







