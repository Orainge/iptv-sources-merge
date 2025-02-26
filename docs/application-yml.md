## 配置项说明

### 1. 服务器配置

```yml
server:
  port: 19980
```

- **port**：指定服务器监听的端口号，默认为 `19980`。

### 2. 日志级别配置

```yml
logging:
  level:
    "com.orainge.iptv.sources_merge": INFO
    "com.orainge.tools": INFO
```

- **logging.level**：设置不同模块的日志级别。
  - `com.orainge.iptv.sources_merge`：如果日志级别设为 `DEBUG`，用于 IPTV 直播源合并服务的调试信息。
  - `com.orainge.tools`：如果日志级别设为 `DEBUG`，用于工具类的调试信息。

### 3. 服务器外部代理配置

```yml
server-config:
  external-proxy:
    server-list:
      - url: "127.0.0.1"
        port: 1080
        type: "http"
        host:
          - "*"
```

- **server-config.external-proxy**：外部代理服务器相关配置。
  - **server-list**：定义代理服务器列表。
    - **url**：代理服务器地址。
    - **port**：代理服务器端口，示例中 `1080` 端口用于 HTTP 代理。
    - **type**：代理类型，如 `http`、`socks5` 等。
    - **host**：允许代理的主机，`*` 表示所有主机均可代理。

### 4. IP 直播源合并配置

```yml
ip-sources-merge:
  no-source-url: http://127.0.0.1/no-signal
```

- **ip-sources-merge.no-source-url**：当没有可用直播源时，返回的占位 URL，通常用于指向一个默认的“无信号”画面。

## 说明

- 该配置文件采用 YAML 格式，确保缩进正确，以避免解析错误。
- 代理配置可根据实际需求进行修改，例如更换代理类型或端口。
- 日志级别可调整为 `INFO` 或 `ERROR` 以减少日志输出量，提高运行效率。
- 若 `no-source-url` 为空，则当无可用直播源时可能会导致播放失败。