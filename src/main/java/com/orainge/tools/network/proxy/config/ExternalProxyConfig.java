package com.orainge.tools.network.proxy.config;

import com.orainge.tools.network.proxy.bean.ProxyServerInfo;
import com.orainge.tools.network.proxy.utils.ExternalProxyUtil;
import lombok.Data;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 外部代理设置
 */
@Data
public class ExternalProxyConfig {
    /**
     * 服务器信息
     */
    private List<ProxyServerInfo> serverList;

    @PostConstruct
    public void init() {
        if (serverList != null) {
            ExternalProxyUtil.serverList = serverList;
        }
    }
}
