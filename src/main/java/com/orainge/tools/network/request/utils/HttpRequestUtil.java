package com.orainge.tools.network.request.utils;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.orainge.tools.network.proxy.utils.ExternalProxyUtil;
import com.orainge.tools.network.request.bean.HttpRequestParams;
import com.orainge.tools.network.request.config.HttpRequestConfig;
import com.orainge.tools.network.request.exception.HttpConnectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Http 请求工具
 */
@Slf4j
public class HttpRequestUtil {
    protected HttpRequestConfig httpRequestConfig;

    protected int bufferSize;

    protected static final HostnameVerifier TRUST_ALL_HOSTNAME_VERIFIER = (hostname, session) -> true;

    protected static SSLSocketFactory skipSSLSocketFactory = null;

    public HttpRequestUtil(HttpRequestConfig httpRequestConfig) throws Exception {
        this.httpRequestConfig = httpRequestConfig;

        // 缓冲区数组大小
        bufferSize = httpRequestConfig.getBufferSize() * 1024;

        // 初始化跳过证书
        TrustManager[] trustAllManagers = new TrustManager[]{
                new X509TrustManager() {
                    //检查客户端证书，若不信任该证书抛出异常
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    //检查服务器的证书，若不信任该证书抛出异常，可以不检查默认都信任
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    //返回受信任的X509证书数组
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllManagers, null);
        skipSSLSocketFactory = sc.getSocketFactory();

//        // 如果设置了跳过证书验证
//        if (httpRequestConfig.getSkipSslVerification()) {
//            // HttpURLConnection 设置默认跳过 SSL 验证
//            HttpsURLConnection.setDefaultSSLSocketFactory(skipSSLSocketFactory);
//        }
    }

    /**
     * GET 请求 (HttpURLConnection 请求)，将响应结果写入 HttpServletResponse 中
     */
    public void doRequestWriteToResponse(HttpRequestParams httpRequestParams, HttpServletResponse response) {
        doRequestFull(httpRequestParams, (conn -> {
            // 设置响应状态代码
            int code = conn.getResponseCode();
            response.setStatus(code);

            // 设置响应 Header
            Map<String, List<String>> headerMap = conn.getHeaderFields();
            if (headerMap != null && !headerMap.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
                    String headerName = entry.getKey();
                    List<String> headerValues = entry.getValue();

                    if (headerValues != null) {
                        for (String headerValue : headerValues) {
                            response.addHeader(headerName, headerValue);
                        }
                    }
                }
            }

            // 写入数据
            writeWithBuffer(conn.getInputStream(), response);
        }), ((e, conn) -> response.setStatus(500)));
    }

    /**
     * HttpURLConnection 请求<br>
     * 附加请求参数、请求头
     */
    public void doRequestFull(HttpRequestParams httpRequestParams,
                              ThrowingConsumer<HttpURLConnection> afterConnectedFunction,
                              BiConsumer<Throwable, HttpURLConnection> exceptionFunction) {
        boolean tag = false;
        String url = httpRequestParams.getUrl();

        // 附加 URL 请求参数
        HttpServletRequest request = httpRequestParams.getRequestToAppendParams();
        if (request != null) {
            url = appendQueryParams(url, request);
        }

        // 最大重试次数
        Integer maxRetryTimes = httpRequestParams.getConnectRetryTimes();
        if (maxRetryTimes == null) {
            maxRetryTimes = httpRequestConfig.getConnectRetryTimes();
        }

        // 最大重定向次数
        Integer maxRedirect = httpRequestParams.getMaxRequestRedirect();
        if (maxRedirect == null) {
            maxRedirect = httpRequestConfig.getMaxRequestRedirect();
        }

        // 读取时长（秒）
        Integer readTimeout = httpRequestParams.getReadTimeout();
        if (readTimeout == null) {
            readTimeout = httpRequestConfig.getReadTimeout();
        }

        // 连接时长（秒）
        Integer connectTimeout = httpRequestParams.getConnectTimeout();
        if (connectTimeout == null) {
            connectTimeout = httpRequestConfig.getConnectTimeout();
        }

        // 跳过 SSL 验证
        Boolean isSkipSslVerification = httpRequestParams.getSkipSslVerification();
        if (isSkipSslVerification == null) {
            isSkipSslVerification = httpRequestConfig.getSkipSslVerification();
        }

        for (int i = 0; i <= maxRetryTimes; i++) {
            HttpURLConnection conn = null;
            try {
                int redirectCount = 0; // 已重定向的次数
                do {
                    // 第一次请求 && 如果开启外部代理
                    if (redirectCount == 0 && httpRequestParams.isEnableExternalProxy()) {
                        // 设置 Proxy
                        conn = ExternalProxyUtil.getProxyHttpURLConnection(url);

                        // 如果默认不跳过，则代理的时候要跳过
                        if (!isSkipSslVerification) {
                            skipSSLVerification(conn);
                        }
                    } else {
                        URL u = new URL(url);
                        conn = (HttpURLConnection) u.openConnection();

                        // 如果设置了跳过验证，则更改为跳过证书验证
                        if (isSkipSslVerification) {
                            skipSSLVerification(conn);
                        }
                    }

                    conn.setRequestMethod(httpRequestParams.getMethodName()); // 请求方式，需要大写，小写会抛出异常
                    conn.setReadTimeout(readTimeout * 1000); // 设置读取时长
                    conn.setConnectTimeout(connectTimeout * 1000); // 设置连接时长
                    conn.setInstanceFollowRedirects(false); // 取消自动重定向

                    // 追加 User-Agent
                    // 可以在请求参数中自定义
                    String userAgent = httpRequestParams.getUserAgent();
                    if (StringUtils.isEmpty(userAgent)) {
                        userAgent = httpRequestConfig.getUserAgent();
                    }
                    if (!StringUtils.isEmpty(userAgent)) {
                        conn.addRequestProperty("User-Agent", httpRequestParams.getUserAgent());
                    }

                    // 执行连接
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                            responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                        // 重定向跳转
                        String redirectUrl = conn.getHeaderField("Location");
                        if (redirectUrl != null) {
                            // 跳转 URL 不为空，更改 URL 为重定向后的 URL
                            url = redirectUrl;

                            // 关闭当前连接
                            conn.disconnect();

                            // 重定向次数 +1
                            redirectCount++;
                        } else {
                            // 跳转 URL 为空
                            // 设置标签为 true
                            tag = true;

                            break;
                        }
                    } else {
                        // 非跳转
                        // 执行回调函数
                        if (afterConnectedFunction != null) {
                            afterConnectedFunction.accept(conn);
                        }

                        // 设置标签为 true
                        tag = true;

                        break;
                    }
                } while (redirectCount <= maxRedirect);
            } catch (Exception e) {
                if (exceptionFunction != null) {
                    exceptionFunction.accept(e, conn);
                }

                if (httpRequestParams.isPrintErrorLog()) {
                    HttpConnectException.log(i + 1, url, e.getMessage(), HttpRequestUtil.class);
                }
            } finally {
                // 断开连接
                if (conn != null) {
                    conn.disconnect();
                }
            }
            if (tag) {
                break;
            }
        }

        // 如果请求失败，则抛出异常
        if (!tag) {
            throw new HttpConnectException(url);
        }
    }

    /**
     * Hutool 请求<br>
     * 附加请求参数、请求头
     */
    public HttpResponse doRequest(HttpRequestParams httpRequestParams) {
        return doRequest(httpRequestParams, null, null);
    }

    /**
     * Hutool 请求<br>
     * 附加请求参数、请求头
     */
    public HttpResponse doRequest(HttpRequestParams httpRequestParams,
                                  ThrowingConsumer<HttpResponse> afterConnectedFunction,
                                  BiConsumer<Throwable, HttpRequest> exceptionFunction) {
        boolean tag = false;
        HttpResponse result = null;

        // 如果有请求参数，则附加上去
        String url = httpRequestParams.getUrl();
        url = appendQueryParams(url, httpRequestParams.getQueryParams());

        HttpServletRequest appendParamsByRequest = httpRequestParams.getRequestToAppendParams();
        if (appendParamsByRequest != null) {
            url = appendQueryParams(url, appendParamsByRequest);
        }

        // 保存原始 URL
        String originalUrl = url;

        // 最大重试次数
        Integer maxRetryTimes = httpRequestParams.getConnectRetryTimes();
        if (maxRetryTimes == null) {
            maxRetryTimes = httpRequestConfig.getConnectRetryTimes();
        }

        // 最大重定向次数
        Integer maxRedirect = httpRequestParams.getMaxRequestRedirect();
        if (maxRedirect == null) {
            maxRedirect = httpRequestConfig.getMaxRequestRedirect();
        }

        // 读取时长（秒）
        Integer readTimeout = httpRequestParams.getReadTimeout();
        if (readTimeout == null) {
            readTimeout = httpRequestConfig.getReadTimeout();
        }

        // 连接时长（秒）
        Integer connectTimeout = httpRequestParams.getConnectTimeout();
        if (connectTimeout == null) {
            connectTimeout = httpRequestConfig.getConnectTimeout();
        }

        for (int i = 0; i <= maxRetryTimes; i++) {
            // 还原 URL
            url = originalUrl;

            HttpRequest httpRequest = new HttpRequest(url);
            try {
                // 添加请求配置
                httpRequest.method(httpRequestParams.getMethod())
                        .setMaxRedirectCount(maxRedirect)
                        .setReadTimeout(readTimeout * 1000)
                        .setConnectionTimeout(connectTimeout * 1000)
                        .addHeaders(httpRequestParams.getHeaders()); // 添加自定义请求头（内部已判空）

                // 追加 User-Agent
                // 可以在请求参数中自定义
                String userAgent = httpRequestParams.getUserAgent();
                if (StringUtils.isEmpty(userAgent)) {
                    userAgent = httpRequestConfig.getUserAgent();
                }
                if (!StringUtils.isEmpty(userAgent)) {
                    httpRequest.header(Header.USER_AGENT, userAgent, false);
                }

                // 如果有请求体
                Object body = httpRequestParams.getBody();
                if (body != null) {
                    // body 为 json 字符串
                    httpRequest.body(JSONUtil.toJsonStr(body))
                            .header(Header.CONTENT_TYPE, "application/json;charset=UTF-8", false);
                }

                // 跳过 SSL 验证
                Boolean isSkipSslVerification = httpRequestParams.getSkipSslVerification();
                if (isSkipSslVerification == null) {
                    isSkipSslVerification = httpRequestConfig.getSkipSslVerification();
                }

                if (isSkipSslVerification) {
                    skipSSLVerification(httpRequest);
                }

                // 如果开启外部代理
                if (httpRequestParams.isEnableExternalProxy()) {
                    // 设置 Proxy
                    httpRequest.setProxy(ExternalProxyUtil.getProxy(url));

                    // 添加请求头，获取修改后的 URL
                    url = ExternalProxyUtil.setHostHeader(httpRequest, url);
                    httpRequest.setUrl(url);

                    // 如果默认不跳过，则代理的时候要跳过
                    if (!isSkipSslVerification) {
                        skipSSLVerification(httpRequest);
                    }
                }

                // 发起请求
                result = httpRequest.execute();

                // 执行回调函数
                if (afterConnectedFunction != null) {
                    afterConnectedFunction.accept(result);
                }

                // 变更签名
                tag = true;
            } catch (Exception e) {
                if (exceptionFunction != null) {
                    exceptionFunction.accept(e, httpRequest);
                }

                if (httpRequestParams.isPrintErrorLog()) {
                    HttpConnectException.log(i + 1, url, e.getMessage(), HttpRequestUtil.class);
                }
            } finally {
                if (result != null) {
                    result.close();
                }
            }
            if (tag) {
                break;
            }
        }

        if (result == null) {
            throw new HttpConnectException(originalUrl);
        }

        return result;
    }

    /**
     * 使用 buffer 写入数据到 response 中
     */
    public void writeWithBuffer(InputStream inputStream, HttpServletResponse response) {
        try {
            response.setBufferSize(bufferSize);

            byte[] buffer = new byte[bufferSize];
            try (OutputStream outputStream = response.getOutputStream();
                 InputStream in = inputStream) {
                int length;
                while ((length = in.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                    outputStream.flush();
                }
            }
        } catch (IOException ignore) {
            // 远程主机中断链接，不抛出异常
        }
    }

    /**
     * 使用 buffer 写入数据到 response 中
     */
    public void writeWithBuffer(byte[] bodyBytes, HttpServletResponse response) {
        try {
            response.setBufferSize(bufferSize);

            int part = bodyBytes.length / bufferSize;
            int more;
            if ((more = bodyBytes.length % bufferSize) > 0) {
                part += 1;
            }

            try (OutputStream outputStream = response.getOutputStream()) {
                for (int i = 0; i < part; i++) {
                    int offset = i * bufferSize;
                    int length = i == part - 1 ? (more > 0 ? more : bufferSize) : bufferSize;
                    outputStream.write(bodyBytes, offset, length);
                    outputStream.flush();
                }
            }
        } catch (IOException ignore) {
            // 远程主机中断链接，不抛出异常
        }
    }

    /**
     * 添加请求的所有参数到 query 参数到 URL 中
     */
    public String appendQueryParams(String url, HttpServletRequest request) {
        // 获取请求中的所有参数
        Map<String, String[]> params = request.getParameterMap();

        // 如果没有返回参数，就直接返回原串
        if (params.isEmpty()) {
            return url;
        }

        // 遍历请求参数，拼接到最终的URL
        StringBuilder appendUrlBuilder = new StringBuilder();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();
            for (String paramValue : paramValues) {
                appendUrlBuilder.append(paramName).append("=").append(paramValue).append("&");
            }
        }

        // 如果有需要追加的参数，就追加它
        if (appendUrlBuilder.length() != 0) {
            // 删除最后多余的'&'
            if (appendUrlBuilder.charAt(appendUrlBuilder.length() - 1) == '&') {
                appendUrlBuilder.deleteCharAt(appendUrlBuilder.length() - 1);
            }

            String appendUrl = appendUrlBuilder.toString();

            // 判断原有URL是否已经包含参数
            if (url.contains("?")) {
                // 如果已经包含参数，则在其基础上拼接'&'
                url += "&" + appendUrl;
            } else {
                // 如果不包含参数，则拼接'?'后再拼接'&'
                url += "?" + appendUrl;
            }
        }

        return url;
    }

    /**
     * 添加 query 参数到 URL 中
     */
    public String appendQueryParams(String url, Map<String, String> params) {
        // 如果没有返回参数，就直接返回原串
        if (params == null || params.isEmpty()) {
            return url;
        }

        // 遍历请求参数，拼接到最终的URL
        StringBuilder appendUrlBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            // 这里不需要用到过滤参数的功能，因为只有在生成代理链接的时候才调用
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            appendUrlBuilder.append(paramName).append("=").append(paramValue).append("&");
        }

        // 如果有需要追加的参数，就追加它
        if (appendUrlBuilder.length() != 0) {
            // 删除最后多余的'&'
            if (appendUrlBuilder.charAt(appendUrlBuilder.length() - 1) == '&') {
                appendUrlBuilder.deleteCharAt(appendUrlBuilder.length() - 1);
            }

            String appendUrl = appendUrlBuilder.toString();

            // 判断原有URL是否已经包含参数
            if (url.contains("?")) {
                // 如果已经包含参数，则在其基础上拼接'&'
                url += "&" + appendUrl;
            } else {
                // 如果不包含参数，则拼接'?'后再拼接'&'
                url += "?" + appendUrl;
            }
        }

        return url;
    }

    /**
     * 跳过证书验证 (HttpURLConnection)
     */
    private static void skipSSLVerification(HttpURLConnection connection) {
        if (!(connection instanceof HttpsURLConnection)) {
            // 如果不是 HTTPS 连接，返回
            return;
        }
        HttpsURLConnection conn = (HttpsURLConnection) connection;
        conn.setSSLSocketFactory(skipSSLSocketFactory);
        conn.setHostnameVerifier(TRUST_ALL_HOSTNAME_VERIFIER);
    }

    /**
     * 跳过证书验证 (Hutool)
     */
    private static void skipSSLVerification(HttpRequest httpRequest) {
        httpRequest.setSSLSocketFactory(skipSSLSocketFactory);
        httpRequest.setHostnameVerifier(TRUST_ALL_HOSTNAME_VERIFIER);
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }
}
