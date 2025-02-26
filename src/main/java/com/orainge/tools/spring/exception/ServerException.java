package com.orainge.tools.spring.exception;

/**
 * 服务器错误
 */
public class ServerException extends RuntimeException {
    public ServerException() {
        super("内部错误");
    }

    public ServerException(String message) {
        super("内部错误: " + message);
    }

    public ServerException(Throwable e) {
        super("内部错误: " + e.getMessage(), e);
    }

    public ServerException(String message, Throwable e) {
        super("内部错误: " + message, e);
    }
}
