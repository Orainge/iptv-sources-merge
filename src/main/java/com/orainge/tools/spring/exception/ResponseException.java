package com.orainge.tools.spring.exception;

/**
 * 请求结果错误 异常
 */
public class ResponseException extends RuntimeException {
    public ResponseException() {
        super("错误");
    }

    public ResponseException(String message) {
        super("错误: " + message);
    }

    public ResponseException(Throwable e) {
        super("错误: " + e.getMessage(), e);
    }

    public ResponseException(String message, Throwable e) {
        super("错误: " + message, e);
    }
}
