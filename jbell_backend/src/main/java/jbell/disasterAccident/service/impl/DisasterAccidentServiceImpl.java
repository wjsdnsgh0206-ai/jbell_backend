//package jbell.disasterAccident.service.impl;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import com.fasterxml.jackson.databind.JsonNode;
//
//import jbell.disasterAccident.dto.DisasterMessageResponse;
//import jbell.disasterAccident.mapper.DisasterAccidentMapper;
//import jbell.disasterAccident.service.DisasterAccidentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import reactor.core.publisher.Flux; // 💡 추가
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DisasterAccidentServiceImpl implements DisasterAccidentService {
//
//	private final DisasterAccidentMapper disasterAccidentMapper;
//
//	@Value("${VITE_API_DISATER_TEXT_MESSAGE_KEY}")
//	private String dmsKey;
//
//	// 1. 자동 수집 스케줄러 (매 1시간마다 실행)
//	@Scheduled(cron = "0 0 * * * *")
//	// fetchAndSaveDisasterMessage() 메서드를 실행(subscribe)하여 
//	// 외부 API 데이터를 가져온 뒤 DB에 자동으로 저장
//	public void scheduledFetch() {
//		log.info(">>> [스케줄러] 재난문자 자동 수집 시작");
//		fetchAndSaveDisasterMessage().subscribe();
//	}
//
//	// 2. select로 db데이터 불러오기
//	@Override
//	public Mono<List<DisasterMessageResponse>> selectDisasterMessageList() {
//		// Mono.fromCallable(() -> ...)은 이 작업을 나중에 실행 할 수 있게 Mono라는 비동기작업 보관함에 넣어달라는 의미.
//		// disasterAccidentMapper.selectDisasterMessageList() : xml의 select쿼리 실행하여 db에 있는 재난문자 불러오기
//		return Mono.fromCallable(() -> disasterAccidentMapper.selectDisasterMessageList())
//				// db조회 작업은 별도의 작업실(boundedElastic)가서 하라고 지정. 
//				.subscribeOn(Schedulers.boundedElastic());
//	}
//
//	// 3. 외부api호출 후, 데이터 수집하는 로직
//	@Override
//	public Mono<String> fetchAndSaveDisasterMessage() { 
//		log.info(">>> 재난문자 수집 시작");
//		
//		// 7일치 날짜 리스트 생성 (기존에 작성한 리스트 로직 유지)
//		List<String> targetDates = java.util.stream.IntStream.range(0, 7)
//	            .mapToObj(i -> LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyyMMdd")))
//	            .collect(java.util.stream.Collectors.toList());
//		
//		// 공공 api 서버의 기본 주소 설정.
//		WebClient webClient = WebClient.builder().baseUrl("https://www.safetydata.go.kr").build();
//		
//		// Flux.fromIterable을 사용하여 7일간의 날짜를 하나씩 꺼내어 반복 호출함
//		return Flux.fromIterable(targetDates)
//				.flatMap(date -> { // 💡 targetDates에서 꺼낸 각 날짜(date)를 사용하여 API 호출
//					log.info(">>> [{}] 날짜 데이터 수집 시도", date);
//					
//					// 어떤 데이터를 가져올지 설정.
//					return webClient.get()
//							.uri(uriBuilder -> uriBuilder.path("/V2/api/DSSP-IF-00247")
//									.queryParam("serviceKey", dmsKey)
//									.queryParam("returnType", "json")
//									.queryParam("pageNo", 1) 
//									.queryParam("crtDt", date) // 💡 루프를 도는 현재 날짜(date) 적용
//									.queryParam("numOfRows", 100) // 100개씩 긁어오자!
//									.queryParam("rgnNm", "전북") // "전북"으로 검색
//									.build())
//							// 실제 api서버에 신호를 보내서 데이터를 가져오고, 가져온 Json데이터를 JsonNode형태로 변환.
//							.retrieve().bodyToMono(JsonNode.class).flatMap(response -> {
//								// response.pah("body"): api응답중, 데이터가 들어있는 body부분만 빼는 작업.
//								JsonNode dataList = response.path("body");
//
//								// 만약, 가져온 데이터가 없거나 형식이 이상하다면 데이터가 없다고 처리하고 종료.
//								if (!dataList.isArray() || dataList.isEmpty()) {
//									// Flux 내에서 처리 중이므로 Mono.empty()를 반환하여 다음 날짜로 진행
//									return Mono.empty();
//								}
//								
//								// 날짜 형식 변환
//								DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//								
//								// subscribeOn을 사용하여 DB 저장을 별도 스레드에서 실행하고 완료되면 응답 반환
//								return Mono.fromRunnable(() -> {
//									// 수집된 데이터를 하나씩 반복하여 꺼냄
//									for (JsonNode node : dataList) {
//										
//										// 루프 시작 직후에 추가
//										log.info(">>> API 수신 데이터 - SN: {}, 날짜: {}", node.path("SN").asInt(), node.path("CRT_DT").asText());
//										
//										// 꺼낸 데이터들을 자바객체변수에 담는다. (ex. dto.setSn())
//										DisasterMessageResponse dto = new DisasterMessageResponse();
//										dto.setSn(node.path("SN").asInt());
//										// crtDt: 문자열을 받아서 LocalDate로 변환
//										String dateStr = node.path("CRT_DT").asText(); // "2023/09/19 12:22:17"
//										if (dateStr != null && !dateStr.isEmpty()) {
//											// 1. 문자열을 일단 시간까지 포함된 LocalDateTime으로 파싱
//											// 2. 그중에서 날짜 부분만 쏙 빼서(toLocalDate) dto에 저장
//											LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
//											dto.setCrtDt(localDateTime);
//										}
//										dto.setMsgCn(node.path("MSG_CN").asText());
//										dto.setRcptnRgnNm(node.path("RCPTN_RGN_NM").asText());
//										dto.setEmrgStepNm(node.path("EMRG_STEP_NM").asText());
//										
//										// api에서 제공되는 재난구분이름(ex.지진)을 apiSeNm에 저장한다.
//										String apiSeNm = node.path("DST_SE_NM").asText();
//										String dbCode = "ITEM_001";
//
//										// 한글이름을 DB전용코드로 변환.(DB컬럼에 맞게 변환해야 외래키에러없이 들어감)
//										if (apiSeNm.contains("태풍"))
//											dbCode = "NATURAL_TYPHOON";
//										else if (apiSeNm.contains("홍수"))
//											dbCode = "NATURAL_FLOOD";
//										else if (apiSeNm.contains("지진"))
//											dbCode = "NATURAL_EARTHQUAKE";
//										else if (apiSeNm.contains("호우"))
//											dbCode = "NATURAL_HEAVYRAIN";
//
//										dto.setDstType(dbCode);
//										
//										// 가져온 데이터들을 db에 저장한다. 
//										try {
//											disasterAccidentMapper.insertDisasterMessage(dto);
//										} catch (Exception e) {
//											log.error(">>> 중복 또는 저장 에러 (SN: {}): {}", dto.getSn(), e.getMessage());
//										}
//									}
//								// 작업들을 스레드에게 맡기고, 모든 작업이 끝나면 성공 메시지 발행한다.
//								}).subscribeOn(Schedulers.boundedElastic());
//							});
//				})
//				.then(Mono.just("최근 7일 데이터 수집 및 저장 완료!")); // 💡 모든 날짜 처리가 끝나면 결과 메시지 반환
//	}
//}