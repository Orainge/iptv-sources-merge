package com.orainge.tools.network.dns;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.orainge.tools.network.dns.bean.DNSRecord;
import com.orainge.tools.network.dns.consts.DNSType;
import com.orainge.tools.network.dns.exception.DnsQueryException;
import com.orainge.tools.network.dns.exception.NoDnsRecordException;
import com.orainge.tools.network.url.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * DNS 查询<br>
 * 通过 DOH 实现<br>
 * https://developers.cloudflare.com/1.1.1.1/encryption/dns-over-https/make-api-requests/dns-json/
 */
@Slf4j
public class DNSQuery {
    /**
     * 查询 DNS 记录
     *
     * @param domain  域名
     * @param dnsType DNS 类型
     */
    public static DNSRecord query(String domain, DNSType dnsType) {
        // 构建 DoH 请求
        String url = getDohUrl() + "?name=" + domain + "&type=" + dnsType.getType();
        log.debug("[DNS 查询] - {}", url);
        HttpRequest httpRequest = HttpRequest.get(url)
                .setReadTimeout(10 * 1000) // 设置读取时长
                .setConnectionTimeout(60 * 1000)  // 设置连接时长
                .header("accept", "application/dns-json")
                .setMethod(Method.GET);

        // 执行请求
        HttpResponse result;

        try {
            result = httpRequest.execute();
        } catch (Exception e) {
            throw new DnsQueryException(e);
        }

        // 检查请求是否正常
        int status = result.getStatus();
        String bodyStr = result.body();
        if (status != 200) {
            throw new DnsQueryException("查询错误 [" + status + "] - " + bodyStr);
        }

        // 转换 Body
        JSONObject body;
        try {
            body = JSONUtil.parseObj(bodyStr);
            log.debug("[{} 请求结果] - {}", url, body);
            if (!"0".equals(body.get("Status").toString())) {
                // 查询不正确
                log.error("[无法查询到该记录]: {}", bodyStr);
                throw new NoDnsRecordException();
            }
            JSONArray answers = body.get("Answer", JSONArray.class);
            int ttl = 0;
            String value = null;

            for (int i = 0; i < answers.size(); i++) {
                JSONObject answer = answers.get(i, JSONObject.class); // 获取第一个记录
                value = answer.get("data", String.class); // 记录值

                // 该记录是否合法的 Tag
                boolean isValid = true;
                if (DNSType.A.equals(dnsType)) {
                    // IPV4
                    isValid = UrlUtils.isValidIPv4(value);
                } else if (DNSType.AAAA.equals(dnsType)) {
                    // IPV6
                    isValid = UrlUtils.isValidIPv6(value);
                }
                // 其他查询类型直接选择第一个

                if (isValid) {
                    // 如果 value 符合要求
                    ttl = Objects.requireNonNull(answer).get("TTL", Integer.class); // TTL
                    break;
                }
            }

            // 创建 DNS 记录并返回
            return new DNSRecord(dnsType, domain, value, ttl);
        } catch (DnsQueryException | NoDnsRecordException e) {
            throw e;
        } catch (Exception e) {
            throw new DnsQueryException(e);
        }
    }

    /**
     * 获取 DOH 服务器地址<br>
     * 只支持 CloudFlare 和 google 的 JSON 格式<br>
     * 后续可以修改为多个轮询查询
     */
    private static String getDohUrl() {
        return "https://1.1.1.1/dns-query";
//        return "https://1.0.0.1/dns-query";
    }
}
