package com.orainge.tools.network.url.enums;

import com.orainge.tools.network.url.exception.UrlTypeConfigErrorException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * URL 类型
 */
public enum UrlType {
    IPV4("IPV4", "IPV4 地址", "ipv4", "IPV4", "v4", "V4"),
    IPV6("IPV6", "IPV6 地址", "ipv4", "IPV6", "v6", "V6"),
    DOMAIN("DOMAIN", "域名", "domain", "DOMAIN");

    private final String name;
    private final String description;
    private final Set<String> configKeySet;

    UrlType(String name, String description, String... configKeys) {
        this.name = name;
        this.description = description;
        configKeySet = new HashSet<>(Arrays.asList(configKeys));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getConfigKeySet() {
        return configKeySet;
    }

    /**
     * 根据配置字符串获取 UrlType
     */
    public static UrlType getUrlType(String configKey) {
        for (UrlType value : UrlType.values()) {
            Set<String> configKeySet = value.getConfigKeySet();
            if (configKeySet.contains(configKey)) {
                return value;
            }
        }
        throw new UrlTypeConfigErrorException("不存在 URL 类型: " + configKey);
    }
}
