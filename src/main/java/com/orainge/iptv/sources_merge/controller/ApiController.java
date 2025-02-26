package com.orainge.iptv.sources_merge.controller;

import com.orainge.iptv.sources_merge.service.ApiService;
import com.orainge.iptv.sources_merge.vo.GenerateTxtFileParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * API Controller
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    @Resource
    private ApiService apiService;

    /**
     * 创建直播源 TXT 文件
     */
    @PostMapping("/txt/generate")
    public String generate(HttpServletResponse response,
                           @RequestBody GenerateTxtFileParams bodyParams) {
        return apiService.generate(response, bodyParams);
    }
}
