package com.orainge.iptv.sources_merge.bean.generate.category;

import lombok.Data;

import java.util.List;

/**
 * 频道分类配置
 */
@Data
public class CategoryConfig {
    /**
     * 分类名称
     */
    private String name;

    /**
     * 频道配置列表
     */
    private List<ChannelConfigItem> list;
}
