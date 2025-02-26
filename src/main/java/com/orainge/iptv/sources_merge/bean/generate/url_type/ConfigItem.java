package com.orainge.iptv.sources_merge.bean.generate.url_type;

import lombok.Data;

/**
 * URL 类型配置
 */
@Data
public class ConfigItem {
    /**
     * 是否启用
     */
    private boolean enable;

    /**
     * 是否检查IP类型（使用DNS解析，仅域名时生效）
     */
    private boolean checkIpType;
}