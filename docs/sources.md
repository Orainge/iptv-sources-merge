# 直播源来源配置文件

  文件：[sources.json](sources.json)

## 配置结构

该配置文件采用 JSON 格式，主要用于定义直播源的来源信息。每个来源可以限定适用的频道分类和频道，并可配置是否使用代理访问。

```json
[
    {
        "id": "1",
        "name": "官方直播源",
        "category": ["体育", "新闻"],
        "channel": ["CCTV-5", "CCTV-13"],
        "enableProxy": false,
        "url": "http://example.com/live/source1.txt"
    },
    {
        "id": "2",
        "name": "地方频道",
        "category": ["地方频道"],
        "channel": ["*"],
        "enableProxy": true,
        "url": "http://example.com/live/local_channels.txt"
    }
]
```

## 字段说明

| 字段名      | 类型    | 说明                                                         |
| ----------- | ------- | ------------------------------------------------------------ |
| id          | string  | 直播源的唯一标识符                                           |
| name        | string  | 直播源的名称                                                 |
| category    | array   | 该直播源适用的频道分类，支持正则匹配，`*` 代表适用于所有分类 |
| channel     | array   | 该直播源适用的频道名称，支持正则匹配，`*` 代表适用于所有频道 |
| enableProxy | boolean | 是否启用代理来获取该直播源                                   |
| url         | string  | 直播源的 URL 地址                                            |

## 示例

```json
[
    {
        "id": "3",
        "name": "国际新闻",
        "category": ["新闻"],
        "channel": ["BBC News", "CNN", "Al Jazeera"],
        "enableProxy": true,
        "url": "http://example.com/live/international_news.txt"
    }
]
```

上述示例表示：

- 直播源 ID 为 `3`，名称为“国际新闻”
- 该直播源适用于“新闻”分类
- 该直播源适用于 `BBC News`、`CNN`、`Al Jazeera` 频道
- 该直播源需要启用代理访问

## 注意事项

1. `category` 和 `channel` 字段支持正则表达式匹配，如 `"^CCTV.*$"` 可匹配所有以 `CCTV` 开头的频道。
2. `enableProxy` 设为 `true` 时，表示该直播源需要通过代理访问。
3. `url` 字段应指向有效的直播源地址。
