package com.orainge.tools.network.url.exception;

/**
 * URL 类型配置错误
 */
public class UrlTypeConfigErrorException extends RuntimeException {
    public UrlTypeConfigErrorException() {
        super("URL 类型配置错误");
    }

    public UrlTypeConfigErrorException(String message) {
        super("URL 类型配置错误 [" + message + "]");
    }
}
