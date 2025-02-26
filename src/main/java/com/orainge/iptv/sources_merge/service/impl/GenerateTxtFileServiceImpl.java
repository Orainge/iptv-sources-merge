package com.orainge.iptv.sources_merge.service.impl;

import cn.hutool.core.lang.TypeReference;
import com.orainge.iptv.sources_merge.bean.generate.GenerateConfig;
import com.orainge.iptv.sources_merge.bean.generate.category.CategoryConfig;
import com.orainge.iptv.sources_merge.bean.generate.category.ChannelConfigItem;
import com.orainge.iptv.sources_merge.bean.generate.category.ChannelSourceItem;
import com.orainge.iptv.sources_merge.bean.generate.proxy.ProxyConfig;
import com.orainge.iptv.sources_merge.bean.generate.proxy.ProxyFilterConfig;
import com.orainge.iptv.sources_merge.bean.generate.source.ChannelData;
import com.orainge.iptv.sources_merge.bean.generate.source.SourceConfig;
import com.orainge.iptv.sources_merge.bean.generate.url_type.UrlTypeConfig;
import com.orainge.tools.common.utils.KeywordRegExrUtils;
import com.orainge.tools.network.dns.DNSManager;
import com.orainge.tools.network.request.utils.RequestFileUtil;
import com.orainge.iptv.sources_merge.service.GenerateTxtFileService;
import com.orainge.iptv.sources_merge.service.RequestService;
import com.orainge.tools.network.dns.exception.DnsQueryException;
import com.orainge.tools.network.url.enums.UrlType;
import com.orainge.tools.network.url.utils.UrlUtils;
import com.orainge.tools.spring.exception.RequestParamsException;
import com.orainge.tools.spring.exception.ResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

/**
 * 创建 TXT 直播源文件服务
 */
@Service
@Slf4j
public class GenerateTxtFileServiceImpl implements GenerateTxtFileService {
    /**
     * 频道没有直播源时使用的默认 URL 进行空占位
     */
    @Value("${ip-sources-merge.no-source-url}")
    private String noSourceUrl = "http://127.0.0.1";

    @Resource
    private Executor threadPoolTaskExecutor;

    @Resource
    private RequestService requestService;

    @Resource
    private RequestFileUtil requestFileUtil;

    @Override
    public String processToTxtContent(GenerateConfig generateConfig) {
        // 1. 获取数据源
        getSourceConfig(generateConfig);

        // 2. 获取分类配置
        getCategoryConfig(generateConfig);

        // 3. 整合直播源
        mergeSourceList(generateConfig);

        // 先做代理后做检查，因为有可能需要代理才能访问
        // 4. 检测是否需要替换为代理 URL
        checkProxy(generateConfig);

        // 5. 检测连接性
        checkConnection(generateConfig);

        // 6. 创建 TXT 文本内容并返回结果
        return generateText(generateConfig);
    }

    /**
     * 获取直播源配置
     */
    @Override
    public void getSourceConfig(GenerateConfig generateConfig) {
        log.info("[获取直播源] - 开始 [名称: {}]", generateConfig.getName());

        String url = generateConfig.getSourceConfigUrl();

        List<SourceConfig> sourceConfigList;
        if (StringUtils.isEmpty(url)) {
            // 使用提交的配置文件
            sourceConfigList = generateConfig.getSourceConfig();
            if (sourceConfigList == null || sourceConfigList.isEmpty()) {
                throw new RequestParamsException("分类配置不存在");
            }
        } else {
            // 从 URL 获取配置文件
            requestFileUtil.getJsonToParams(
                    url,
                    Boolean.TRUE.equals(generateConfig.getEnableSourceConfigUrlProxy()),
                    generateConfig,
                    "sourceConfig",
                    new TypeReference<List<SourceConfig>>() {
                    }
            );
            sourceConfigList = generateConfig.getSourceConfig();
        }

        // 请求直播源文件
        List<CompletableFuture<Boolean>> futureList = new LinkedList<>();
        for (SourceConfig sourceConfig : sourceConfigList) {
            futureList.add(CompletableFuture.supplyAsync(
                    () -> requestService.requestSourceFile(generateConfig, sourceConfig),
                    threadPoolTaskExecutor)
            );
        }

        // 获取请求结果
        for (CompletableFuture<Boolean> future : futureList) {
            try {
                Boolean result = future.get();
                // 允许跳过异常的直播源
//                if (!result) {
//                    // 只要有一个直播源获取异常，整个流程终止
//                    throw new ResponseException("获取直播源异常");
//                }
            } catch (Exception e) {
                log.warn("[获取直播源] - 错误 [名称: {}, URL: {}, 错误信息: {}]", generateConfig.getName(), url, e.getMessage());
                throw new ResponseException("获取直播源异常，流程终止");
            }
        }

        log.info("[获取直播源] - 结束 [名称: {}]", generateConfig.getName());
    }

    /**
     * 获取分类配置
     */
    @Override
    public void getCategoryConfig(GenerateConfig generateConfig) {
        log.info("[获取分类配置] - 开始 [名称: {}]", generateConfig.getName());

        String url = generateConfig.getCategoryConfigUrl();
        if (StringUtils.isEmpty(url)) {
            // 使用提交的配置文件
            List<CategoryConfig> categoryConfig = generateConfig.getCategoryConfig();
            if (categoryConfig == null || categoryConfig.isEmpty()) {
                throw new RequestParamsException("分类配置不存在");
            }
        } else {
            // 从 URL 获取配置文件
            requestFileUtil.getJsonToParams(
                    url,
                    Boolean.TRUE.equals(generateConfig.getEnableCategoryConfigUrlProxy()),
                    generateConfig,
                    "categoryConfig",
                    new TypeReference<List<CategoryConfig>>() {
                    }
            );
        }

        log.info("[获取分类配置] - 结束 [名称: {}]", generateConfig.getName());
    }

    /**
     * 整合直播源
     */
    @Override
    public void mergeSourceList(GenerateConfig generateConfig) {
        log.info("[整合直播源] - 开始 [名称: {}]", generateConfig.getName());

        // 是否启用直播源 URL 关键字排除
        List<String> excludeSourceUrlKeyword = generateConfig.getExcludeSourceUrlKeyword();
        boolean enableExcludeSourceUrl = excludeSourceUrlKeyword != null && !excludeSourceUrlKeyword.isEmpty();

        // 需要启用什么 URL 类型的直播源 URL
        UrlTypeConfig urlTypeConfig = generateConfig.getUrlType();
        List<UrlType> enableUrlTypeList = urlTypeConfig.getEnableUrlTypeList();

        // 拿到分类对象
        List<CategoryConfig> categoryConfigList = generateConfig.getCategoryConfig();
        for (CategoryConfig categoryConfig : categoryConfigList) {
            // 获取分类名称
            String categoryName = categoryConfig.getName();

            // 找到可用的直播源配置（根据名称）
            List<SourceConfig> availableSourceConfig = new LinkedList<>();
            for (SourceConfig sourceConfig : generateConfig.getSourceConfig()) {
                List<String> categoryList = sourceConfig.getCategory();

                // 直播源不为空 && (未配置分类 || 包含关键字，添加该直播源)
                if (!sourceConfig.getChannelData().isEmpty() &&
                        (categoryList == null || categoryList.isEmpty() ||
                                KeywordRegExrUtils.isHintString(categoryList, categoryName))) {
                    availableSourceConfig.add(sourceConfig);
                }
            }

            // 遍历具体的频道
            List<ChannelConfigItem> channelConfigList = categoryConfig.getList();
            for (ChannelConfigItem channelItem : channelConfigList) {
                // 频道 Keyword
                List<Pattern> keywordPatternList = channelItem.getKeywordPattern();

                // 频道 ExcludeUrlKeyword
                List<Pattern> excludeUrlKeywordPatternList = channelItem.getExcludeUrlKeywordPattern();

                // 遍历所有直播源，获取与当前频道匹配的所有直播源
                // 直播源 ID -> 直播源 URL Map
                Map<String, LinkedHashSet<ChannelSourceItem>> sourceIdToUrlMap = new LinkedHashMap<>();
                for (SourceConfig sourceConfig : availableSourceConfig) {
                    String sourceId = sourceConfig.getId();
                    ChannelData channelData = sourceConfig.getChannelData();

                    // 直播源的 IP 类型也要合并检测
                    List<UrlType> finallyEnableUrlType = sourceConfig.getFinallyEnableUrlType();
                    if (finallyEnableUrlType == null) {
                        // 计算最终使用的 URL 类型 (仅一次)
                        List<UrlType> sourceUrlTypeList = sourceConfig.getUrlTypeList();
                        if (sourceUrlTypeList == null || sourceUrlTypeList.isEmpty()) {
                            // 如果没有自定义，就使用默认的
                            finallyEnableUrlType = enableUrlTypeList;
                        } else {
                            // 有自定义的，取交集
                            // 将交集的结果放入到 list 中，所以要新建一个列表，否则会影响原来的 list
                            List<UrlType> list = new LinkedList<>(enableUrlTypeList);
                            list.retainAll(sourceUrlTypeList);
                            finallyEnableUrlType = list;
                        }

                        // 设置最终的结果
                        sourceConfig.setFinallyEnableUrlType(finallyEnableUrlType);
                    }

                    for (Map.Entry<String, LinkedHashSet<String>> entry : channelData.entrySet()) {
                        if (KeywordRegExrUtils.isHintPattern(keywordPatternList, entry.getKey())) {
                            // 当前频道是需要筛选出来的频道
                            LinkedHashSet<ChannelSourceItem> values = sourceIdToUrlMap.computeIfAbsent(sourceId, (k) -> new LinkedHashSet<>());

                            for (String url : entry.getValue()) {
                                // 检测 URL 是否包含指定的关键字，包含就要排除
                                if (enableExcludeSourceUrl && KeywordRegExrUtils.isHintString(excludeSourceUrlKeyword, url)) {
                                    // 启用排除，需要进行过滤
                                    continue;
                                }

                                // 检查频道是否启用了自定义 URL 是否排除
                                if (KeywordRegExrUtils.isHintPattern(excludeUrlKeywordPatternList, url)) {
                                    // 启用排除，需要进行过滤
                                    continue;
                                }

                                // 检测是否是需要保留的 URL 类型
                                UrlType urlType;
                                try {
                                    urlType = UrlUtils.judgeUrlType(url);
                                    if (!finallyEnableUrlType.contains(urlType)) {
                                        // 不包括该类型的 URL，过滤
                                        continue;
                                    }
                                } catch (Exception e) {
                                    // 不存在该类型，过滤
                                    continue;
                                }

                                UrlType urlIpType = urlType;

                                // 请求 URL 做域名检测
                                // 如果启用了域名 IP 类型检测
                                if (UrlType.DOMAIN.equals(urlType) &&
                                        urlTypeConfig.isEnableDomainCheckIpType()) {
                                    // 查询域名对应的 IP 地址是否存在
                                    try {
                                        DNSManager.queryByUrl(url, urlTypeConfig.getSingleDnsType());
                                        urlIpType = urlTypeConfig.getSingleUrlType();
                                    } catch (DnsQueryException e) {
                                        log.error("DNS 查询错误", e);
                                        // 查询错误，说明不存在该类型的 URL，舍弃
                                        continue;
                                    }
                                }

                                // 将 URL 添加到列表中
                                values.add(new ChannelSourceItem()
                                        .setUrl(url)
                                        .setUrlType(urlType)
                                        .setUrlIpType(urlIpType)
                                );
                            }
                        }
                    }
                }

                // 如果直播源不为空
                if (!sourceIdToUrlMap.isEmpty()) {
                    // 检测分类排序
                    List<String> sourceOrderList = channelItem.getSourceOrder();
                    CopyOnWriteArraySet<ChannelSourceItem> finallyUrlSet = channelItem.getMergeSourceSet();
                    if (sourceOrderList != null && !sourceOrderList.isEmpty()) {
                        // 启用分类排序，先添加优先排序的直播源
                        for (String idOrder : sourceOrderList) {
                            LinkedHashSet<ChannelSourceItem> sourceItems = sourceIdToUrlMap.remove(idOrder);
                            if (sourceItems != null && !sourceItems.isEmpty()) {
                                finallyUrlSet.addAll(sourceItems);
                            }
                        }
                    }

                    // 添加剩余的直播源/未启用分类排序，直接合并所有直播源
                    for (LinkedHashSet<ChannelSourceItem> sourceItem : sourceIdToUrlMap.values()) {
                        finallyUrlSet.addAll(sourceItem);
                    }
                }
            }
        }

        log.info("[整合直播源] - 结束 [名称: {}]", generateConfig.getName());
    }

    /**
     * 检测 URL 连接性
     */
    @Override
    public void checkConnection(GenerateConfig generateConfig) {
        if (!Boolean.TRUE.equals(generateConfig.getCheckConnection())) {
            return;
        }

        log.info("[连接性检查] - 开始 [名称: {}]", generateConfig.getName());

        // 排除检查连接性的 URL
        List<String> excludeCheckConnectionUrlKeyword = generateConfig.getExcludeCheckConnectionUrlKeyword();
        boolean enableExcludeCheckConnectionUrlKeyword = excludeCheckConnectionUrlKeyword != null && !excludeCheckConnectionUrlKeyword.isEmpty();

        // 多线程列表
        List<CompletableFuture<Boolean>> futureList = new LinkedList<>();

        // 遍历直播源
        List<CategoryConfig> categoryConfigList = generateConfig.getCategoryConfig();
        for (CategoryConfig categoryConfig : categoryConfigList) {
            String categoryName = categoryConfig.getName(); // 分类名称
            List<ChannelConfigItem> channelList = categoryConfig.getList();
            for (ChannelConfigItem channelItem : channelList) {
                String channelName = channelItem.getName(); // 频道名称
                CopyOnWriteArraySet<ChannelSourceItem> sourceItems = channelItem.getMergeSourceSet(); // 直播源 Set
                for (ChannelSourceItem sourceItem : sourceItems) {
                    if (enableExcludeCheckConnectionUrlKeyword &&
                            KeywordRegExrUtils.isHintString(excludeCheckConnectionUrlKeyword, sourceItem.getUrl())) {
                        // 启用 URL 排除，需要进行过滤
                        continue;
                    }

                    // 执行连接性检查，如果无法连接就会删除
                    futureList.add(CompletableFuture.supplyAsync(
                            () -> requestService.doConnectionCheck(generateConfig, sourceItem, sourceItems, categoryName, channelName),
                            threadPoolTaskExecutor)
                    );
                }
            }
        }

        log.info("[连接性检查] - 任务创建完成，等待检查完成 [名称: {}]", generateConfig.getName());

        // 等待所有直播源检测完成
        int errorCount = 0;
        for (CompletableFuture<Boolean> future : futureList) {
            try {
                Boolean result = future.get();
                if (!result) {
                    errorCount++;
                }
            } catch (Exception e) {
                log.debug("[连接性检查] - 错误 [名称: {}, 信息: {}]", generateConfig.getName(), e.getMessage());
                errorCount++;
            }
        }

        log.info("[连接性检查] - 完成 [名称: {}, 需要检测的 URL 总数: {}, 失效数: {}]", generateConfig.getName(), futureList.size(), errorCount);
    }

    /**
     * 检测是否需要替换为代理 URL
     */
    @Override
    public void checkProxy(GenerateConfig generateConfig) {
        // 代理配置
        ProxyConfig proxyConfig = generateConfig.getProxy();
        if (proxyConfig == null || !proxyConfig.isEnable()) {
            // 没有代理配置 || 未开启代理，无需检查
            return;
        }

        log.info("[直播源代理替换] - 开始 [名称: {}]", generateConfig.getName());

        // 多线程列表
        List<CompletableFuture<Boolean>> futureList = new LinkedList<>();

        // 遍历直播源
        List<CategoryConfig> categoryConfigList = generateConfig.getCategoryConfig();
        for (CategoryConfig categoryConfig : categoryConfigList) {
            String categoryName = categoryConfig.getName(); // 分类名称
            List<ChannelConfigItem> channelList = categoryConfig.getList();
            for (ChannelConfigItem channelItem : channelList) {
                String channelName = channelItem.getName(); // 频道名称
                CopyOnWriteArraySet<ChannelSourceItem> sourceItems = channelItem.getMergeSourceSet(); // 直播源 Set
                for (ChannelSourceItem sourceItem : sourceItems) {
                    // 判断是否需要获取代理链接
                    ProxyFilterConfig proxyFilterConfig = proxyConfig.getProxyFilter(categoryName, channelName, sourceItem);
                    if (proxyFilterConfig != null) {
                        // 获取代理
                        futureList.add(CompletableFuture.supplyAsync(
                                () -> requestService.getProxyUrl(generateConfig, categoryName, channelName, proxyFilterConfig, sourceItem),
                                threadPoolTaskExecutor)
                        );
                    }
                }
            }
        }

        // 等待所有直播源替换完成
        int errorCount = 0;
        for (CompletableFuture<Boolean> future : futureList) {
            try {
                Boolean result = future.get();
                if (!result) {
                    errorCount++;
                }
            } catch (Exception e) {
                log.warn("[直播源代理替换] - 错误 [名称: {}, 信息: {}]", generateConfig.getName(), e.getMessage());
                errorCount++;
            }
        }

        log.info("[直播源代理替换] - 完成 [名称: {}, 总数: {}, 失效数: {}]", generateConfig.getName(), futureList.size(), errorCount);
    }

    /**
     * 生成 TXT 文本
     */
    @Override
    public String generateText(GenerateConfig generateConfig) {
        log.info("[生成整合的直播源] - 开始 [名称: {}]", generateConfig.getName());

        // 创建 Builder
        StringBuilder builder = new StringBuilder();

        // 拿到分类对象
        List<CategoryConfig> categoryConfigList = generateConfig.getCategoryConfig();
        for (CategoryConfig categoryConfig : categoryConfigList) {
            // 生成标题
            builder.append(categoryConfig.getName()).append(",#genre#").append("\n");

            // 附加直播源
            List<ChannelConfigItem> list = categoryConfig.getList();
            for (ChannelConfigItem channelItem : list) {
                CopyOnWriteArraySet<ChannelSourceItem> mergeSourceList = channelItem.getMergeSourceSet();
                if (mergeSourceList.isEmpty()) {
                    // 该频道没有直播源，添加一个地址占位
                    log.debug("[生成整合的直播源] - 警告 [名称: {}, 分类: {}, 频道: {}, 信息: 该频道无直播源，使用默认 URL {} 替换]",
                            generateConfig.getName(), categoryConfig.getName(), channelItem.getName(), noSourceUrl
                    );
                    builder.append(channelItem.getName()).append(",").append(noSourceUrl).append("\n");
                } else {
                    // 该频道有直播源，逐个添加
                    for (ChannelSourceItem sourceItem : mergeSourceList) {
                        builder.append(channelItem.getName()).append(",").append(sourceItem).append("\n");
                    }
                }
            }

            // 添加空行
            builder.append("\n");
        }

        log.info("[生成整合的直播源] - 结束 [名称: {}]", generateConfig.getName());

        // 返回整个 TXT 文本
        return builder.toString();
    }
}
