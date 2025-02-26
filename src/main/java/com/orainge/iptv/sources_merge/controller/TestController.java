package com.orainge.iptv.sources_merge.controller;

import com.orainge.tools.network.request.bean.HttpRequestParams;
import com.orainge.tools.network.request.utils.HttpRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * API Controller
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    @Resource
    private HttpRequestUtil httpRequestUtil;

    /**
     * 创建直播源 TXT 文件
     */
    @GetMapping("/a")
    public void generate() {
        // 开始请求
        httpRequestUtil.doRequestFull(
                new HttpRequestParams()
                        .setUrl("http://[2409:8087:7000:20:1000::22]:6060/yinhe/2/ch00000090990000002065/index.m3u8?virtualDomain=yinhe.live_hls.zte.com")
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.71 Safari/537.36")
                        .setConnectRetryTimes(2)
                        .setConnectTimeout(15)
                        .setReadTimeout(15)
                        .setPrintErrorLog(false),
                (conn) -> {
                    int statusCode = conn.getResponseCode();
                    if (statusCode == 200 || statusCode == 301 || statusCode == 302 || statusCode == 307) {
                        // 检查 contentType (以 video 开头 || 包含 mpegurl）
                        // application/vnd.apple.mpegurl
                        // video/MP2T
                        // video/x-flv
                        // video/*
                        String contentType = conn.getContentType();

                        if (contentType.startsWith("video/") || contentType.contains("mpegurl")) {
                            // 检查内容是否为空
                            // 检测输入流是否为空
                            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            if (in.read() == -1) {
                                // 请求内容为空
                                log.debug("[连接性检查] - 错误 [错误信息: 请求内容为空]");
                            } else {
                                // 请求内容不为空
                                log.debug("[连接性检查] - 成功");
                            }
                            // 关闭输入流
                            in.close();
                        } else {
                            log.debug("[连接性检查] - 错误 [错误信息: Content-Type 类型不正确: {}]", contentType);
                        }
                    } else {
                        log.debug("[连接性检查] - 错误 [错误信息: 状态码不正确: {}]", statusCode);
                    }
                },
                (e, h) -> {
                }
        );
    }
}
