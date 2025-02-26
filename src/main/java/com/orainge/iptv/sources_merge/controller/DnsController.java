package com.orainge.iptv.sources_merge.controller;

import com.orainge.tools.network.dns.DNSManager;
import com.orainge.tools.network.dns.consts.DNSType;
import com.orainge.tools.spring.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * DNS 工具
 */
@RestController
@RequestMapping("/dns")
@Slf4j
public class DnsController {
    /**
     * 查询 DNS
     *
     * @param name 域名
     * @param type DNS 查询类型
     */
    @GetMapping("/query")
    public Result query(@RequestParam(value = "name") String name,
                        @RequestParam(value = "type") String type) {
        return Result.ok(DNSManager.query(name, Objects.requireNonNull(DNSType.getByType(type))));
    }

    /**
     * 清空缓存
     */
    @GetMapping("/refresh")
    public Result refresh() {
        DNSManager.refresh();
        return Result.ok("已清空 DNS 缓存");
    }
}
