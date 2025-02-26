package com.orainge.iptv.sources_merge.service;

import com.orainge.iptv.sources_merge.bean.generate.GenerateConfig;
import com.orainge.iptv.sources_merge.bean.generate.category.ChannelSourceItem;
import com.orainge.iptv.sources_merge.bean.generate.proxy.ProxyFilterConfig;
import com.orainge.iptv.sources_merge.bean.generate.source.SourceConfig;

import java.util.LinkedHashSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 请求文件服务
 */
public interface RequestService {
    boolean requestSourceFile(GenerateConfig generateConfig, SourceConfig sourceConfig);

    boolean doConnectionCheck(GenerateConfig generateConfig,
                              ChannelSourceItem channelSourceItem,
                              CopyOnWriteArraySet<ChannelSourceItem> channelSourceItems,
                              String categoryName,
                              String channelName);

    boolean getProxyUrl(GenerateConfig generateConfig,
                        String categoryName,
                        String channelName,
                        ProxyFilterConfig filterConfig,
                        ChannelSourceItem channelSourceItem);
}
