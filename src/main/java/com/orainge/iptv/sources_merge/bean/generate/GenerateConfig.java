package com.orainge.iptv.sources_merge.bean.generate;

import com.orainge.iptv.sources_merge.bean.generate.category.CategoryConfig;
import com.orainge.iptv.sources_merge.bean.generate.proxy.ProxyConfig;
import com.orainge.iptv.sources_merge.bean.generate.source.SourceConfig;
import com.orainge.iptv.sources_merge.bean.generate.url_type.UrlTypeConfig;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 生成最终直播源的配置类
 */
@Data
public class GenerateConfig {
    /**
     * 配置文件名称
     */
    private String name;

    /**
     * 生成的文件名称
     */
    private String fileName;

    /**
     * 直播源配置文件 URL
     */
    private String sourceConfigUrl;

    /**
     * 节目分类配置文件 URL
     */
    private String categoryConfigUrl;

    /**
     * 直播源配置<br>
     * 由 [直播源配置文件 URL] 获取得到 || 直接提交
     */
    private List<SourceConfig> sourceConfig;

    /**
     * 节目分类配置<br>
     * 由 [节目分类配置文件 URL] 获取得到 || 直接提交
     */
    private List<CategoryConfig> categoryConfig;

    /**
     * 是否启用代理获取直播源配置文件<br>
     * 仅配置 sourceConfigUrl 时生效
     */
    private Boolean enableSourceConfigUrlProxy;

    /**
     * 是否启用代理获取节目分类配置文件<br>
     * 仅配置 categoryConfigUrl 时生效
     */
    private Boolean enableCategoryConfigUrlProxy;

    /**
     * 排除直播源 URL 关键字<br>
     * 如果 URL 里面出现以下关键字，将被排除
     */
    private List<String> excludeSourceUrlKeyword;

    /**
     * 是否检查连接性
     */
    private Boolean checkConnection;

    /**
     * 排除检查连接性的直播源 URL 关键字<br>
     * 如果 URL 里面出现以下关键字，将不会检测连接性
     */
    private List<String> excludeCheckConnectionUrlKeyword;

    /**
     * 直播源 URL 过滤类型
     */
    private UrlTypeConfig urlType;

    /**
     * 直播源地址代理配置
     */
    private ProxyConfig proxy;

    /**
     * 判断参数是否为空
     */
    public boolean isEmpty() {
        return StringUtils.isEmpty(name) ||
                StringUtils.isEmpty(fileName) ||
                (StringUtils.isEmpty(sourceConfigUrl) || (sourceConfig == null || sourceConfig.isEmpty())) ||
                (StringUtils.isEmpty(categoryConfigUrl) || (categoryConfig == null || categoryConfig.isEmpty()));
    }
}