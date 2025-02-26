package com.orainge.tools.network.request.bean;

import cn.hutool.http.Method;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP 请求参数
 */
@Data
@Accessors(chain = true)
public class HttpRequestParams {
    /**
     * 请求参数
     */
    private String url;

    /**
     * 请求方式<br>
     * 默认为 GET
     */
    private String methodName = "GET";

    /**
     * 请求头
     */
    private Map<String, String> headers;

    /**
     * Query 参数
     */
    private Map<String, String> queryParams;

    /**
     * 请求 Body<br>
     * 以 JSON 格式提交，自动添加请求头
     */
    private Object body;

    /**
     * 是否使用外部代理请求<br>
     * 默认为 false
     */
    private boolean enableExternalProxy = false;

    /**
     * 读取超时时间（秒）
     */
    private Integer readTimeout;

    /**
     * 连接超时时间（秒）
     */
    private Integer connectTimeout;

    /**
     * 连接失败后重试次数
     */
    private Integer connectRetryTimes;

    /**
     * 最大重定向次数
     */
    private Integer maxRequestRedirect;

    /**
     * 跳过 SSL 证书验证
     */
    private Boolean skipSslVerification;

    /**
     * 自定义请求头
     */
    private String userAgent;

    /**
     * 设置要附加的请求参数的 HttpServletRequest
     */
    private HttpServletRequest requestToAppendParams;

    /**
     * 是否输出错误日志<br>
     * 默认输出
     */
    private boolean printErrorLog = true;

    /**
     * 获取请求方法
     *
     * @return 请求方法字符串
     */
    public Method getMethod() {
        // 将字符串转换为 Method 对象
        try {
            return Method.valueOf(methodName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException("不支持的 HTTP 请求方式: " + methodName);
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public HttpRequestParams setMethodName(String methodName) {
        this.methodName = methodName.toUpperCase(Locale.ROOT);
        return this;
    }
}
