package com.orainge.tools.network.dns;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.orainge.tools.network.dns.bean.DNSRecord;
import com.orainge.tools.network.dns.consts.DNSType;
import com.orainge.tools.network.dns.exception.DnsQueryException;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * DNS 管理器
 */
@Slf4j
public class DNSManager {
    private static final String keySeparator = "\n";

    /**
     * 静态 Host 地址 Map
     */
    private static final Map<String, DNSRecord> HOST_MAP = new HashMap<>();

    private static final LoadingCache<String, DNSRecord> dnsRecords = CacheBuilder.newBuilder()
            .initialCapacity(10) //设置缓存容器的初始容量为10
            .maximumSize(30) // 最大缓存数量
            .concurrencyLevel(5) //设置并发级别为8，并发级别是指可以同时写缓存的线程数
            .expireAfterWrite(1, TimeUnit.HOURS) // 数据写入多久之后就会过期
            .build(new CacheLoader<String, DNSRecord>() {
                @Override
                public DNSRecord load(String key) {
                    try {
                        // 查看有没有预先配置
                        DNSRecord host = HOST_MAP.get(key);
                        if (host != null) {
                            return host;
                        }

                        String[] keys = key.split(keySeparator);
                        String domain = keys[0];
                        String type = keys[1];
                        return DNSQuery.query(domain, Objects.requireNonNull(DNSType.getByType(type)));
                    } catch (Exception e) {
                        log.error("DNS 查询错误", e);
                        return null;
                    }
                }
            });

    /**
     * 通过 URL 查询 DNS 记录
     *
     * @param url     URL 地址
     * @param dnsType DNS 类型
     */
    public static DNSRecord queryByUrl(String url, DNSType dnsType) {
        String domain;

        try {
            URL u = new URL(url);
            domain = u.getHost();
        } catch (Exception e) {
            throw new DnsQueryException(e);
        }

        return query(domain, dnsType);
    }

    /**
     * 查询 DNS 记录
     *
     * @param domain  域名
     * @param dnsType DNS 类型
     */
    public static DNSRecord query(String domain, DNSType dnsType) {
        try {
            String key = domain + keySeparator + dnsType.getType();
            DNSRecord record = dnsRecords.get(key);

            if (record != null) {
                // 缓存失效
                if (record.isExpired()) {
                    dnsRecords.invalidate(key);
                    // 重新查询
                    record = dnsRecords.get(key);
                } else {
                    // 更新 ttl
                    record.updateTTL();
                }
            }

            return record;
        } catch (Exception e) {
            throw new DnsQueryException(e);
        }
    }

    /**
     * 刷新缓存
     */
    public static void refresh() {
        dnsRecords.invalidateAll();
    }
}