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
		super("转换实体类["+clz.getName()+"]："+message);
		
	}
}
