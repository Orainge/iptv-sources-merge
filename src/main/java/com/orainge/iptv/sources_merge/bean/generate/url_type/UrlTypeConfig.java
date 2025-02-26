package com.orainge.iptv.sources_merge.bean.generate.url_type;

import com.orainge.tools.network.dns.consts.DNSType;
import com.orainge.tools.network.url.enums.UrlType;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * URL 过滤配置
 */
@Data
public class UrlTypeConfig {
    /**
     * IPV4 地址
     */
    private ConfigItem v4;

    /**
     * IPV6 地址
     */
    private ConfigItem v6;

    /**
     * 域名
     */
    private ConfigItem domain;

    /**
     * 是否启用域名 IP 类型检测
     */
    private Boolean enableDomainCheckIpType;

    /**
     * 单栈的 IP 类型
     */
    private UrlType singleUrlType;

    /**
     * 单栈的 DNS 类型
     */
    private DNSType singleDnsType;

    /**
     * 获取启用的 URL 类型
     */
    public List<UrlType> getEnableUrlTypeList() {
        List<UrlType> urlTypeList = new LinkedList<>();

        // 启用 IPV4
        if (v4.isEnable()) {
            urlTypeList.add(UrlType.IPV4);
        }

        // 启用 IPV6
        if (v6.isEnable()) {
            urlTypeList.add(UrlType.IPV6);
        }

        // 启用域名
        if (domain.isEnable()) {
            urlTypeList.add(UrlType.DOMAIN);
        }

        return urlTypeList;
    }

    /**
     * 是否启用域名 IP 类型检测
     */
    public boolean isEnableDomainCheckIpType() {
        if (enableDomainCheckIpType == null) {
            // 计算是否启用
            // 当且仅当启用单栈的时候才需要检测
            if (domain.isCheckIpType()) {
                boolean b1 = v4.isEnable();
                boolean b2 = v6.isEnable();
                if (b1 && !b2) {
                    // 启用了单栈 IPV4
                    enableDomainCheckIpType = true;
                    singleDnsType = DNSType.A;
                    singleUrlType = UrlType.IPV4;
                } else if (!b1 && b2) {
                    // 启用了单栈 IPV6
                    enableDomainCheckIpType = true;
                    singleDnsType = DNSType.AAAA;
                    singleUrlType = UrlType.IPV6;
                } else {
                    enableDomainCheckIpType = false;
                }
            } else {
                enableDomainCheckIpType = false;
            }
        }

        return enableDomainCheckIpType;
    }
}