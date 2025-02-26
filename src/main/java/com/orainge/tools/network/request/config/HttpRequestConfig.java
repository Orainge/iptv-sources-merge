package com.orainge.tools.network.request.config;

import lombok.Data;

import javax.annotation.PostConstruct;

/**
 * 请求配置
 */
@Data
public class HttpRequestConfig {
    /**
     * 跳过 SSL 证书验证
     */
    private Boolean skipSslVerification;

    /**
     * 连接失败后重试次数
     */
    private Integer connectRetryTimes = 3;

    /**
     * 读取超时时间（秒）
     */
    private Integer readTimeout = 10;

    /**
     * 连接超时时间（秒）
     */
    private Integer connectTimeout = 60;

    /**
     * 最大重定向次数
     */
    private Integer maxRequestRedirect = 5;

    /**
     * 请求 USER-AGENT
     */
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";

    /**
     * 缓冲区大小 (kb)
     */
    private int bufferSize = 16;

    @PostConstruct
    public void init() {
        if (readTimeout != null) {
            this.readTimeout = this.readTimeout * 1000;
        }

        if (connectTimeout != null) {
            this.connectTimeout = this.connectTimeout * 1000;
        }
    }

    public void setUserAgent(String userAgent) {
        if (userAgent != null && !"".equals(userAgent)) {
            this.userAgent = userAgent;
        }
    }
}
