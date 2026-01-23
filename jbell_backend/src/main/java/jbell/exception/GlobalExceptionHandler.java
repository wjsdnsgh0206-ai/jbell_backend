package jbell.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jbell.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	/**
	 * 사용자 정의 예외처리
	 * @param ex
	 * @param request
	 * @return ResponseEntity<ApiResponse> 에러필드명, 에러메시지
	 */
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<?>> handleCustomException(CustomException ex, HttpServletRequest request){
		StackTraceElement[] stackTrace = ex.getStackTrace();
		StackTraceElement origin = stackTrace[0];
		log.error( "\n[Exception] {}\n[Method]:{} ({}:{}) - message={}"
					, origin.getClassName()
					, origin.getMethodName()
					, origin.getFileName()
					, origin.getLineNumber()
					, ex.getMessage()
				 );
		ErrorCode errorCode = ex.getErrorCode();
		ApiResponse<String> response = ApiResponse.error(errorCode.status().value(), ex.getMessage());
		return ResponseEntity.status(errorCode.status().value()).body(response);
	}
	
	/**
	 * 유효성검사(spring boot validation) 예외처리
	 * @param MethodArgumentNotValidException ex
	 * @param request
	 * @return ResponseEntity<ApiResponse> 에러필드명, 에러메시지
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<List<Map<String, String>>>> handleValidationException
										(MethodArgumentNotValidException ex, HttpServletRequest request){
		StackTraceElement[] stackTrace = ex.getStackTrace();
		StackTraceElement origin = stackTrace[0];
		log.error( "\n[Exception] {}\n[Method]:{} ({}:{}) - message={}"
					, origin.getClassName()
					, origin.getMethodName()
					, origin.getFileName()
					, origin.getLineNumber()
					, ex.getMessage()
				 );
		// 1. validation error리스트 생성
		var error = ex.getBindingResult()
				      .getFieldErrors().stream()
									   .map(filedError -> Map.of(
											  						"filed", filedError.getField()
											  					,   "message", filedError.getDefaultMessage()
									   )).collect(Collectors.toList());
		ApiResponse<List<Map<String, String>>> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value()
														, "유효하지않은 데이터입니다."
														, error);
		return ResponseEntity.badRequest().body(response);
	}

	/**
	 * 전역 예외처리
	 * @param ex
	 * @param request
	 * @return ResponseEntity<ApiResponse> 
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handelGlobalException(Exception ex, HttpServletRequest request){
		// 사용자의 요청정보 객체 HttpServletRequest
		String uri = request.getRequestURI();
		
		StackTraceElement[] stackTrace = ex.getStackTrace();
		StackTraceElement origin = stackTrace[0];
		log.error( "\n[Exception] {}\n[Method]:{} ({}:{}) - message={}"
					, origin.getClassName()
					, origin.getMethodName()
					, origin.getFileName()
					, origin.getLineNumber()
					, ex.getMessage()
				 );
		ApiResponse<String> response = ApiResponse.error(
														HttpStatus.INTERNAL_SERVER_ERROR.value()
														, "관리자에게 문의하세요"
														);
		return ResponseEntity.internalServerError().body(response);
	}
}








