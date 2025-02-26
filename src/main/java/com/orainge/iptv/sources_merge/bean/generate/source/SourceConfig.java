package com.orainge.iptv.sources_merge.bean.generate.source;

import com.orainge.tools.network.url.enums.UrlType;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * IPTV 直播源
 */
@Data
public class SourceConfig {
    /**
     * 直播源 ID
     */
    private String id;

    /**
     * 直播源名称
     */
    private String name;

    /**
     * 直播源所属分类（部分匹配，*表示所有分类均可用）
     */
    private List<String> category;

    /**
     * 需要从该直播源抽取的频道名称（部分匹配，*表示所有分类均可用）
     */
    private List<String> channel;

    /**
     * 请求该直播源时是否启用代理
     */
    private Boolean enableProxy;

    /**
     * 直播源 URL
     */
    private String url;

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
     * 命中条件：最终使用的直播源的 URL 类型<br>
     * 内部使用
     */
    private List<UrlType> finallyEnableUrlType = null;

    /**
     * 直播源数据<br>
     * 频道名称无序，直播源URL有序
     */
    private ChannelData channelData = new ChannelData();

    /**
     * 设置过滤的频道
     */
    public void setChannel(List<String> channel) {
        this.channel = channel;
        channelData.setIncludeChannelNameList(channel);
    }

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
