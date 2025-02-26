package com.orainge.iptv.sources_merge.service;

import com.orainge.iptv.sources_merge.vo.GenerateTxtFileParams;

import javax.servlet.http.HttpServletResponse;

public interface ApiService {
    String generate(HttpServletResponse response,
                  GenerateTxtFileParams bodyParams);
}
