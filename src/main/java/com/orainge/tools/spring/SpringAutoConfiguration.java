package com.orainge.tools.spring;

import com.orainge.tools.spring.utils.ApplicationContextUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类
 */
@Configuration
public class SpringAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ApplicationContextUtils.class)
    public ApplicationContextUtils getApplicationContextUtils(ApplicationContext applicationContext) {
        return new ApplicationContextUtils(applicationContext);
    }
}
