package com.orainge.iptv.sources_merge.vo;

import com.orainge.iptv.sources_merge.bean.generate.GenerateConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请求生成直播源 TXT 文件参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateTxtFileParams extends ApiParams {
    /**
     * 配置文件 URL
     */
    private String configUrl;

    /**
     * 是否启用代理获取配置文件<br>
     * 仅配置 configUrl 时生效
     */
    private Boolean enableConfigUrlProxy;

    /**
     * 配置文件 JSON 字符串<br>
     * 优先级最高
     */
    private GenerateConfig config;
}
