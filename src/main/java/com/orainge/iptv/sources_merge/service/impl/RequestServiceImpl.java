package com.orainge.iptv.sources_merge.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.orainge.iptv.sources_merge.bean.generate.GenerateConfig;
import com.orainge.tools.network.request.bean.HttpRequestParams;
import com.orainge.iptv.sources_merge.bean.generate.category.ChannelSourceItem;
import com.orainge.iptv.sources_merge.bean.generate.proxy.ProxyApiConfig;
import com.orainge.iptv.sources_merge.bean.generate.proxy.ProxyApiDataConfig;
import com.orainge.iptv.sources_merge.bean.generate.proxy.ProxyFilterConfig;
import com.orainge.iptv.sources_merge.bean.generate.source.ChannelData;
import com.orainge.iptv.sources_merge.bean.generate.source.SourceConfig;
import com.orainge.iptv.sources_merge.service.RequestService;
import com.orainge.tools.network.request.utils.HttpRequestUtil;
import com.orainge.tools.network.request.utils.RequestFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 请求文件服务
 */
@Service
@Slf4j
public class RequestServiceImpl implements RequestService {
    @Resource
    private RequestFileUtil requestFileUtil;

    @Resource
    private HttpRequestUtil httpRequestUtil;

    /**
     * 请求直播源文件
     */
    @Override
    public boolean requestSourceFile(GenerateConfig generateConfig, SourceConfig sourceConfig) {
        String url = sourceConfig.getUrl();
        ChannelData channelData = sourceConfig.getChannelData();
        try (InputStream inputStream = requestFileUtil.getBodyStream(new HttpRequestParams()
                .setUrl(url)
                .setEnableExternalProxy(sourceConfig.getEnableProxy())
                .setConnectTimeout(5)
                .setReadTimeout(40)
        );
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            // 按行读取
            String line;
            while ((line = reader.readLine()) != null) {
                // 逐行处理
                if (line.length() == 0 || line.startsWith("#") || line.contains("#genre#")) {
                    // 空行 || 以 # 开始 || 分类tag，跳过该行
                    continue;
                }

                channelData.addChannelByLine(line);
            }
        } catch (Exception e) {
            log.warn("[获取直播源] - 错误 [名称: {}, URL: {}, 错误信息: {}]", generateConfig.getName(), url, e.getMessage());
            return false;
        }

        // 如果没有获取到直播源
        if (channelData.isEmpty()) {
            log.warn("[获取直播源] - 错误 [名称: {}, URL: {}, 错误信息: 请求的文件中无可用的直播源]", generateConfig.getName(), url);
            return false;
        }

        log.debug("[获取直播源] - 成功 [名称: {}, URL: {}]", generateConfig.getName(), url);

        // 返回成功信息
        return true;
    }

    /**
     * 执行连接性检查
     */
    @Override
    public boolean doConnectionCheck(GenerateConfig generateConfig,
                                     ChannelSourceItem channelSourceItem,
                                     CopyOnWriteArraySet<ChannelSourceItem> channelSourceItems,
                                     String categoryName,
                                     String channelName) {
        // 直播源 URL
        String url = channelSourceItem.getUrl();

        try {
            // 请求结果
            AtomicBoolean result = new AtomicBoolean(false);

            // 开始请求
            httpRequestUtil.doRequestFull(
                    new HttpRequestParams()
                            .setUserAgent("okhttp/3")
                            .setUrl(url)
                            .setConnectRetryTimes(2)
                            .setConnectTimeout(10)
                            .setReadTimeout(6)
                            .setPrintErrorLog(false),
                    (conn) -> {
                        int statusCode = conn.getResponseCode();
                        if (statusCode == 200) {
                            // 检查 contentType 是否符合以下类型
                            // M3U8 文件：application/octet-stream || application/vnd.apple.mpegurl || application/x-mpegURL
                            // 视频文件（流式传输）：application/octet-stream || video/*
                            // 要读取文件内容查看: 其他类型

                            // 比较 Content-Type, 统一转换为小写进行比较
                            String contentType = conn.getContentType();
                            if (contentType == null) {
                                contentType = "";
                            }
                            contentType = contentType.toLowerCase(Locale.ROOT);

                            try (InputStream in = conn.getInputStream()) {
                                if (contentType.startsWith("video/") || contentType.contains("mpegurl")) {
                                    // 已知的文件类型
                                    // 检查输入流是否为空
                                    if (in.read() == -1) {
                                        // 请求内容为空
                                        log.debug("[连接性检查] - 错误 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: 请求内容为空]",
                                                generateConfig.getName(), categoryName, channelName, url);
                                    } else {
                                        // 请求内容不为空，检测通过
                                        result.set(true);
                                        log.debug("[连接性检查] - 成功 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}]",
                                                generateConfig.getName(), categoryName, channelName, url);
                                    }
                                } else {
                                    // 未知 Content-Type，要读取第一行看看是不是 #EXTM3U
                                    // 读取第一个字节
                                    int firstByte = in.read();

                                    // 判断第一个字节是否存在（如果没有读取到字节，返回值为 -1）
                                    if (firstByte == -1) {
                                        log.debug("[连接性检查] - 错误 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: 请求内容为空]",
                                                generateConfig.getName(), categoryName, channelName, url);
                                    } else {
                                        // 如果读取到数据，继续检查是否以 "#EXTM3U" 开头
                                        byte[] buffer = new byte[7];
                                        buffer[0] = (byte) firstByte;
                                        int bytesRead = in.read(buffer, 1, 6);

                                        if (bytesRead == 6) {
                                            // 将字节流转换为字符串，使用 UTF-8 解码
                                            String firstBytes = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

                                            if (firstBytes.equals("#EXTM3U")) {
                                                result.set(true);
                                                log.debug("[连接性检查] - 成功 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}]",
                                                        generateConfig.getName(), categoryName, channelName, url);
                                            } else {
                                                log.debug("[连接性检查] - 错误 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: 检测文件内容，不是M3U8文件]",
                                                        generateConfig.getName(), categoryName, channelName, url);
                                            }
                                        } else {
                                            log.debug("[连接性检查] - 错误 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: 检测文件内容，不是M3U8文件]",
                                                    generateConfig.getName(), categoryName, channelName, url);
                                        }
                                    }
                                }
                            }
                        } else {
                            log.debug("[连接性检查] - 错误 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: 状态码不正确: {}]",
                                    generateConfig.getName(), categoryName, channelName, url, statusCode);
                        }
                    },
                    (e, h) -> {
                        log.debug("[连接性检查] - 错误 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: {}]",
                                generateConfig.getName(), categoryName, channelName, url, e.getMessage());
                    }
            );

            // 获取检查结果
            boolean r = result.get();

            if (!r) {
                // 连接错误，移除 URL
                channelSourceItems.remove(channelSourceItem);
            }

            // 返回检查结果
            return r;
        } catch (Exception e) {
            //log.debug("[连接性检查] - 错误 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: {}]",
            //        generateConfig.getName(), categoryName, channelName, url, e.getMessage());
            channelSourceItems.remove(channelSourceItem); // 移除该 URL
            return false;
        }
    }

    /**
     * 获取代理后的直播源 URL
     */
    @Override
    public boolean getProxyUrl(GenerateConfig generateConfig,
                               String categoryName,
                               String channelName,
                               ProxyFilterConfig filterConfig,
                               ChannelSourceItem channelSourceItem) {
        String url = channelSourceItem.getUrl();
        try {
            // 获取代理配置
            ProxyApiConfig apiConfig = filterConfig.getApi();
            String apiUrl = apiConfig.getUrl();
            String methodName = apiConfig.getMethodName();
            ProxyApiDataConfig apiData = apiConfig.getApiData();

            // 赋值和替换 body 里面的参数
            for (Map.Entry<String, Object> entry : apiData.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    if ("${timestamp}".equals(value)) {
                        // 时间戳
                        entry.setValue(System.currentTimeMillis() / 1000);
                    } else if ("${url}".equals(value)) {
                        // 直播源 URL
                        entry.setValue(url);
                    } else if ("${externalProxy}".equals(value)) {
                        // 是否需要外部代理
                        entry.setValue(filterConfig.getExternalProxy());
                    }
                }
            }

            // 请求 API
            HttpResponse response = httpRequestUtil.doRequest(
                    new HttpRequestParams()
                            .setMethodName(methodName)
                            .setUrl(apiUrl)
                            .setBody(apiData)
            );

            // 获取结果
            // 从响应结果里的 [data] 字段获取
            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            String proxyUrl = jsonObject.get("data", String.class);

            if (proxyUrl == null || "".equals(proxyUrl)) {
                // 获取的结果为空
                log.warn("[获取代理 URL] - 错误 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: API 获取的结果为空]",
                        generateConfig.getName(), categoryName, channelName, url);
                return false;
            } else {
                // 赋值
                channelSourceItem.setUrl(proxyUrl);
                log.debug("[获取代理 URL] - 成功 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}]",
                        generateConfig.getName(), categoryName, channelName, url);
                return true;
            }
        } catch (Exception e) {
            // 如果出错就抛出打印异常忽略它
            log.warn("[获取代理 URL] - 成功 [名称: {}, 频道分类: {}, 频道名称: {}, URL: {}, 错误信息: {}]",
                    generateConfig.getName(), categoryName, channelName, url, e.getMessage());
            return false;
        }
    }
}
