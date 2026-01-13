package jbell.exception;

import org.springframework.http.HttpStatus;

/**
 * API 에러 코드 정의
 * <p>
 * HTTP 상태 코드와 에러 메시지를 관리하는 Enum 클래스
 * </p>
 */
public enum ErrorCode {

	/* ==================== 4XX CLIENT ERROR ==================== */

	// 400 Bad Request
	/** 잘못된 요청 */
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

	/** 입력값이 유효하지 않음 */
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),

	/** 필수 입력 필드 누락 */
	MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "필수 입력값이 누락되었습니다."),

	/** 입력 타입 불일치 */
	INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "입력 타입이 올바르지 않습니다."),

	/** 입력 형식 오류 */
	INVALID_FORMAT(HttpStatus.BAD_REQUEST, "입력 형식이 올바르지 않습니다."),

	/** 전화번호 형식 오류 (예: 010-1234-5678) */
	INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "전화번호 형식이 올바르지 않습니다."),

	/** 이메일 형식 오류 */
	INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),

	/** 비밀번호 형식 오류 (길이, 특수문자 등) */
	INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다."),

	/** 비밀번호 확인 불일치 */
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

	/** 지원하지 않는 파일 확장자 */
	INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),

	/** 파일 크기 제한 초과 */
	FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다."),

	/** 나이 범위 설정 오류 (시작 나이 > 종료 나이) */
	INVALID_AGE_RANGE(HttpStatus.BAD_REQUEST, "나이 범위가 올바르지 않습니다."),

	/** 가격이 음수이거나 0 */
	INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격은 0보다 커야 합니다."),

	/** 수량이 1 미만 */
	INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다."),

	// 401 Unauthorized
	/** 인증 필요 (로그인 안 됨) */
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

	/** 유효하지 않은 JWT 토큰 */
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

	/** 만료된 JWT 토큰 */
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

	/** 토큰이 요청 헤더에 없음 */
	TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),

	/** 로그인 실패 (아이디 또는 비밀번호 불일치) */
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),

	/** 로그인이 필요한 기능 */
	LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다."),

	/** 세션 만료 */
	SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "세션이 만료되었습니다. 다시 로그인해주세요."),

	// 403 Forbidden
	/** 접근 권한 없음 */
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

	/** 리소스 접근 거부 */
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 리소스에 대한 접근이 거부되었습니다."),

	/** 리소스 소유자가 아님 (본인 글만 수정/삭제 가능) */
	NOT_RESOURCE_OWNER(HttpStatus.FORBIDDEN, "본인의 리소스만 수정/삭제할 수 있습니다."),

	/** 밴드 멤버가 아님 */
	NOT_BAND_MEMBER(HttpStatus.FORBIDDEN, "밴드 멤버만 접근할 수 있습니다."),

	/** 밴드 리더가 아님 (리더 권한 필요) */
	NOT_BAND_LEADER(HttpStatus.FORBIDDEN, "밴드 리더만 수행할 수 있는 작업입니다."),

	/** 정지된 계정 (신고 등으로 인한 제재) */
	ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "정지된 계정입니다. 관리자에게 문의하세요."),

	/** 잠긴 계정 (로그인 시도 횟수 초과 등) */
	ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "잠긴 계정입니다."),

	/** 권한 부족 */
	INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "권한이 부족합니다."),

	/** 관리자 전용 기능 */
	ADMIN_ONLY(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다."),

	// 404 Not Found
	/** 리소스를 찾을 수 없음 */
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

	/** 상품정보를 찾을 수 없음 */
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 상품의 정보를 찾을 수 없습니다."),

	/** 사용자 정보 없음 */
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),

	/** 밴드 정보 없음 */
	BAND_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 밴드입니다."),

	/** 스튜디오 정보 없음 */
	STUDIO_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 스튜디오입니다."),

	/** 방 정보 없음 */
	ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 방입니다."),

	/** 게시글 정보 없음 */
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),

	/** 댓글 정보 없음 */
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),

	/** 답글 정보 없음 */
	REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 답글입니다."),

	/** 예약 정보 없음 */
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."),

	/** 결제 정보 없음 */
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 결제 정보입니다."),

	/** 파일 정보 없음 */
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

	/** 밴드 모집글 정보 없음 */
	BAND_RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 밴드 모집글입니다."),

	/** 게스트 모집글 정보 없음 */
	GUEST_RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게스트 모집글입니다."),

	/** 공연 정보 없음 */
	EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 공연입니다."),

	/** 중고거래 게시글 정보 없음 */
	TRADE_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 중고거래 게시글입니다."),

	/** 쪽지 정보 없음 */
	MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쪽지입니다."),

	/** 일정 정보 없음 */
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 일정입니다."),

	/** 신청 정보 없음 */
	APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신청 정보입니다."),

	// 405 Method Not Allowed
	/** 허용되지 않은 HTTP 메서드 (예: POST만 허용하는데 GET 요청) */
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),

	// 408 Request Timeout
	/** 요청 처리 시간 초과 */
	REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "요청 시간이 초과되었습니다."),

	// 409 Conflict
	/** 리소스 상태 충돌 */
	CONFLICT(HttpStatus.CONFLICT, "리소스 충돌이 발생했습니다."),

	/** 중복된 아이디 */
	CONFLICT_ID(HttpStatus.CONFLICT, "사용할 수 없는 아이디입니다. 다른 아이디를 입력해 주세요."),

	/** 중복된 이메일 */
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

	/** 중복된 닉네임 */
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

	/** 중복된 전화번호 */
	DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "이미 등록된 전화번호입니다."),

	/** 중복된 밴드 이름 */
	DUPLICATE_BAND_NAME(HttpStatus.CONFLICT, "이미 사용 중인 밴드 이름입니다."),

	/** 중복된 스튜디오 이름 */
	DUPLICATE_STUDIO_NAME(HttpStatus.CONFLICT, "이미 등록된 스튜디오 이름입니다."),

	/** 이미 밴드 멤버로 가입됨 */
	ALREADY_BAND_MEMBER(HttpStatus.CONFLICT, "이미 해당 밴드의 멤버입니다."),

	/** 이미 신청 완료 */
	ALREADY_APPLIED(HttpStatus.CONFLICT, "이미 신청하였습니다."),

	/** 이미 스크랩 완료 */
	ALREADY_SCRAPED(HttpStatus.CONFLICT, "이미 스크랩한 게시글입니다."),

	/** 이미 좋아요 완료 */
	ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 누른 게시글입니다."),

	/** 예약 시간 충돌 */
	RESERVATION_CONFLICT(HttpStatus.CONFLICT, "해당 시간에 이미 예약이 존재합니다."),

	/** 일정 충돌 */
	SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "해당 시간에 이미 일정이 존재합니다."),

	/** 이미 처리된 요청 (중복 처리 방지) */
	ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),

	// 410 Gone
	/** 영구 삭제된 리소스 */
	DELETED_RESOURCE(HttpStatus.GONE, "삭제된 리소스입니다."),

	/** 탈퇴한 사용자 */
	WITHDRAWN_USER(HttpStatus.GONE, "탈퇴한 사용자입니다."),

	/** 해체된 밴드 */
	DISBANDED_BAND(HttpStatus.GONE, "해체된 밴드입니다."),

	/** 마감된 게시글 (모집 완료) */
	CLOSED_POST(HttpStatus.GONE, "마감된 게시글입니다."),

	/** 종료된 공연 */
	EXPIRED_EVENT(HttpStatus.GONE, "종료된 공연입니다."),

	/** 판매 완료된 중고거래 상품 */
	SOLD_OUT(HttpStatus.GONE, "판매 완료된 상품입니다."),

	// 413 Payload Too Large
	/** 요청 본문 크기 초과 */
	PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "요청 데이터가 너무 큽니다."),

	// 415 Unsupported Media Type
	/** 지원하지 않는 Content-Type */
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다."),

	// 422 Unprocessable Entity
	/** 의미상 오류가 있는 요청 */
	UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "처리할 수 없는 요청입니다."),

	/** 날짜 범위 오류 (시작일 > 종료일) */
	INVALID_DATE_RANGE(HttpStatus.UNPROCESSABLE_ENTITY, "날짜 범위가 올바르지 않습니다."),

	/** 시간 범위 오류 (시작 시간 > 종료 시간) */
	INVALID_TIME_RANGE(HttpStatus.UNPROCESSABLE_ENTITY, "시간 범위가 올바르지 않습니다."),

	/** 과거 날짜 선택 불가 */
	PAST_DATE_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "과거 날짜는 선택할 수 없습니다."),

	/** 영업 시간 외 예약 시도 */
	INVALID_BUSINESS_HOURS(HttpStatus.UNPROCESSABLE_ENTITY, "영업 시간이 아닙니다."),

	/** 최소 예약 시간 미충족 */
	MIN_RESERVATION_TIME_NOT_MET(HttpStatus.UNPROCESSABLE_ENTITY, "최소 예약 시간을 충족하지 못했습니다."),

	/** 최대 수용 인원 초과 */
	MAX_CAPACITY_EXCEEDED(HttpStatus.UNPROCESSABLE_ENTITY, "최대 인원을 초과했습니다."),

	/** 모집 마감 */
	RECRUITMENT_CLOSED(HttpStatus.UNPROCESSABLE_ENTITY, "모집이 마감되었습니다."),

	/** 모집 인원 초과 */
	RECRUITMENT_FULL(HttpStatus.UNPROCESSABLE_ENTITY, "모집 인원이 초과되었습니다."),

	/** 본인 게시글 신청 불가 */
	CANNOT_APPLY_OWN_POST(HttpStatus.UNPROCESSABLE_ENTITY, "본인의 게시글에는 신청할 수 없습니다."),

	/** 리더는 밴드 탈퇴 불가 */
	CANNOT_LEAVE_AS_LEADER(HttpStatus.UNPROCESSABLE_ENTITY, "리더는 밴드를 탈퇴할 수 없습니다. 리더를 위임하거나 밴드를 해체하세요."),

	/** 마지막 멤버 탈퇴 불가 */
	LAST_MEMBER_CANNOT_LEAVE(HttpStatus.UNPROCESSABLE_ENTITY, "마지막 멤버는 탈퇴할 수 없습니다."),

	/** 잔액 부족 */
	INSUFFICIENT_BALANCE(HttpStatus.UNPROCESSABLE_ENTITY, "잔액이 부족합니다."),

	/** 결제 금액 불일치 */
	PAYMENT_AMOUNT_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "결제 금액이 일치하지 않습니다."),

	/** 예약 취소 불가 상태 */
	CANNOT_CANCEL_RESERVATION(HttpStatus.UNPROCESSABLE_ENTITY, "예약 취소가 불가능한 상태입니다."),

	/** 취소 기한 경과 */
	CANCELLATION_DEADLINE_PASSED(HttpStatus.UNPROCESSABLE_ENTITY, "취소 가능 시간이 지났습니다."),

	/** 이미 취소된 예약 */
	ALREADY_CANCELLED(HttpStatus.UNPROCESSABLE_ENTITY, "이미 취소된 예약입니다."),

	/** 멤버가 있는 밴드는 삭제 불가 */
	CANNOT_DELETE_WITH_MEMBERS(HttpStatus.UNPROCESSABLE_ENTITY, "멤버가 있는 밴드는 삭제할 수 없습니다."),

	/** 승인 후 수정 불가 */
	CANNOT_MODIFY_AFTER_APPROVAL(HttpStatus.UNPROCESSABLE_ENTITY, "승인 후에는 수정할 수 없습니다."),

	/** 잘못된 상태 전환 (예: 대기 → 완료로 바로 변경) */
	INVALID_STATUS_TRANSITION(HttpStatus.UNPROCESSABLE_ENTITY, "잘못된 상태 전환입니다."),

	// 423 Locked
	/** 리소스가 잠김 (동시 수정 방지) */
	RESOURCE_LOCKED(HttpStatus.LOCKED, "다른 사용자가 수정 중인 리소스입니다."),

	// 429 Too Many Requests
	/** 요청 횟수 제한 초과 (Rate Limiting) */
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

	/** 로그인 시도 횟수 초과 */
	TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),

	/* ==================== 5XX SERVER ERROR ==================== */

	// 500 Internal Server Error
	/** 서버 내부 오류 (일반) */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요."),

	/** 데이터베이스 오류 (일반) */
	DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다."),

	/** DB 연결 실패 */
	DATABASE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 연결에 실패했습니다."),

	/** SQL 쿼리 실행 오류 */
	QUERY_EXECUTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "쿼리 실행 중 오류가 발생했습니다."),

	/** 트랜잭션 처리 오류 */
	TRANSACTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션 처리 중 오류가 발생했습니다."),

	/** 파일 업로드 실패 */
	FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다."),

	/** 파일 삭제 실패 */
	FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 중 오류가 발생했습니다."),

	/** 파일 읽기 실패 */
	FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 읽기 중 오류가 발생했습니다."),

	/** 이미지 처리 실패 (리사이징, 압축 등) */
	IMAGE_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 처리 중 오류가 발생했습니다."),

	/** 결제 처리 실패 */
	PAYMENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 오류가 발생했습니다."),

	/** 결제 검증 실패 */
	PAYMENT_VERIFICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 검증 중 오류가 발생했습니다."),

	/** 환불 처리 실패 */
	REFUND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "환불 처리 중 오류가 발생했습니다."),

	/** 이메일 전송 실패 */
	EMAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송 중 오류가 발생했습니다."),

	/** SMS 전송 실패 */
	SMS_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 전송 중 오류가 발생했습니다."),

	/** 암호화 처리 실패 */
	ENCRYPTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "암호화 처리 중 오류가 발생했습니다."),

	/** 복호화 처리 실패 */
	DECRYPTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "복호화 처리 중 오류가 발생했습니다."),

	/** JSON 파싱 실패 */
	JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 중 오류가 발생했습니다."),

	/** 데이터 타입 변환 실패 */
	DATA_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 변환 중 오류가 발생했습니다."),

	// 502 Bad Gateway
	/** 게이트웨이 오류 */
	BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "게이트웨이 오류가 발생했습니다."),

	/** 외부 API 호출 실패 */
	EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 호출 중 오류가 발생했습니다."),

	/** 결제 게이트웨이 (Toss Payments 등) 오류 */
	PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "결제 게이트웨이 오류가 발생했습니다."),

	/** 카카오맵 등 지도 API 오류 */
	MAP_API_ERROR(HttpStatus.BAD_GATEWAY, "지도 API 호출 중 오류가 발생했습니다."),

	// 503 Service Unavailable
	/** 서비스 일시 중단 */
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 일시적으로 사용할 수 없습니다."),

	/** 서비스 점검 중 */
	UNDER_MAINTENANCE(HttpStatus.SERVICE_UNAVAILABLE, "서비스 점검 중입니다."),

	/** DB 서버 다운 */
	DATABASE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "데이터베이스에 연결할 수 없습니다."),

	// 504 Gateway Timeout
	/** 게이트웨이 타임아웃 */
	GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "게이트웨이 시간 초과입니다."),

	/** 외부 API 응답 지연 */
	EXTERNAL_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "외부 API 응답 시간이 초과되었습니다.");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	/**
	 * HTTP 상태 코드를 반환합니다.
	 * 
	 * @return HttpStatus 객체
	 */
	public HttpStatus status() {
		return status;
	}

	/**
	 * 에러 메시지를 반환합니다.
	 * 
	 * @return 에러 메시지 문자열
	 */
	public String message() {
		return message;
	}

	/**
	 * HTTP 상태 코드 숫자를 반환합니다.
	 * 
	 * @return HTTP 상태 코드 (예: 200, 404, 500)
	 */
	public int code() {
		return status.value();
	}
}