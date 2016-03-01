package com.bing.excel.exception;

public class MissingCellConfigException extends RuntimeException {

	public MissingCellConfigException() {
		super();
	}

	public MissingCellConfigException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MissingCellConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingCellConfigException(String message) {
		super(message);
	}

	public MissingCellConfigException(Throwable cause) {
		super(cause);
	}

}
