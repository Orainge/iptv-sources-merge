package com.orainge.iptv.sources_merge.bean.generate.source;

import com.orainge.tools.common.utils.KeywordRegExrUtils;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 直播源频道数据
 */
@Accessors(chain = true)
public class ChannelData extends HashMap<String, LinkedHashSet<String>> {
    /**
     * 需要从该直播源抽取的频道名称（部分匹配，*表示所有分类均可用）
     */
    private List<String> includeChannelNameList = null;

    private boolean enableChannelFilter = false;

    public void setIncludeChannelNameList(List<String> includeChannelNameList) {
        this.includeChannelNameList = includeChannelNameList;
        this.enableChannelFilter = includeChannelNameList != null && !includeChannelNameList.isEmpty();
    }

    /**
     * 从直播源文本中创建
     *
     * @param lineStr 一行文本
     */
    public void addChannelByLine(String lineStr) {
        try {
            String[] strs = lineStr.split(",");
            addChannel(strs[0], strs[1]);
        } catch (Exception ignore) {
        }
    }

    /**
     * 添加频道直播源
     *
     * @param channelName 频道名称
     * @param url         直播源
     */
    public void addChannel(String channelName, String url) {
        // 过滤频道或者 URL 为空的项目
        if (channelName.isEmpty() || url.isEmpty()) {
            return;
        }

        // 未开启频道过滤 || （开启频道过滤 && 命中条件）
        if (!enableChannelFilter || KeywordRegExrUtils.isHintString(includeChannelNameList, channelName)) {
            // 获取对应频道的 URL 集合
            LinkedHashSet<String> urlSet = computeIfAbsent(channelName, (k) -> new LinkedHashSet<>());

            // 将 url 中的 80 和 443 端口去掉
            url = url.replace(":80/", "/");
            url = url.replace(":443/", "/");

            // 这里不用做 URL 合法性校验，因为后续添加流程会根据 URL 类型过滤

            // 添加 URL
            urlSet.add(url);
        }
    }
}
