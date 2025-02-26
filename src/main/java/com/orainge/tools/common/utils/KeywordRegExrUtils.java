package com.orainge.tools.common.utils;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 字符串工具类（正则校验）
 */
public class KeywordRegExrUtils {
    /**
     * 以正则表达式检查 targetStr 是否命中
     */
    public static boolean isHintString(String keywordRexExp, String targetStr) {
        if (keywordRexExp == null || keywordRexExp.length() == 0) {
            return false;
        }

        // targetStr 为空表示全部命中
        if (targetStr == null || targetStr.length() == 0) {
            return true;
        }

        // * 号表示全部命中
        if ("*".equals(keywordRexExp) || "*".equals(targetStr)) {
            return true;
        }

        return Pattern.compile(keywordRexExp).matcher(targetStr).find();
    }

    /**
     * 以正则表达式检查 targetStr 是否命中
     */
    public static boolean isHintPattern(Pattern pattern, String targetStr) {
        if (pattern == null || pattern.toString().length() == 0) {
            return false;
        }

        // targetStr 为空表示全部命中
        if (targetStr == null || targetStr.length() == 0) {
            return true;
        }

        // * 号表示全部命中
        if ("*".equals(pattern.toString()) || "*".equals(targetStr)) {
            return true;
        }

        return pattern.matcher(targetStr).find();
    }

    /**
     * 以正则表达式检查 [targetStrList 中任意字符串] 是否命中
     */
    public static boolean isHintString(String keywordRexExp, Collection<String> targetStrList) {
        if (keywordRexExp == null || keywordRexExp.length() == 0) {
            return false;
        }

        // targetStrList 为空表示全部命中
        if (targetStrList == null || targetStrList.isEmpty()) {
            return true;
        }

        // * 号表示全部命中
        if ("*".equals(keywordRexExp) || targetStrList.contains("*")) {
            return true;
        }

        for (String targetStr : targetStrList) {
            if (Pattern.compile(keywordRexExp).matcher(targetStr).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 以正则表达式检查 [targetStrList 中任意字符串] 是否命中
     */
    public static boolean isHintPattern(Pattern pattern, Collection<String> targetStrList) {
        if (pattern == null || pattern.toString().length() == 0) {
            return false;
        }

        // targetStrList 为空表示全部命中
        if (targetStrList == null || targetStrList.isEmpty()) {
            return true;
        }

        // * 号表示全部命中
        if ("*".equals(pattern.toString()) || targetStrList.contains("*")) {
            return true;
        }

        for (String targetStr : targetStrList) {
            if (pattern.matcher(targetStr).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 以正则表达式检查 targetStr 中是否存在命中 [keywordRexExpList 中任意字符串] 的字符串
     */
    public static boolean isHintString(Collection<String> keywordRexExpList, String targetStr) {
        if (keywordRexExpList == null || keywordRexExpList.isEmpty()) {
            return false;
        }

        // targetStr 为空表示全部命中
        if (targetStr == null || targetStr.length() == 0) {
            return true;
        }

        // * 号表示全部命中
        if (keywordRexExpList.contains("*") || "*".equals(targetStr)) {
            return true;
        }

        for (String keywordRexExp : keywordRexExpList) {
            if (keywordRexExp.length() != 0 &&
                    Pattern.compile(keywordRexExp).matcher(targetStr).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 以正则表达式检查 targetStr 中是否存在命中 [keywordPatternList 中任意字符串] 的字符串
     */
    public static boolean isHintPattern(Collection<Pattern> keywordPatternList, String targetStr) {
        if (keywordPatternList == null || keywordPatternList.isEmpty()) {
            return false;
        }

        // targetStr 为空表示全部命中
        if (targetStr == null || targetStr.length() == 0) {
            return true;
        }

        // * 号表示全部命中
        for (Pattern pattern : keywordPatternList) {
            if ("*".equals(pattern.toString())) {
                return true;
            }
        }

        if ("*".equals(targetStr)) {
            return true;
        }

        for (Pattern pattern : keywordPatternList) {
            if (pattern.toString().length() != 0 &&
                    pattern.matcher(targetStr).find()) {
                return true;
            }
        }

        return false;
    }


    /**
     * 以正则表达式检查 [targetStrList 中任意字符串] 中是否存在命中 [keywordRexExpList 中任意字符串] 的字符串
     */
    public static boolean isHintString(Collection<String> keywordRexExpList, Collection<String> targetStrList) {
        if (keywordRexExpList == null || keywordRexExpList.isEmpty()) {
            return false;
        }

        // targetStrList 为空表示全部命中
        if (targetStrList == null || targetStrList.isEmpty()) {
            return true;
        }

        // * 号表示全部命中
        if (keywordRexExpList.contains("*") || targetStrList.contains("*")) {
            return true;
        }

        for (String keywordRexExp : keywordRexExpList) {
            if (keywordRexExp.length() == 0) {
                // 跳过检查空字符串
                continue;
            }
            for (String targetStr : targetStrList) {
                if (Pattern.compile(keywordRexExp).matcher(targetStr).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 以正则表达式检查 [targetStrList 中任意字符串] 中是否存在命中 [keywordRexExpList 中任意字符串] 的字符串
     */
    public static boolean isHintPattern(Collection<Pattern> keywordPatternList, Collection<String> targetStrList) {
        if (keywordPatternList == null || keywordPatternList.isEmpty()) {
            return false;
        }

        // targetStrList 为空表示全部命中
        if (targetStrList == null || targetStrList.isEmpty()) {
            return true;
        }


        // * 号表示全部命中
        for (Pattern pattern : keywordPatternList) {
            if ("*".equals(pattern.toString())) {
                return true;
            }
        }

        if (targetStrList.contains("*")) {
            return true;
        }

        for (Pattern pattern : keywordPatternList) {
            if (pattern.toString().length() == 0) {
                // 跳过检查空字符串
                continue;
            }
            for (String targetStr : targetStrList) {
                if (pattern.matcher(targetStr).find()) {
                    return true;
                }
            }
        }

        return false;
    }
}
