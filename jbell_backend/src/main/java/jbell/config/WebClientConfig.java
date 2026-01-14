package jbell.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@Slf4j
public class WebClientConfig {
	
	/**
	 * objectMapper 공통설정후 bean생성 json 변환
	 */
	@Bean("objectMapper")
	ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		// Java 8 날짜/시간 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());
        // 날짜를 타임스탬프가 아닌 ISO-8601 형식으로 직렬화
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);        
		objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		return objectMapper;
	}
	
	/**
	 * xmlMapper 공통설정후 bean생성 xml 변환
	 */
	@Bean("xmlMapper")
	XmlMapper xmlMapper() {
		XmlMapper xmlMapper = new XmlMapper();
		// Java 8 날짜/시간 모듈 등록
		xmlMapper.registerModule(new JavaTimeModule());
        // 날짜를 타임스탬프가 아닌 ISO-8601 형식으로 직렬화
		xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		xmlMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);	
		return xmlMapper;
	}

	/**
	 * 요청/응답 로깅 필터
	 */
	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			log.info("========== WebClient Request ==========");
			log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
			log.info("Headers: {}", clientRequest.headers());
			log.info("=======================================");
			return Mono.just(clientRequest);
		});
	}

	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			log.info("========== WebClient Response ==========");
			log.info("Status: {}", clientResponse.statusCode());
			log.info("Headers: {}", clientResponse.headers().asHttpHeaders());
			log.info("========================================");
			return Mono.just(clientResponse);
		});
	}
	
	/**
     * 공공데이터 WebClient 설정 (XML/JSON 자동 파싱)
     */
    @Bean("publicDataWebClient")
    WebClient publicDataWebClient(
            @Qualifier("objectMapper") ObjectMapper objectMapper,
            @Qualifier("xmlMapper") XmlMapper xmlMapper) {
        
        ConnectionProvider connectionProvider = ConnectionProvider.builder("public-data-pool")
                .maxConnections(10)
                .maxIdleTime(Duration.ofSeconds(10))
                .maxLifeTime(Duration.ofSeconds(30))
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .evictInBackground(Duration.ofSeconds(60))
                .build();
        
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .responseTimeout(Duration.ofMillis(15000))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(15000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(15000, TimeUnit.MILLISECONDS)));
        
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024); // 2MB
                    
                    // JSON 코덱
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                    
                    // XML 코덱 (XmlMapper 사용)
                    configurer.customCodecs().register(
                            new Jackson2JsonEncoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
                    configurer.customCodecs().register(
                            new Jackson2JsonDecoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
                })
                .build();
        
        return WebClient.builder()
                .baseUrl("https://apis.data.go.kr")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .filter(logRequest()) // 필터 없애면 요청 로그 없어짐
                .filter(logResponse()) // 필터 없애면 응담 로그 없어짐
                .build();
    }
    
    /**
     * 재난안전공공데이터 포털 WebClient 설정 (XML/JSON 자동 파싱)
     */
    @Bean("safetyDataWebClient")
    WebClient safetyDataWebClient(
            @Qualifier("objectMapper") ObjectMapper objectMapper,
            @Qualifier("xmlMapper") XmlMapper xmlMapper) {
        
        ConnectionProvider connectionProvider = ConnectionProvider.builder("safety-data-pool")
                .maxConnections(10)
                .maxIdleTime(Duration.ofSeconds(10))
                .maxLifeTime(Duration.ofSeconds(30))
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .evictInBackground(Duration.ofSeconds(60))
                .build();
        
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .responseTimeout(Duration.ofMillis(15000))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(15000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(15000, TimeUnit.MILLISECONDS)));
        
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024); // 2MB
                    
                    // JSON 코덱
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                    
                    // XML 코덱 (XmlMapper 사용)
                    configurer.customCodecs().register(
                            new Jackson2JsonEncoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
                    configurer.customCodecs().register(
                            new Jackson2JsonDecoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
                })
                .build();
        
        return WebClient.builder()
                .baseUrl("https://www.safetydata.go.kr/V2/api")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }
    
    /**
     * 날씨 WebClient 설정 (XML/JSON 자동 파싱)
     */
    @Bean("weatherDataWebClient")
    WebClient weatherDataWebClient(
    		@Qualifier("objectMapper") ObjectMapper objectMapper,
    		@Qualifier("xmlMapper") XmlMapper xmlMapper) {
    	
    	ConnectionProvider connectionProvider = ConnectionProvider.builder("weather-data-pool")
    			.maxConnections(10)
    			.maxIdleTime(Duration.ofSeconds(10))
    			.maxLifeTime(Duration.ofSeconds(30))
    			.pendingAcquireTimeout(Duration.ofSeconds(30))
    			.evictInBackground(Duration.ofSeconds(60))
    			.build();
    	
    	HttpClient httpClient = HttpClient.create(connectionProvider)
    			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
    			.option(ChannelOption.SO_KEEPALIVE, false)
    			.responseTimeout(Duration.ofMillis(15000))
    			.doOnConnected(conn -> conn
    					.addHandlerLast(new ReadTimeoutHandler(15000, TimeUnit.MILLISECONDS))
    					.addHandlerLast(new WriteTimeoutHandler(15000, TimeUnit.MILLISECONDS)));
    	
    	ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
    			.codecs(configurer -> {
    				configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024); // 2MB
    				
    				// JSON 코덱
    				configurer.defaultCodecs().jackson2JsonEncoder(
    						new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
    				configurer.defaultCodecs().jackson2JsonDecoder(
    						new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
    				
    				// XML 코덱 (XmlMapper 사용)
    				configurer.customCodecs().register(
    						new Jackson2JsonEncoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
    				configurer.customCodecs().register(
    						new Jackson2JsonDecoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
    			})
    			.build();
    	
    	return WebClient.builder()
    			.baseUrl("https://api.openweathermap.org/data/2.5")
    			.clientConnector(new ReactorClientHttpConnector(httpClient))
    			.exchangeStrategies(exchangeStrategies)
    			.build();
    }
    
    /**
     * 기상청 api 허브 WebClient 설정 (XML/JSON 자동 파싱)
     */
    @Bean("apihubDataWebClient")
    WebClient apihubDataWebClient(
    		@Qualifier("objectMapper") ObjectMapper objectMapper,
    		@Qualifier("xmlMapper") XmlMapper xmlMapper) {
    	
    	ConnectionProvider connectionProvider = ConnectionProvider.builder("apihub-data-pool")
    			.maxConnections(10)
    			.maxIdleTime(Duration.ofSeconds(10))
    			.maxLifeTime(Duration.ofSeconds(30))
    			.pendingAcquireTimeout(Duration.ofSeconds(30))
    			.evictInBackground(Duration.ofSeconds(60))
    			.build();
    	
    	HttpClient httpClient = HttpClient.create(connectionProvider)
    			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
    			.option(ChannelOption.SO_KEEPALIVE, false)
    			.responseTimeout(Duration.ofMillis(15000))
    			.doOnConnected(conn -> conn
    					.addHandlerLast(new ReadTimeoutHandler(15000, TimeUnit.MILLISECONDS))
    					.addHandlerLast(new WriteTimeoutHandler(15000, TimeUnit.MILLISECONDS)));
    	
    	ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
    			.codecs(configurer -> {
    				configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024); // 2MB
    				
    				// JSON 코덱
    				configurer.defaultCodecs().jackson2JsonEncoder(
    						new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
    				configurer.defaultCodecs().jackson2JsonDecoder(
    						new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
    				
    				// XML 코덱 (XmlMapper 사용)
    				configurer.customCodecs().register(
    						new Jackson2JsonEncoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
    				configurer.customCodecs().register(
    						new Jackson2JsonDecoder(xmlMapper, MediaType.APPLICATION_XML, MediaType.TEXT_XML));
    			})
    			.build();
    	
    	return WebClient.builder()
    			.baseUrl("https://apihub.kma.go.kr/api")
    			.clientConnector(new ReactorClientHttpConnector(httpClient))
    			.exchangeStrategies(exchangeStrategies)
    			.build();
    }
}