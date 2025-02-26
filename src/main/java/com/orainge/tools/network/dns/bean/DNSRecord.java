package com.orainge.tools.network.dns.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.orainge.tools.network.dns.consts.DNSType;
import com.orainge.tools.network.dns.serializer.DNSTypeSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DNS 记录
 */
@Data
@Accessors(chain = true)
public class DNSRecord {
    /**
     * 类型
     */
    @JsonProperty("type")
    @JsonSerialize(using = DNSTypeSerializer.class)
    private DNSType dnsType;

    /**
     * 域名
     */
    private String domain;

    /**
     * 记录值
     */
    private String value;

    /**
     * TTL
     */
    private Integer ttl;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Long expireTimeStamp;

    public DNSRecord() {
    }

    public DNSRecord(DNSType dnsType, String domain, String value, int ttl) {
        this.domain = domain;
        this.dnsType = dnsType;
        this.value = value;
        this.ttl = ttl;
        this.expireTimeStamp = System.currentTimeMillis() + ttl * 1000L;
    }

    /**
     * 创建静态记录
     */
    public static DNSRecord buildStaticRecord(String domain, String value) {
        return new DNSRecord()
                .setDomain(domain)
                .setValue(value)
                .setDnsType(DNSType.A);
    }

    /**
     * 该记录是否过期
     */
    @JsonIgnore
    public boolean isExpired() {
        // 未设置过期时间，默认不过期
        if (expireTimeStamp == null) {
            return false;
        }
        return expireTimeStamp - System.currentTimeMillis() < 0;
    }

    /**
     * 更新 TTL 时间
     */
    public void updateTTL() {
        if (ttl == null || expireTimeStamp == null) {
            // 未设置过期时间，不更新
            return;
        }

        long newTTL = (expireTimeStamp - System.currentTimeMillis()) / 1000L;
        if (newTTL < 0) {
            newTTL = 0;
        }
        this.ttl = (int) newTTL;
    }

    @Override
    public String toString() {
        return "{" +
                "\"domain\":\"" + domain + "\"," +
                "\"dnsType\":\"" + dnsType + "\"," +
                "\"value\":\"" + value + "\"," +
                "\"ttl\":" + ttl +
                "}";
    }
}