package com.orainge.iptv.sources_merge.vo;

import lombok.Data;

/**
 * API 参数
 */
@Data
public class ApiParams {
    /**
     * 请求 Token
     */
    private String token;

    /**
     * 参数是否为空
     */
    public boolean isEmpty() {
        return token == null || "".equals(token);
    }

    /**
     * 检验请求的 token 是否正确
     */
    public boolean isValidToken(String configToken) {
        // 未配置 token，不校验
        if (configToken == null || "".equals(configToken)) {
            return true;
        }

        // 检验请求的 Token 是否与配置的 token 一致
        return configToken.equals(token);
    }
}
