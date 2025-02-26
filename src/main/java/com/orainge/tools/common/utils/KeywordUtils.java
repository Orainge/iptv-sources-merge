package com.orainge.tools.common.utils;

import java.util.*;

/**
 * 字符串工具类
 */
public class KeywordUtils {
    /**
     * 检查 targetStr 中是否存在部分相等 keyword 的字符串
     */
    public static boolean isHint(String keyword, String targetStr) {
        if (keyword == null || keyword.length() == 0) {
            return false;
        }

        // targetStr 为空表示全部命中
        if (targetStr == null || targetStr.length() == 0) {
            return true;
        }

        // * 号表示全部命中
        if ("*".equals(keyword) || "*".equals(targetStr)) {
            return true;
        }

        return targetStr.contains(keyword);
    }

    /**
     * 检查 [targetStrList 中任意字符串] 中是否存在部分相等 keyword 的字符串
     */
    public static boolean isHint(String keyword, Collection<String> targetStrList) {
        if (keyword == null || keyword.length() == 0) {
            return false;
        }

        // targetStrList 为空表示全部命中
        if (targetStrList == null || targetStrList.isEmpty()) {
            return true;
        }

        // * 号表示全部命中
        if ("*".equals(keyword) || targetStrList.contains("*")) {
            return true;
        }

        for (String targetStr : targetStrList) {
            if (targetStr.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查 targetStr 中是否存在部分相等 [keywordList 中任意字符串] 的字符串
     */
    public static boolean isHint(Collection<String> keywordList, String targetStr) {
        if (keywordList == null || keywordList.isEmpty()) {
            return false;
        }

        // targetStr 为空表示全部命中
        if (targetStr == null || targetStr.length() == 0) {
            return true;
        }

        // * 号表示全部命中
        if (keywordList.contains("*") || "*".equals(targetStr)) {
            return true;
        }

        for (String keyword : keywordList) {
            if (keyword.length() != 0 && targetStr.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查 [targetStrList 中任意字符串] 中是否存在部分相等 [keywordList 中任意字符串] 的字符串
     */
    public static boolean isHint(Collection<String> keywordList, Collection<String> targetStrList) {
        if (keywordList == null || keywordList.isEmpty()) {
            return false;
        }

        // targetStrList 为空表示全部命中
        if (targetStrList == null || targetStrList.isEmpty()) {
            return true;
        }

        // * 号表示全部命中
        if (keywordList.contains("*") || targetStrList.contains("*")) {
            return true;
        }

        for (String keyword : keywordList) {
            if (keyword.length() == 0) {
                // 跳过检查空字符串
                continue;
            }
            for (String targetStr : targetStrList) {
                if (targetStr.contains(keyword)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查 targetStr 中是否存在完全相等 keyword 的字符串
     */
    public static boolean isHintFull(String keyword, String targetStr) {
        if (keyword == null || keyword.length() == 0) {
            return false;
        }

        // targetStr 为空表示全部命中
        if (targetStr == null || targetStr.length() == 0) {
            return true;
        }

        // * 号表示全部命中
        if ("*".equals(keyword) || "*".equals(targetStr)) {
            return true;
        }

        return targetStr.equals(keyword);
    }

    /**
     * 检查 [targetStrList 中任意字符串] 中是否存在部分相等 keyword 的字符串
     */
    public static boolean isHintFull(String keyword, Collection<String> targetStrList) {
        if (keyword == null || keyword.length() == 0) {
            return false;
        }

        // targetStrList 为空表示全部命中
        if (targetStrList == null || targetStrList.isEmpty()) {
            return true;
        }

        // * 号表示全部命中
        if ("*".equals(keyword) || targetStrList.contains("*")) {
            return true;
        }

        for (String targetStr : targetStrList) {
            if (targetStr.equals(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查 targetStr 中是否存在部分相等 [keywordList 中任意字符串] 的字符串
     */
    public static boolean isHintFull(Collection<String> keywordList, String targetStr) {
        if (keywordList == null || keywordList.isEmpty()) {
            return false;
        }

        // targetStr 为空表示全部命中
        if (targetStr == null || targetStr.length() == 0) {
            return true;
        }

        // * 号表示全部命中
        if (keywordList.contains("*") || "*".equals(targetStr)) {
            return true;
        }

        for (String keyword : keywordList) {
            if (keyword.length() != 0 && targetStr.equals(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查 [targetStrList 中任意字符串] 中是否存在部分相等 [keywordList 中任意字符串] 的字符串
     */
    public static boolean isHintFull(Collection<String> keywordList, Collection<String> targetStrList) {
        if (keywordList == null || keywordList.isEmpty()) {
            return false;
        }

        // targetStrList 为空表示全部命中
        if (targetStrList == null || targetStrList.isEmpty()) {
            return true;
        }

        // * 号表示全部命中
        if (keywordList.contains("*") || targetStrList.contains("*")) {
            return true;
        }

        for (String keyword : keywordList) {
            if (keyword.length() == 0) {
                // 跳过检查空字符串
                continue;
            }
            for (String targetStr : targetStrList) {
                if (targetStr.equals(keyword)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 将字符串按照逗号分隔后，转换成小写，存储到 Set 中
     *
     * @param strings 带逗号的字符串
     */
    public static Set<String> toLowerStringSet(String strings) {
        if (strings == null || "".equals(strings)) {
            return null;
        }
        String[] stringList = strings.split(",");

        // 数组为空，直接返回 null
        if (stringList.length == 0) {
            return null;
        }

        // 转换成小写
        Set<String> set = new HashSet<>();
        for (String str : stringList) {
            if ("*".equals(str)) {
                // 表示全匹配，直接返回
                set = new HashSet<>();
                set.add("*");
                return set;
            }
            set.add(str.toLowerCase());
        }
        return set;
    }

    /**
     * 检查字符串是否包含 Set 中的任何字符串（将字符串转换成小写后比较）
     */
    public static boolean containsAnyLowerString(String str, Set<String> targetStrings) {
        // 如果 set 为 null 或为空，则返回 false
        if (targetStrings == null) {
            return false;
        }

        str = str.toLowerCase();
        for (String target : targetStrings) {
            if (target.startsWith("*") || str.contains(target)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查字符串是否包含 Set 中的任何字符串（将字符串转换成小写后比较）<br>
     * 如果 Set 为 null， 则返回 null
     */
    public static Boolean containsAnyLowerStringWithNull(String str, Set<String> targetStrings) {
        // 如果 set 为 null 或为空，则返回 null
        if (targetStrings == null) {
            return null;
        }

        str = str.toLowerCase();
        for (String target : targetStrings) {
            if (target.startsWith("*") || str.contains(target)) {
                return true;
            }
        }

        return false;
    }
}
