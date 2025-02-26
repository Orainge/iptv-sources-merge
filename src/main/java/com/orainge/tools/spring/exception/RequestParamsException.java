package com.orainge.tools.spring.exception;

/**
 * 请求参数错误 异常
 */
public class RequestParamsException extends RuntimeException {
    public RequestParamsException() {
        super("参数错误");
    }

    public RequestParamsException(String message) {
        super("参数错误: " + message);
    }

    public RequestParamsException(Throwable e) {
        super("参数错误: " + e.getMessage(), e);
    }

    public RequestParamsException(String message, Throwable e) {
        super("参数错误: " + message, e);
    }
}
