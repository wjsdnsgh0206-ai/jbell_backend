package jbell.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException{
	
	private static final long serialversionUID = 1L;
	
	private final ErrorCode errorCode;
	
	public BaseException(ErrorCode errorCode) {
		super(errorCode.message());
		this.errorCode = errorCode;
	}


}
