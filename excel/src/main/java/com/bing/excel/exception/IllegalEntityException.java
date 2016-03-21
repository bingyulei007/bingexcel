package com.bing.excel.exception;

public class IllegalEntityException extends RuntimeException {
	@Override
	public String getMessage() {
		return super.getMessage();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public IllegalEntityException(Class<?> clz,String message) {
		super("实体类["+clz.getName()+"]："+message);
		
	}

	public IllegalEntityException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
