package jbell.externapi.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jbell.disaster.dto.DisasterExternApiRequest;
import jbell.disaster.dto.PredictionInfoResponse;
import jbell.exception.CustomException;
import jbell.exception.ErrorCode;
import jbell.externapi.dto.PublicDataResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class PublicDataService {
	
	@Value("${publicdata.servicekey}")
	private String serviceKey;
	
	@Value("${external.api.timeout}")
	private int timeout;

	private final WebClient publicDataWebClient;
    private final ObjectMapper objectMapper;    
    private final XmlMapper xmlMapper;
	
	
	public PublicDataService(@Qualifier("publicDataWebClient") WebClient publicDataWebClient
							,@Qualifier("objectMapper") ObjectMapper objectMapper
							,@Qualifier("xmlMapper") XmlMapper xmlMapper
							) {
		this.publicDataWebClient = publicDataWebClient;
		this.objectMapper = objectMapper;
		this.xmlMapper = xmlMapper;
	}
	
    /**
     * 산림청_산사태예측정보 조회
     * @param <T>
     * @param request
     * @param responseType 최종 반환할 클래스
     *  요청 방법 예시 getLandslidePredictionInfo(request, PredictionInfoResponse.class) 
     */
    public <T> Mono<PublicDataResponse<T>> getLandslidePredictionInfo(DisasterExternApiRequest request, Class<T> responseType) {
        log.info("request:{}", request);
        
        return publicDataWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/1400000/predictionInfoService/predictionInfoList")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("pageNo", request.getPageNo())
                        .queryParam("numOfRows", request.getNumOfRows())
                        .queryParam("_type", request.getType())
                        .queryParam("sgg", request.getSgg())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(responseString -> parseResponse(responseString, request.getType(), responseType))
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(firstResponse -> log.info("Successfully fetched page {}", firstResponse))
                .doOnError(error -> log.error("Error fetching page {}: {}", request.getPageNo(), error.getMessage()))
                .onErrorResume(CustomException.class, e -> {
                    log.error("WebClient error on page: Status={}", e.getMessage());
                    return Mono.empty();
                })
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))  // 재시도 횟수 축소 (3->2)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying page {} - attempt {}", request.getPageNo(), retrySignal.totalRetries() + 1)));
                /*
                // 마지막 페이지의 번호로 시작
                .flatMap(response -> {
                    if (response == null || response.getResponse().getBody() == null) {
                        log.warn("First response is empty");
                        return Mono.empty();
                    }
                    
                    // 첫 번째 응답 데이터를 사용해서 두 번째 요청
                    double totalCount = response.getResponse().getBody().getTotalCount();
                    Integer numOfRows = response.getResponse().getBody().getNumOfRows();
                    int lastPage = (int) Math.ceil(totalCount/numOfRows);
                    log.info("전체 페이지 수: {}", lastPage);
                    
                    return publicDataWebClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/1400000/predictionInfoService/predictionInfoList")
                                    .queryParam("serviceKey", serviceKey)
                                    .queryParam("pageNo", lastPage)
                                    .queryParam("numOfRows", numOfRows)
                                    .queryParam("_type", request.getType())
                                    .queryParam("sgg", request.getSgg())
                                    .build())
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<PublicDataResponse<PredictionInfoResponse>>() {})
                            .timeout(Duration.ofMillis(timeout))
                            .doOnSuccess(secondResponse -> log.info("Successfully fetched page {}", secondResponse))
                            .doOnError(error -> log.error("Error fetching page {}: {}", request.getPageNo(), error.getMessage()))
                            .onErrorResume(WebClientResponseException.class, e -> {
                                log.error("WebClient error on page {}: Status={}", request.getPageNo(), e.getStatusCode());
                                return Mono.empty();
                            })
                            .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))  // 재시도 횟수 축소 (3->2)
                                    .maxBackoff(Duration.ofSeconds(5))
                                    .doBeforeRetry(retrySignal -> 
                                            log.warn("Retrying page {} - attempt {}", request.getPageNo(), retrySignal.totalRetries() + 1)));
                });
                */
    }
    
    /**
     * 응답타입에 따른 객체 변환
     * @param responseString 응답데이터
     * @param type 변환할 타입(xml, json)
     * @return PublicDataResponse 공공데이터응답객체
     */
    private <T> PublicDataResponse<T> parseResponse(String responseString, String type, Class<T> responseType) {
    	System.out.println(responseString);
        try {
            if ("xml".equalsIgnoreCase(type)) {
                return xmlMapper.readValue(
                        responseString,
                        xmlMapper.getTypeFactory().constructParametricType(
                        		PublicDataResponse.class,
                        		responseType
                        )
                );
            } else {
                return objectMapper.readValue(
                        responseString,
                        objectMapper.getTypeFactory().constructParametricType(
                                PublicDataResponse.class,
                                responseType
                        )
                );
            }
        } catch (Exception e) {
            log.error("Parsing failed for type {}: {}", type, e.getMessage());
            throw new CustomException(ErrorCode.DATA_PARSING_ERROR);
        }
    }
    
}
