package com.orainge.tools.network.proxy;

import com.orainge.tools.network.proxy.config.ExternalProxyConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类
 */
@Configuration
public class NetworkProxyAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ExternalProxyConfig.class)
    @ConfigurationProperties(prefix = "server-config.external-proxy", ignoreInvalidFields = true)
    public ExternalProxyConfig getExternalProxyConfig() {
        return new ExternalProxyConfig();
    }
}
