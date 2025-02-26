package com.orainge.tools.network.request;

import com.orainge.tools.network.request.config.HttpRequestConfig;
import com.orainge.tools.network.request.utils.HttpRequestUtil;
import com.orainge.tools.network.request.utils.RequestFileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类
 */
@Configuration
public class NetworkRequestAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(HttpRequestConfig.class)
    @ConfigurationProperties(prefix = "server-config.request", ignoreInvalidFields = true)
    public HttpRequestConfig getHttpRequestConfig() {
        return new HttpRequestConfig();
    }

    @Bean
    @ConditionalOnMissingBean(HttpRequestUtil.class)
    public HttpRequestUtil getHttpRequestUtil(HttpRequestConfig httpRequestConfig) throws Exception {
        return new HttpRequestUtil(httpRequestConfig);
    }

    @Bean
    @ConditionalOnMissingBean(RequestFileUtil.class)
    public RequestFileUtil getRequestFileUtil(HttpRequestUtil httpRequestUtil) {
        return new RequestFileUtil(httpRequestUtil);
    }
}
