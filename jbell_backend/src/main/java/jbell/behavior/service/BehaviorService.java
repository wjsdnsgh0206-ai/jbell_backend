package jbell.behavior.service;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jbell.behavior.dto.BehaviorMethod;
import jbell.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;


@Slf4j
@Service
public class BehaviorService {
	
	private final ObjectMapper objectMapper;
	
	// 재난안전데이터공유플랫폼
	@Value("${safetydata.servicekey}")
    private String safetyDataServiceKey;
	
	@Value("${external.api.timeout}")
	private int timeout;
	
	private final WebClient safetyDataWebClient;
	
	public BehaviorService(@Qualifier("safetyDataWebClient") WebClient safetyDataWebClient
						   ,@Qualifier("objectMapper") ObjectMapper objectMapper) {
		this.safetyDataWebClient = safetyDataWebClient;
		this.objectMapper = objectMapper;
	}
	
	public void getBehaviorInfo(String category) {
		log.info("{}", category);
		log.info("{}", safetyDataServiceKey);
		
		// 행동요령-자연재난-태풍
	    safetyDataWebClient.get()
		            		.uri(uriBuilder -> uriBuilder
		            				.path("/DSSP-IF-20588")
		            				.queryParam("serviceKey", safetyDataServiceKey)
		            				.queryParam("pageNo", 1)
		            				.queryParam("numOfRows", 10)
		            				.queryParam("safety_cate", category)
		            				.build())
		            		.retrieve()
		            		.bodyToMono(JsonNode.class)
		            		.timeout(Duration.ofMillis(timeout))
		            		.doOnSuccess(firstResponse -> {
		            			log.info("Successfully fetched page {}", firstResponse);
		            			JsonNode body = firstResponse.get("body");
		            			var list = objectMapper.convertValue(
			            					body, 
			            					new TypeReference<List<BehaviorMethod>>() {}
            					);
		            			log.info("{}", list);
		            			/*
		            			List<JsonNode> list = StreamSupport.stream(body.spliterator(), false)
		            						 					   .collect(Collectors.toList());
		            			list.forEach(json -> log.info("{}", json));
		            			*/
		            		})
		            		.doOnError(error -> log.error("Error fetching", error.getMessage()))
		            		.onErrorResume(CustomException.class, e -> {
							    log.error("WebClient error : Status={}",  e.getMessage());
		            			return Mono.empty();
		            		})
		            		.retryWhen(Retry.backoff(2, Duration.ofSeconds(2))  // 재시도 횟수 축소 (3->2)
		            				.maxBackoff(Duration.ofSeconds(5))
		            				.doBeforeRetry(retrySignal -> log.warn("Retrying - attempt {}", retrySignal.totalRetries() + 1)))
		            		.subscribe();
	    
	}
}
