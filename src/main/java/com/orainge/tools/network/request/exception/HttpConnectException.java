package com.orainge.tools.network.request.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP 请求错误
 */
public class HttpConnectException extends RuntimeException {
    public HttpConnectException() {
        super("请求失败");
    }

    public HttpConnectException(String url) {
        super("请求失败 [" + url + "]");
    }

    public HttpConnectException(String url, int statusCode, String body) {
        super("[请求错误 (" + statusCode + ")] - " + url + ": " + body);
    }

    public static void log(int connectTimes, String url, String message, Class<?> clazz) {
        Logger log = LoggerFactory.getLogger(clazz);
        log.error("[请求错误(第 {} 次)] - {} - {}", connectTimes, url, message);
    }
}
