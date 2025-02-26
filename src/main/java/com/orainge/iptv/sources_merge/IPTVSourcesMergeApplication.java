package com.orainge.iptv.sources_merge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.util.Objects;

@SpringBootApplication
@ComponentScan({"com.orainge.tools.*", "com.orainge.iptv.sources_merge.*"})
@Slf4j
public class IPTVSourcesMergeApplication extends SpringBootServletInitializer {
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        log.debug("NOTE: DEBUG ENABLED.");
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource[]{
                new ClassPathResource("server-config.yml")
        });
        pspc.setProperties(Objects.requireNonNull(yaml.getObject()));
        return pspc;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(IPTVSourcesMergeApplication.class);
    }

    public static void main(String[] args) {
        // 启动主程序
        SpringApplicationBuilder builder = new SpringApplicationBuilder(IPTVSourcesMergeApplication.class);
        builder.run(args);
    }
}
