package com.orainge.iptv.sources_merge.bean.generate.proxy;

import cn.hutool.json.JSONUtil;
import lombok.Data;

/**
 * 外部代理 API 配置
 */
public class ProxyApiConfig {
    /**
     * URL 名称
     */
    private String url;

    /**
     * 请求方式字符串
     */
    private String method;

    /**
     * API 需要 POST 的参数<br>
     * 遇到 "${xxx}" 的参数表示使用系统预留参数替换<br>
     */
    private ProxyApiDataConfig data;

    /**
     * 获取 API 需要 POST 的参数<br>
     * 使用拷贝复制一份新的数据
     */
    public ProxyApiDataConfig getApiData() {
        ProxyApiDataConfig apiData = new ProxyApiDataConfig();
        apiData.putAll(data);
        return apiData;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethodName() {
        return method;
    }

    public void setData(ProxyApiDataConfig data) {
        this.data = data;
    }
}