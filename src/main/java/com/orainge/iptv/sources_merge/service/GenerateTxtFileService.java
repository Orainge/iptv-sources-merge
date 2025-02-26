package com.orainge.iptv.sources_merge.service;

import com.orainge.iptv.sources_merge.bean.generate.GenerateConfig;

/**
 * 创建 TXT 直播源文件服务
 */
public interface GenerateTxtFileService {
    String processToTxtContent(GenerateConfig generateConfig);

    void getSourceConfig(GenerateConfig generateConfig);

    void getCategoryConfig(GenerateConfig generateConfig);

    void mergeSourceList(GenerateConfig generateConfig);

    void checkConnection(GenerateConfig generateConfig);

    void checkProxy(GenerateConfig generateConfig);

    String generateText(GenerateConfig generateConfig);
}
