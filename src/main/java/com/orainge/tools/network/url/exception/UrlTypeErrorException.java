package com.orainge.tools.network.url.exception;

/**
 * URL 类型错误
 */
public class UrlTypeErrorException extends RuntimeException {
    public UrlTypeErrorException() {
        super("URL 类型错误");
    }

    public UrlTypeErrorException(String message) {
        super("URL 类型错误 [" + message + "]");
    }
}
