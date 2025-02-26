# 处理规则配置文件

  文件：[config.json](config.json)

## 配置结构

该配置文件用于管理默认直播源的筛选和代理配置，包含来源配置、分类配置、URL 过滤、连接性检查、代理访问等功能。

```json
{
    "name": "通用直播源", 
    "fileName": "general_streams.txt", 
    "sourceConfigUrl": "http://example.com/api/sources/general.json", 
    "categoryConfigUrl": "http://example.com/api/category/general.json", 
    "enableSourceConfigUrlProxy": false,
    "enableCategoryConfigUrlProxy": false,
    "excludeSourceUrlKeyword": [
        "^rtp:", "^http(s)?://raw.git", "iptv.example.com", "cdn.example.net"
    ],
    "checkConnection": true,
    "excludeCheckConnectionUrlKeyword": [
        "192.168.1.100:1234", "testcdn.example.com", "private.example.org"
    ],
    "urlType": {
        "v4": {
            "enable": true
        },
        "v6": {
            "enable": true
        },
        "domain": {
            "enable": true,
            "checkIpType": false
        }
    },
    "proxy": {
        "enable": false,
        "api": {
            "url": "http://example.com/api/proxy/getUrl",
            "method": "POST",
            "data": {
                "TMP": "${timestamp}",
                "ATK": "TOKEN",
                "URL": "${url}",
                "ENP": "${externalProxy}"
            }
        },
        "filter": [{
            "urlType": ["v4", "v6", "domain"],
            "category":  ["国际"],
            "channelName": ["XXX World"],
            "channelUrl": ["*"],
            "externalProxy": true
        }]
    }
}
```

## 字段说明

| 字段名                           | 类型    | 说明                                    |
| -------------------------------- | ------- | --------------------------------------- |
| name                             | string  | 配置文件名称                            |
| fileName                         | string  | 生成的文件名，目前暂无使用              |
| sourceConfigUrl                  | string  | 直播源来源配置文件 URL                  |
| categoryConfigUrl                | string  | 频道分类配置文件 URL                    |
| enableSourceConfigUrlProxy       | boolean | 是否使用代理获取直播源来源配置文件      |
| enableCategoryConfigUrlProxy     | boolean | 是否使用代理获取频道分类配置文件        |
| excludeSourceUrlKeyword          | array   | 需要排除的直播源 URL，支持正则匹配      |
| checkConnection                  | boolean | 是否检查直播源的连接性                  |
| excludeCheckConnectionUrlKeyword | array   | 在检查连接性时排除的 URL，支持正则匹配  |
| urlType                          | object  | 按 URL 类型筛选                         |
| urlType.v4.enable                | boolean | 是否包含 IPv4 地址的直播源              |
| urlType.v6.enable                | boolean | 是否包含 IPv6 地址的直播源              |
| urlType.domain.enable            | boolean | 是否包含域名地址的直播源                |
| urlType.domain.checkIpType       | boolean | 是否检查域名的 IP 类型（IPv4 或 IPv6）  |
| proxy                            | object  | 代理配置                                |
| proxy.enable                     | boolean | 是否需要代理 URL 访问                   |
| proxy.api                        | object  | 代理 API 配置                           |
| proxy.api.url                    | string  | 代理 API 地址                           |
| proxy.api.method                 | string  | 请求方法（如 `POST`）                   |
| proxy.api.data                   | object  | 请求数据格式                            |
| proxy.filter                     | array   | 代理过滤规则                            |
| proxy.filter.urlType             | array   | 适用的 URL 类型（`v4`、`v6`、`domain`） |
| proxy.filter.category            | array   | 适用的频道分类（支持正则）              |
| proxy.filter.channelName         | array   | 适用的频道名称（支持正则）              |
| proxy.filter.channelUrl          | array   | 适用的频道 URL（支持正则）              |
| proxy.filter.externalProxy       | boolean | 是否启用外部代理                        |

## 示例说明

上述示例表示：

- 该配置文件名称为“通用直播源”。
- 直播源来源和频道分类的 URL 地址指向 `example.com`。
- 连接性检查开启，但部分 URL 会被排除。
- 直播源支持 IPv4、IPv6 和域名地址。
- 代理功能关闭，但可以根据 `proxy.filter` 设置特定频道启用代理。
- `proxy.api.data`中，可用的额外参数：
  - ${timestamp} 时间戳
  - ${url} 原始直播源 URL
  - ${externalProxy} 是否启用外部代理

- `proxy.filter.externalProxy`：此处设置的值会替换到`proxy.api.data`中配置的`${externalProxy}`

## 注意事项

1. `excludeSourceUrlKeyword` 和 `excludeCheckConnectionUrlKeyword` 采用正则匹配，确保格式正确。
2. `proxy`的配置模板依赖项目 [Orainge/m3u8-proxy-server](https://github.com/Orainge/m3u8-proxy-server)，具体使用方法查看该项目说明文件。
3. `proxy.filter` 可用于特定频道的直播源代理请求，适用于地理限制的内容访问。
