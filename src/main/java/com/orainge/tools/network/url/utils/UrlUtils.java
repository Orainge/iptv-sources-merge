package com.orainge.tools.network.url.utils;

import com.orainge.tools.network.url.enums.UrlType;
import com.orainge.tools.network.url.exception.UrlTypeErrorException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * URL 工具类
 */
@Slf4j
public class UrlUtils {
    /**
     * 正则表达式：IPV4 地址（含端口号）
     */
    private static final String IPV4_PATTERN = "((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)(\\.((25[0-5])|(2[0-4]\\d)|(1\\d\\d)|([1-9]\\d)|\\d)){3}(:[0-9]+)?";

    /**
     * 正则表达式：带方括号的 IPV6 地址（含端口号）
     */
    private static final String IPV6_PATTERN = "\\[((?:[\\da-fA-F]{0,4}:[\\da-fA-F]{0,4}){2,7})(?:[\\/\\\\%](\\d{1,3}))?\\](:([0-9]+))?";

    /**
     * 正则表达式：域名地址（含端口号）
     */
    private static final String DOMAIN_PATTERN = "[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?(:[0-9]+)?";

    private static final Pattern IPV4_PATTERN_OBJ = Pattern.compile(IPV4_PATTERN);
    private static final Pattern IPV6_PATTERN_OBJ = Pattern.compile(IPV6_PATTERN);
    private static final Pattern DOMAIN_PATTERN_OBJ = Pattern.compile(DOMAIN_PATTERN);

    /**
     * 字符串是否是 IPV4 地址
     */
    public static boolean isValidIPv4(String url) {
        return IPV4_PATTERN_OBJ.matcher(url).find();
    }

    /**
     * 字符串是否是 IPV6 地址
     */
    public static boolean isValidIPv6(String url) {
        return IPV6_PATTERN_OBJ.matcher(url).find();
    }

    /**
     * 字符串是否是 IP 地址
     */
    public static boolean isValidIPAddress(String url) {
        return isValidIPv4(url) || isValidIPv6(url);
    }

    /**
     * 字符串是否是域名地址
     */
    public static boolean isValidDomain(String url) {
        return DOMAIN_PATTERN_OBJ.matcher(url).find();
    }

    /**
     * 根据 URL 字符串获取 UrlType
     */
    public static UrlType judgeUrlType(String url) throws UrlTypeErrorException {
        if (isValidIPv4(url)) {
            return UrlType.IPV4;
        } else if (isValidIPv6(url)) {
            return UrlType.IPV6;
        } else if (isValidDomain(url)) {
            return UrlType.DOMAIN;
        } else {
            throw new UrlTypeErrorException();
        }
    }
}
