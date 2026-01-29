//
//// ====== DB에서 목록 가져오는 인터페이스 ======
//// service에서는 기능의 이름만 정의 해두는 것!
//package jbell.disasterAccident.service;
//
//import java.util.List;
//import jbell.disasterAccident.dto.DisasterMessageResponse;
//import reactor.core.publisher.Mono;
//
//public interface DisasterAccidentService {
//	
//    // 조회 로직
//	// 저장된 재난문자 목록을 전부 다 보여주는 기능 정의
//    Mono<List<DisasterMessageResponse>> selectDisasterMessageList();
//    
//    // 수집 로직 (추가)
//    // 외부 api에서 데이터 가져오고 db에 저장하는 기능 정의
//    Mono<String> fetchAndSaveDisasterMessage();
//}
