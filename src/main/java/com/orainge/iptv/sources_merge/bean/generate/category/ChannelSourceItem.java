package com.orainge.iptv.sources_merge.bean.generate.category;

import com.orainge.tools.network.url.enums.UrlType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * 频道直播源项
 */
@Data
@Accessors(chain = true)
public class ChannelSourceItem {
    /**
     * 直播源 URL
     */
    private String url;

    /**
     * 直播源 URL 类型
     */
    private UrlType urlType;

    /**
     * 直播源 URL IP 类型
     */
    private UrlType urlIpType;

    @Override
    public String toString() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelSourceItem that = (ChannelSourceItem) o;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
