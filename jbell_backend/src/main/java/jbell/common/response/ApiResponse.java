package jbell.common.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
/**
 * API 공통 응답 클래스
 * @param <T> 응답데이터 타입 (성공시) 또는 오류 상세 (실패시)
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
	private final LocalDateTime localDateTime;		// 서버응답시 시간
	private final String status;					// HTTP 요청 "SUCCESS", "ERROR"
	private final int httpCode;						// HTTP 상태코드 (예 : 2xx, 3xx, 4xx, 500 etc)
	private final String message;					// 응답메시지 (ex: "ok", "사용자를 찾을 수 없습니다.")
	private final T data;							// 실제 응답되는 data
	
	private ApiResponse(String status, int httpCode, String message, T data) {
		super();
		this.localDateTime = LocalDateTime.now(); // 응답 생성시간 자동 할당
		this.status = status;
		this.httpCode = httpCode;
		this.message = message;
		this.data = data;
	}
	
	// =================== 성공 응답 (데이터 포함) =========================
	/**
	 * 200 성공 응답
	 * @param T data (응답시 데이터)
	 */
	public static <T> ApiResponse<T> success(T data){
		return new ApiResponse<T>("SUCCESS", 200, "OK", data);
	}
	
	/**
	 * 200대 성공 응답(사용자 정의 메시지)
	 * @param T data (응답시 데이터)
	 * @param String message (ex: "OK", "CREATED")
	 */
	public static <T> ApiResponse<T> success(T data, int httpCode, String message){
		return new ApiResponse<T>("SUCCESS", httpCode, message, data);
	}
	
	// =================== 실패 응답 =========================
	/**
	 * 실패 응답 (데이터본문 없음)
	 * @param httpCode  HTTP 상태코드
	 * @param message	오류 메시지
	 */
	public static <T> ApiResponse<T> error(int httpCode, String message){
		return new ApiResponse<T>("ERROR", httpCode, message, null);
	}
	
	/**
	 * 실패 응답 (데이터본문 있음)
	 * @param httpCode  HTTP 상태코드
	 * @param message	오류 메시지
	 * @param message	DATA 오류 상세 내역
	 */
	public static <T> ApiResponse<T> error(int httpCode, String message, T data){
		return new ApiResponse<T>("ERROR", httpCode, message, data);
	}
}






















