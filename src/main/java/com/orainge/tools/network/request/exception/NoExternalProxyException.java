package com.orainge.tools.network.request.exception;

/**
 * 没有配置外部代理异常
 */
public class NoExternalProxyException extends RuntimeException {
    public NoExternalProxyException() {
        super("没有配置外部代理");
    }

    public NoExternalProxyException(String url) {
        super("没有为该 url [" + url + "] 配置外部代理");
    }
}
