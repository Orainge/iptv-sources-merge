package com.orainge.tools.network.request.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.orainge.tools.network.request.bean.HttpRequestParams;
import com.orainge.tools.spring.exception.RequestParamsException;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 请求文件工具类
 */
public class RequestFileUtil {
    protected final HttpRequestUtil httpRequestUtil;

    public RequestFileUtil(HttpRequestUtil httpRequestUtil) {
        this.httpRequestUtil = httpRequestUtil;
    }

    /**
     * 从 URL 获取 String Body
     *
     * @param httpRequestParams 请求参数
     */
    public InputStream getBodyStream(HttpRequestParams httpRequestParams) {
        HttpResponse response = httpRequestUtil.doRequest(httpRequestParams);
        return response.bodyStream();
    }

    /**
     * 从 URL 获取 JSON 后转换为实体类
     *
     * @param url           请求 URL
     * @param typeReference 目标实体类类型
     */
    public <T> T getJsonToObject(String url, TypeReference<T> typeReference) {
        return getJsonToObject(url, false, typeReference);
    }

    /**
     * 从 URL 获取 JSON 后转换为实体类
     *
     * @param url           请求 URL
     * @param enableProxy   是否启用代理请求
     * @param typeReference 目标实体类类型
     */
    public <T> T getJsonToObject(String url, boolean enableProxy, TypeReference<T> typeReference) {
        HttpResponse response = httpRequestUtil.doRequest(new HttpRequestParams()
                .setUrl(url)
                .setEnableExternalProxy(enableProxy)
        );
        String body = response.body();
        return JSONUtil.toBean(body, typeReference, true);
    }

    /**
     * 从 URL 获取 JSON 后转换为实体类，然后将类复制到容器类的参数中
     *
     * @param url            请求 URL
     * @param dataContentObj 容器类对象
     * @param paramsName     参数名
     * @param typeReference  目标实体类类型
     */
    public <T> void getJsonToParams(String url, Object dataContentObj, String paramsName, TypeReference<T> typeReference) {
        getJsonToParams(url, false, dataContentObj, paramsName, typeReference);
    }

    /**
     * 从 URL 获取 JSON 后转换为实体类，然后将类复制到容器类的参数中
     *
     * @param url            请求 URL
     * @param enableProxy    是否启用代理请求
     * @param dataContentObj 容器类对象
     * @param paramsName     参数名
     * @param typeReference  目标实体类类型
     */
    public <T> void getJsonToParams(String url,
                                    boolean enableProxy,
                                    Object dataContentObj,
                                    String paramsName,
                                    TypeReference<T> typeReference) {
        try {
            T data = getJsonToObject(url, enableProxy, typeReference);

            // 参数名驼峰转换
            paramsName = Character.toUpperCase(paramsName.charAt(0)) + paramsName.substring(1);

            Method setParamsMethod = dataContentObj.getClass().getMethod("set" + paramsName, getClassFromType(typeReference.getType()));
            setParamsMethod.invoke(dataContentObj, data);
        } catch (Exception e) {
            throw new RequestParamsException(e);
        }
    }

    private Class<?> getClassFromType(Type type) throws ClassNotFoundException {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            } else {
                throw new IllegalArgumentException("Type does not have parameterized types");
            }
        } else {
            // 类型没有参数化类型，给定的类型中没有泛型参数
            String typeName = type.getTypeName();
            return Class.forName(typeName);
        }
    }
}
