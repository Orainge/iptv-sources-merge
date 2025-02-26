package com.orainge.tools.spring.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextUtils implements ApplicationContextAware {
    public static ApplicationContext applicationContext;

    public ApplicationContextUtils(ApplicationContext applicationContext){
        ApplicationContextUtils.applicationContext = applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.applicationContext = applicationContext;
    }

    public static Object getBean(String name) {
        return applicationContext == null ? null : applicationContext.getBean(name);
    }

    public static <T> T getBeanByClass(Class<T> clazz) {
        return applicationContext == null ? null : applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext == null ? null : applicationContext.getBean(name, clazz);
    }
}
