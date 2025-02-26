package com.orainge.tools.network.proxy.bean;

import lombok.Data;

import java.net.Proxy;
import java.util.Set;

@Data
public class ProxyServerInfo {
    /**
     * 代理服务器地址
     */
    private String url;

    /**
     * 代理服务器端口
     */
    private int port;

    /**
     * 代理类型 (http/socks)
     */
    private Proxy.Type type;

    /**
     * 如果为 null，表示任意请求都接受
     */
    private Set<String> host;

    /**
     * 是否命中
     */
    public boolean isHit(String url) {
        if (host == null) {
            return true;
        }

        try {
            for (String hostString : host) {
                if (url.contains(hostString)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void setType(String type) throws Exception {
        if ("socks".equals(type) ||
                "socks4".equals(type) ||
                "socks5".equals(type)) {
            this.type = Proxy.Type.SOCKS;
        } else if ("http".equals(type)) {
            this.type = Proxy.Type.HTTP;
        } else {
            throw new Exception("未知的代理类型 - [" + type + "]");
        }
    }

    public void setHost(Set<String> host) {
        // null / 空 / 全匹配，不初始化
        if (host == null || host.isEmpty() || host.contains("*")) {
            host = null;
        }
        this.host = host;
    }
}
