package com.orainge.iptv.sources_merge.bean.generate.proxy;

import com.orainge.iptv.sources_merge.bean.generate.category.ChannelSourceItem;
import com.orainge.tools.common.utils.KeywordRegExrUtils;
import com.orainge.tools.common.utils.KeywordUtils;
import lombok.Data;

import java.util.List;

/**
 * 外部代理配置
 */
@Data
public class ProxyConfig {
    /**
     * 是否启用外部代理
     */
    private boolean enable;

    /**
     * API 配置
     */
    private ProxyApiConfig api;

    /**
     * 代理配置
     */
    private List<ProxyFilterConfig> filter;

    /**
     * 判断是否需要代理
     *
     * @param categoryName      频道分类名称
     * @param channelName       频道名称
     * @param channelSourceItem 频道源配置
     * @return null: 不需要代理 非 null: 满足代理条件的 Filter
     */
    public ProxyFilterConfig getProxyFilter(String categoryName, String channelName, ChannelSourceItem channelSourceItem) {
        if (filter == null || filter.isEmpty()) {
            // 没有配置 filter，返回
            return null;
        }

        // filter 进行校验
        for (ProxyFilterConfig filterConfig : filter) {
            if (
                // URL 类型（全匹配）
                // 这里不用考虑是否启用域名IP类型检测，因为如果不检测，urlIpType = urlType
                    (KeywordUtils.isHintFull(filterConfig.getUrlType(), channelSourceItem.getUrlType().getConfigKeySet()) ||
                            KeywordUtils.isHintFull(filterConfig.getUrlType(), channelSourceItem.getUrlIpType().getConfigKeySet())) &&
                            // 频道分类名称（部分匹配）
                            KeywordRegExrUtils.isHintString(filterConfig.getCategory(), categoryName) &&
                            // 频道名称（部分匹配）
                            KeywordRegExrUtils.isHintString(filterConfig.getChannelName(), channelName) &&
                            // 频道URL（部分匹配）
                            KeywordRegExrUtils.isHintString(filterConfig.getChannelUrl(), channelSourceItem.getUrl())
            ) {
                // Hint 条件命中

                // 这里需要赋值默认的 apiConfig 到 filter 里面，这样外面可以直接调用
                if (filterConfig.getApi() == null) {
                    filterConfig.setApi(api);
                }

                // 返回配置类
                return filterConfig;
            }
        }

        // 找不到符合条件的 filter 配置，返回 null
        return null;
    }
}