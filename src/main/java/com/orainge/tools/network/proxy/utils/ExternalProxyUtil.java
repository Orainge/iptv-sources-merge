package com.orainge.tools.network.proxy.utils;

import cn.hutool.http.HttpRequest;
import com.orainge.tools.network.proxy.bean.ProxyServerInfo;
import com.orainge.tools.network.dns.DNSManager;
import com.orainge.tools.network.dns.consts.DNSType;
import com.orainge.tools.network.request.exception.NoExternalProxyException;
import com.orainge.tools.network.url.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

/**
 * 外部代理工具类
 */
@Slf4j
public class ExternalProxyUtil {
    public static List<ProxyServerInfo> serverList = null;

    /**
     * 设置 Host 请求头（用自己实现的 DNS 查询记录）
     *
     * @return 新的 URL (需要将域名替换成 IP，在 Host 里设置域名)
     */
    public static String setHostHeader(HttpRequest httpRequest, String url) throws Exception {
        URL u = new URL(url);
        String domain = u.getHost();

        if (UrlUtils.isValidIPAddress(domain)) {
            // 如果是 IP 地址，就直接返回原值，不需要修改
            return url;
        }

        String ipAddress = DNSManager.query(domain, DNSType.A).getValue();
        httpRequest.header("Host", domain);
        return url.replace(domain, ipAddress); // 将原来 URL 中的域名替换成 IP 地址
    }

    /**
     * 设置 Host 请求头（用自己实现的 DNS 查询记录）
     *
     * @return 新的 URL (需要将域名替换成 IP，在 Host 里设置域名)
     */
    public static HttpURLConnection getProxyHttpURLConnection(String url) throws Exception {
        // 查找 domain
        URL u = new URL(url);
        String domain = u.getHost();

        // 如果是 IP 地址，就直接新建连接，不需要查询 DNS
        if (UrlUtils.isValidIPAddress(domain)) {
            return (HttpURLConnection) u.openConnection(ExternalProxyUtil.getProxy(url));
        }

        // 查询 IP 地址
        String ipAddress = DNSManager.query(domain, DNSType.A).getValue();

        // 获取 Proxy 对象 (用原来的 URL 获取代理)
        Proxy proxy = ExternalProxyUtil.getProxy(url);

        // 将原来 URL 中的域名替换成 IP 地址
        url = url.replace(domain, ipAddress);

        // 构建并返回 HttpURLConnection 对象
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(proxy);
        conn.addRequestProperty("Host", domain);
        return conn;
    }

    /**
     * 获取代理信息 Proxy 对象
     */
    public static Proxy getProxy(String url) {
        if (serverList == null) {
            throw new NoExternalProxyException();
        }

        for (ProxyServerInfo proxyServerInfo : serverList) {
            if (proxyServerInfo.isHit(url)) {
                // 找到该 URL 可以使用的代理
                return new Proxy(proxyServerInfo.getType(),
                        InetSocketAddress.createUnresolved(
                                proxyServerInfo.getUrl(),
                                proxyServerInfo.getPort()
                        )
                );
            }
        }

        // 没有找到可以使用的代理，抛出异常
        throw new NoExternalProxyException(url);
    }
}
