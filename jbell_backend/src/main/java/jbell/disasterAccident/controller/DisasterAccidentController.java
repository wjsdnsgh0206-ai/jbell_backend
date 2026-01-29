//// ====== 프론트랑 통신 하는 곳 ======
//// 데이터를 요청하면, 그 요청을 받아서 서비스에게 일처리를 시키고 결과물을 다시 프론트로 돌려줌. 
//package jbell.disasterAccident.controller;
//
//import java.util.List;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import jbell.common.response.ApiResponse;
//import jbell.disasterAccident.dto.DisasterMessageResponse;
//import jbell.disasterAccident.service.DisasterAccidentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import reactor.core.publisher.Mono;
//
//// @RestController : 데이터를 주고받는 api전용 컨트롤러라는 의미
//@RestController
//// @RequstMapping() : 이 컨트롤러로 들어오기 위한 주소. 모든 요청은 이 주소로 시작함.
//@RequestMapping("/api/disaster")
//// @Slf4j : 로그를 남길 수 있게 도와주는 도구
//@Slf4j
//public class DisasterAccidentController {
//
//	// 의존성 주입 : DisasterAccidentService를 불러와서 사용할 준비 (비즈니스 로직 실행준비)
//    private final DisasterAccidentService disasterAccidentService;
//    public DisasterAccidentController(DisasterAccidentService disasterAccidentService) {
//        this.disasterAccidentService = disasterAccidentService;
//    }
//
//    // 데이터 가져와서 db에 넣기.
//    // 왜 post방식을 사용하냐? 외부에서 데이터를 가져와서 db에 넣는 작업이니 단순 조회가 아님. 생성/변경시에는 post사용이 맞음.
//    @PostMapping("/fetch")
//    public Mono<ApiResponse<String>> fetchMessages() {
//        return disasterAccidentService.fetchAndSaveDisasterMessage()
//                .thenReturn(ApiResponse.success("데이터 수집 완료!"));
//    }
//
//    // db에 저장된 데이터를 꺼내서 프론트로 보내는 바구니(ApiResponse에 담는다.)
//    // get방식은 조회기능 구현시 사용.
//    @GetMapping("/message-list")
//    public Mono<ApiResponse<List<DisasterMessageResponse>>> getDisasterMessageList() {
//        return disasterAccidentService.selectDisasterMessageList()
//        		// 서비스가 db에서 가져온 데이터 list들을 ApiResponse에 담아서 프론트엔드로 보내줌. 
//        		// 풀어서 코드 작성하면, .map(list -> ApiResponse.success(list))
//                .map(ApiResponse::success); // success는 메서드임. 그 메서드 안에는 (상태코드/메시지/데이터)가 들어있음.
//    }
//}
