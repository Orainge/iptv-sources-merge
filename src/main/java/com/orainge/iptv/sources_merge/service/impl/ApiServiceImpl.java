package com.orainge.iptv.sources_merge.service.impl;

import cn.hutool.core.lang.TypeReference;
import com.orainge.iptv.sources_merge.bean.generate.GenerateConfig;
import com.orainge.tools.network.request.utils.RequestFileUtil;
import com.orainge.tools.spring.exception.RequestParamsException;
import com.orainge.iptv.sources_merge.service.ApiService;
import com.orainge.iptv.sources_merge.service.GenerateTxtFileService;
import com.orainge.iptv.sources_merge.vo.GenerateTxtFileParams;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * API 服务
 */
@Service
public class ApiServiceImpl implements ApiService {
    @Resource
    private GenerateTxtFileService generateTxtFileService;

    @Resource
    private RequestFileUtil requestFileUtil;

    /**
     * 创建直播源 TXT 文件
     */
    @Override
    public String generate(HttpServletResponse response, GenerateTxtFileParams bodyParams) {
        // 获取具体的配置文件
        GenerateConfig generateConfig;

        String url = bodyParams.getConfigUrl();
        if (StringUtils.isEmpty(url)) {
            // 使用提交的配置文件
            generateConfig = bodyParams.getConfig();
            if (generateConfig == null || generateConfig.isEmpty()) {
                throw new RequestParamsException();
            }
        } else {
            // 从 URL 获取配置文件
            generateConfig = requestFileUtil.getJsonToObject(
                    url,
                    Boolean.TRUE.equals(bodyParams.getEnableConfigUrlProxy()),
                    new TypeReference<GenerateConfig>() {
                    }
            );
        }

        // 提交到处理服务类
        return generateTxtFileService.processToTxtContent(generateConfig);
    }
}
