package com.bing.excel.exception;

public class UnknownException extends RuntimeException{
    public UnknownException( Throwable cause) {
        super("未知异常", cause);
    }


}
