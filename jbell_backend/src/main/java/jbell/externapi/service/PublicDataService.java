package jbell.externapi.service;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jbell.disaster.dto.DisasterExternApiRequest;
import jbell.disaster.dto.PredictionInfoResponse;
import jbell.disaster.mapper.DisasterMessageMapper;
import jbell.disaster.mapper.WeatherWarningMapper;
import jbell.disasterAccident.mapper.DisasterAccidentMapper;
import jbell.exception.CustomException;
import jbell.exception.ErrorCode;
import jbell.externapi.dto.PublicDataResponse;
import jbell.externapi.dto.SafetyDataResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Slf4j
public class PublicDataService {
    
    @Value("${publicdata.servicekey}")
    private String serviceKey;
    
    @Value("${external.api.timeout}")
    private int timeout;

    @Value("${safetydata.disaster.msg.key}") 
    private String safetyDataServiceKey;
    
    @Value("${safetydata.weatherWarning.key}")
    private String safetyDataWeatherWarningKey;
    
    private final WebClient publicDataWebClient;
    private final WebClient safetyDataWebClient; 
    private final ObjectMapper objectMapper;    
    private final XmlMapper xmlMapper;
    private final DisasterMessageMapper disasterMessageMapper;
    private final WeatherWarningMapper weatherWarningMapper;
    
    
    public PublicDataService(@Qualifier("publicDataWebClient") WebClient publicDataWebClient
                            ,@Qualifier("safetyDataWebClient") WebClient safetyDataWebClient 
                            ,@Qualifier("objectMapper") ObjectMapper objectMapper
                            ,@Qualifier("xmlMapper") XmlMapper xmlMapper
                            ,@Qualifier("disasterMessageMapper") DisasterMessageMapper disasterMessageMapper
                            ,@Qualifier("weatherWarningMapper") WeatherWarningMapper weatherWarningMapper
                            ,DisasterAccidentMapper disasterAccidentMapper 
                            ) {
        this.publicDataWebClient = publicDataWebClient;
        this.safetyDataWebClient = safetyDataWebClient;
        this.objectMapper = objectMapper;
        this.xmlMapper = xmlMapper;
        this.disasterMessageMapper = disasterMessageMapper;
        this.weatherWarningMapper = weatherWarningMapper; // ⭐ 이 줄이 정확히 있는지 확인!
    }
    
    // 산사태 정보 조회 (기존 유지)
    public <T> Mono<PublicDataResponse<T>> getLandslidePredictionInfo(DisasterExternApiRequest request, Class<T> responseType) {
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
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)));
    }
    
    private <T> PublicDataResponse<T> parseResponse(String responseString, String type, Class<T> responseType) {
        try {
            if ("xml".equalsIgnoreCase(type)) {
                return xmlMapper.readValue(responseString, xmlMapper.getTypeFactory().constructParametricType(PublicDataResponse.class, responseType));
            } else {
                return objectMapper.readValue(responseString, objectMapper.getTypeFactory().constructParametricType(PublicDataResponse.class, responseType));
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DATA_PARSING_ERROR);
        }
    }
    
    // ================================================================================
    
    // 재난문자 단건 조회 (기존 queryParam 유지)
    public <T> Mono<SafetyDataResponse<T>> getDisasterMessageInfo(DisasterExternApiRequest request, Class<T> responseType) {
        String formattedCrtDt = request.getCrtDt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));   
        
        return safetyDataWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/DSSP-IF-00247")
                        .queryParam("serviceKey", safetyDataServiceKey)
                        .queryParam("numOfRows", request.getNumOfRows())
                        .queryParam("pageNo", request.getPageNo())
                        .queryParam("returnType", request.getType())
                        .queryParam("crtDt", formattedCrtDt)
                        .queryParam("rgnNm", request.getRgnNm())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(responseString -> parseSafetyResponse(responseString, responseType));
    }

    private <T> SafetyDataResponse<T> parseSafetyResponse(String responseString, Class<T> responseType) {
        try {
            return objectMapper.readValue(responseString, objectMapper.getTypeFactory().constructParametricType(SafetyDataResponse.class, responseType));
        } catch (Exception e) {
            log.error("파싱 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.DATA_PARSING_ERROR);
        }
    }
    
    /**
     * ⭐ [수정 핵심] 7일치 데이터를 저장하고, 컨트롤러가 원하는 응답 타입을 정확히 리턴함
     */
    public Mono<SafetyDataResponse<PredictionInfoResponse>> getAndSaveDisasterMessages(DisasterExternApiRequest request) {
        // 1. 오늘 날짜 데이터 호출 준비 (리턴용)
        Mono<SafetyDataResponse<PredictionInfoResponse>> todayResponseMono = getDisasterMessageInfo(request, PredictionInfoResponse.class);

        // 2. 0~6일까지 (오늘 포함 7일) 반복 수집
        return Flux.range(0, 7)
                .flatMap(day -> {
                    // 반복용 요청 객체 생성 (날짜만 변경)
                    DisasterExternApiRequest dailyReq = new DisasterExternApiRequest();
                    dailyReq.setCrtDt(request.getCrtDt().minusDays(day));
                    dailyReq.setRgnNm(request.getRgnNm());
                    dailyReq.setNumOfRows(request.getNumOfRows());
                    dailyReq.setPageNo(request.getPageNo());
                    dailyReq.setType(request.getType());

                    return getDisasterMessageInfo(dailyReq, PredictionInfoResponse.class)
                            .doOnNext(response -> {
                                if (response.getBody() != null && !response.getBody().isEmpty()) {
                                    disasterMessageMapper.insertDisasterMessages(response.getBody());
                                    log.info("{} 수집 및 저장 완료", dailyReq.getCrtDt());
                                }
                            });
                })
                .then(todayResponseMono); // 7일치 작업이 다 끝나면 '오늘 응답'을 컨트롤러에 전달!
    }
    
    public List<PredictionInfoResponse> getSavedDisasterMessages() {
        return disasterMessageMapper.selectDisasterMessageList();
    }
    
    
    
    // ================================================================================
    
    // 기상청 실시간 특보 
    /**
     * 기상청 특보 단건 호출 로직
     */
    public <T> Mono<SafetyDataResponse<T>> getWeatherWarningInfo(DisasterExternApiRequest request, Class<T> responseType) {
        // 기상청 특보는 YYYYMMDD 형식 문자열이 필요함 (inqDt 필드 활용)
        String inqDt = request.getInqDt(); 
        
        return safetyDataWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/DSSP-IF-00045") // 기상청 특보 엔드포인트
                        .queryParam("serviceKey", safetyDataWeatherWarningKey)
                        .queryParam("numOfRows", request.getNumOfRows())
                        .queryParam("pageNo", request.getPageNo())
                        .queryParam("returnType", "json")
                        .queryParam("inqDt", inqDt)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(responseString -> parseSafetyResponse(responseString, responseType));
    }

    /**
     * 기상청 특보 수집 및 저장 (7일치 데이터)
     */
    public Mono<SafetyDataResponse<PredictionInfoResponse>> getAndSaveWeatherWarnings(DisasterExternApiRequest request) {
        // 1. 리턴용 현재 요청 응답 준비
        Mono<SafetyDataResponse<PredictionInfoResponse>> currentResponseMono = getWeatherWarningInfo(request, PredictionInfoResponse.class);

        // 2. 7일치 반복 수집 (기상청 특보는 inqDt가 문자열이므로 계산 필요)
        return Flux.range(0, 7)
                .flatMap(day -> {
                    // 날짜 계산 (YYYYMMDD)
                    String targetDate = java.time.LocalDate.now().minusDays(day)
                                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                    
                    DisasterExternApiRequest dailyReq = new DisasterExternApiRequest();
                    dailyReq.setInqDt(targetDate);
                    dailyReq.setNumOfRows(100); // 넉넉하게 수집
                    dailyReq.setPageNo(1);

                    return getWeatherWarningInfo(dailyReq, PredictionInfoResponse.class)
                            .doOnNext(response -> {
                                if (response.getBody() != null && !response.getBody().isEmpty()) {
                                    weatherWarningMapper.insertWeatherWarnings(response.getBody());
                                    log.info("기상 특보 {} 일자 수집 및 저장 완료 ({}건)", targetDate, response.getBody().size());
                                }
                            });
                })
                .then(currentResponseMono);
    }

    /**
     * DB에서 저장된 기상 특보 가져오기
     */
    public List<PredictionInfoResponse> getSavedWeatherWarnings() {
        return weatherWarningMapper.selectWeatherWarningList();
    }
}