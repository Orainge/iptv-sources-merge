package com.orainge.iptv.sources_merge.bean.generate.category;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

/**
 * 频道配置
 */
@Data
public class ChannelConfigItem {
    /**
     * 频道名称
     */
    private String name;

    /**
     * 频道关键字<br>
     * 直播源列表里包含该关键字即可归类到同一个频道里<br>
     * 以正则表达式进行判断
     */
    private List<String> keyword;

    /**
     * 频道关键字<br>
     * 直播源列表里包含该关键字即可归类到同一个频道里<br>
     * 以正则表达式进行判断
     */
    private List<Pattern> keywordPattern;

    /**
     * 需要排除的 URL 关键字<br>
     * 在筛选完频道关键字后，检查 URL 是否满足该关键字，满足就要排除<br>
     * 以正则表达式进行判断
     */
    private List<String> excludeUrlKeyword;

    /**
     * 需要排除的 URL 关键字<br>
     * 在筛选完频道关键字后，检查 URL 是否满足该关键字，满足就要排除<br>
     * 以正则表达式进行判断
     */
    private List<Pattern> excludeUrlKeywordPattern;

    /**
     * 直播源排序<br>
     * 以直播源 ID 进行区分
     */
    private List<String> sourceOrder;

    /**
     * 合并后的直播源列表
     */
    private CopyOnWriteArraySet<ChannelSourceItem> mergeSourceSet = new CopyOnWriteArraySet<>();

    /**
     * 设置 keyword, 在内部转化为 Pattern
     */
    public void setKeyword(List<String> keywordList) {
        this.keyword = keywordList;
        if (keywordList != null && !keywordList.isEmpty()) {
            keywordPattern = new LinkedList<>();
            for (String keyword : keywordList) {
                keywordPattern.add(Pattern.compile(keyword));
            }
        }
    }

    /**
     * 设置 excludeUrlKeyword, 在内部转化为 Pattern
     */
    public void setExcludeUrlKeyword(List<String> excludeUrlKeyword) {
        this.excludeUrlKeyword = excludeUrlKeyword;
        if (excludeUrlKeyword != null && !excludeUrlKeyword.isEmpty()) {
            excludeUrlKeywordPattern = new LinkedList<>();
            for (String keyword : excludeUrlKeyword) {
                excludeUrlKeywordPattern.add(Pattern.compile(keyword));
            }
        }
    }
}