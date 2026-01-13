package jbell.exception;

import lombok.Getter;

@Getter
public class CustomException extends BaseException{
	
	private static final long serialVersionUID = 1L;

	public CustomException(ErrorCode errorCdoe) {
		super(errorCdoe);
	}
}
