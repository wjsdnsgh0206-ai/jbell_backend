package jbell.externapi.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jbell.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class IntegrationService {
	
	// 공공데이터
	@Value("${publicdata.servicekey}")
	private String publicDataServiceKey;
	
	// 재난안전데이터공유플랫폼
	@Value("${safetydata.servicekey}")
	private String safetyDataServiceKey;
	
	// openweather
	@Value("${openweather.servicekey}")
	private String openWeatherServiceKey;
	
	// 기상청API 허브
	@Value("${apihubdata.servicekey}")
	private String apihuDataServiceKey;
	
	@Value("${external.api.timeout}")
	private int timeout;

	private final WebClient publicDataWebClient;
	private final WebClient safetyDataWebClient;
	private final WebClient weatherDataWebClient;
	private final WebClient apihubDataWebClient;
	private final ObjectMapper objectMapper;
	
	
	public IntegrationService(  @Qualifier("publicDataWebClient") 	WebClient publicDataWebClient
							   ,@Qualifier("safetyDataWebClient") 	WebClient safetyDataWebClient
							   ,@Qualifier("weatherDataWebClient") 	WebClient weatherDataWebClient
							   ,@Qualifier("apihubDataWebClient") 	WebClient apihubDataWebClient
							   ,@Qualifier("objectMapper") 	ObjectMapper objectMapper
							  ) {
		this.publicDataWebClient 	= publicDataWebClient;
		this.safetyDataWebClient 	= safetyDataWebClient;
		this.weatherDataWebClient 	= weatherDataWebClient;
		this.apihubDataWebClient 	= apihubDataWebClient;
		this.objectMapper = objectMapper;
	}
	
    /**
     * 통합현황 데이터 조회
     * 여러 API 응답을 JsonNode로 합치기
     */
    public Mono<JsonNode> getIntegrationInfo(JsonNode request) {
        	log.info("request:{}", request);
        	String weatherLat = request.get("weather").get("lat").asText();
        	String weatherLon = request.get("weather").get("lon").asText();
        	String weatherUnits = request.get("weather").get("units").asText();
        	String weatherLang = request.get("weather").get("lang").asText();
        	
        	String earthquakeOrderTy = request.get("earthquake").get("orderTy").asText();
        	String earthquakeEqArCD = request.get("earthquake").get("eqArCd").asText();
        	//날씨 정보
            Mono<JsonNode> weatherInfo = weatherDataWebClient.get()
												 .uri(uriBuilder -> uriBuilder
												        .path("/weather")
												        .queryParam("appid", openWeatherServiceKey)
												        .queryParam("units", weatherUnits)
												        .queryParam("lat", weatherLat)
												        .queryParam("lon", weatherLon)
												        .queryParam("lang", weatherLang)
												        .build())
												 .retrieve()
												 .bodyToMono(JsonNode.class)
												 .timeout(Duration.ofMillis(timeout))
												 .doOnSuccess(firstResponse -> log.info("Successfully fetched page {}", firstResponse))
												 .doOnError(error -> log.error("Error fetching", error.getMessage()))
												 .onErrorResume(CustomException.class, e -> {
												    log.error("WebClient error : Status={}",  e.getMessage());
												    return Mono.empty();
												 })
												 .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))  // 재시도 횟수 축소 (3->2)
														         .maxBackoff(Duration.ofSeconds(5))
														         .doBeforeRetry(retrySignal -> log.warn("Retrying - attempt {}", retrySignal.totalRetries() + 1)));
            
            //지진특보
            Mono<JsonNode> earthquakeInfo = apihubDataWebClient.get()
							            		.uri(uriBuilder -> uriBuilder
							            				.path("/typ09/url/eqk/urlNewNotiEqk.do")
							            				.queryParam("orderTy", earthquakeOrderTy)
							            				.queryParam("eqArCd", earthquakeEqArCD)
							            				.queryParam("authKey", apihuDataServiceKey)
							            				.build())
							            		.retrieve()
							            		.bodyToMono(JsonNode.class)
							            		.timeout(Duration.ofMillis(timeout))
							            		.doOnSuccess(firstResponse -> log.info("Successfully fetched page {}", firstResponse))
							            		.doOnError(error -> log.error("Error fetching", error.getMessage()))
							            		.onErrorResume(CustomException.class, e -> {
												    log.error("WebClient error : Status={}",  e.getMessage());
							            			return Mono.empty();
							            		})
							            		.retryWhen(Retry.backoff(2, Duration.ofSeconds(2))  // 재시도 횟수 축소 (3->2)
							            				.maxBackoff(Duration.ofSeconds(5))
							            				.doBeforeRetry(retrySignal -> log.warn("Retrying - attempt {}", retrySignal.totalRetries() + 1)));
            
            //진도정보
            Mono<JsonNode> earthquakeMagnitudeInfo = safetyDataWebClient.get()
							            		.uri(uriBuilder -> uriBuilder
							            				.path("/DSSP-IF-00109")
							            				.queryParam("serviceKey", safetyDataServiceKey)
							            				.build())
							            		.retrieve()
							            		.bodyToMono(JsonNode.class)
							            		.timeout(Duration.ofMillis(timeout))
							            		.doOnSuccess(firstResponse -> log.info("Successfully fetched page {}", firstResponse))
							            		.doOnError(error -> log.error("Error fetching", error.getMessage()))
							            		.onErrorResume(CustomException.class, e -> {
												    log.error("WebClient error : Status={}",  e.getMessage());
							            			return Mono.empty();
							            		})
							            		.retryWhen(Retry.backoff(2, Duration.ofSeconds(2))  // 재시도 횟수 축소 (3->2)
							            				.maxBackoff(Duration.ofSeconds(5))
							            				.doBeforeRetry(retrySignal -> log.warn("Retrying - attempt {}", retrySignal.totalRetries() + 1)));
            
            
            return Mono.zip(weatherInfo, earthquakeInfo, earthquakeMagnitudeInfo)
                    .map(tuple -> {
                        ObjectNode combined = objectMapper.createObjectNode();
                        combined.set("weatherInfo", tuple.getT1());
                        combined.set("earthquakeInfo", tuple.getT2());
                        combined.set("earthquakeMagnitudeInfo", tuple.getT3());
                        return combined;
                    });
      }
}
