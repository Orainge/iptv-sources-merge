package com.orainge.iptv.sources_merge.bean.generate.proxy;

import com.orainge.tools.network.url.enums.UrlType;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * 代理的过滤配置
 */
@Data
public class ProxyFilterConfig {
    /**
     * 命中条件：频道 URL 类型（全匹配）
     */
    private List<String> urlType;

    /**
     * 命中条件：频道 URL 类型实体对象<br>
     * 内部使用
     */
    private List<UrlType> urlTypeList;

    /**
     * 命中条件：频道分类（部分匹配）（*表示全部命中）
     */
    private List<String> category;

    /**
     * 命中条件：频道名称（部分匹配）（*表示全部命中）
     */
    private List<String> channelName;

    /**
     * 命中条件：频道URL关键字（部分匹配）（*表示全部命中）
     */
    private List<String> channelUrl;

    /**
     * 命中条件：是否启用外部代理
     */
    private Boolean externalProxy;

    /**
     * API 配置（如果有自定义的优先使用自定义的配置，否则使用全局默认配置）
     */
    private ProxyApiConfig api;

    /**
     * 外部调用：设置URL类型
     */
    public void setUrlType(List<String> urlTypes) {
        this.urlType = urlTypes;

        // 添加 UrlType 实体类
        if (urlTypeList == null) {
            urlTypeList = new LinkedList<>();
        }

        for (String urlType : urlTypes) {
            urlTypeList.add(com.orainge.tools.network.url.enums.UrlType.getUrlType(urlType));
        }
    }
}