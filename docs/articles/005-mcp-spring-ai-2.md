# 005 Spring AI MCP 2.0.0

对应文章：

《Spring AI 2.0.0 接入 MCP：为什么要从 toolCallbacks 改成 tools》

## 版本说明

这份代码使用：

```text
Spring Boot 4.1.0
Spring AI 2.0.0
Java 17
deepseek-v4-flash
```

Spring AI 2.0.x 文档标注支持 Spring Boot 4.0.x / 4.1.x，所以不要只把旧项目里的 `spring-ai.version` 改成 `2.0.0`。

另外，Spring AI 2.0.0 中 `ChatClientRequestSpec.toolCallbacks(...)` 已标记为 deprecated，推荐使用：

```java
.tools(mcpTools)
```

## 运行前准备

设置 DeepSeek API Key：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

准备 MCP Filesystem Server 的测试目录：

```bash
mkdir -p /private/tmp/spring-ai-mcp-demo
echo "Hello from Spring AI MCP Demo" > /private/tmp/spring-ai-mcp-demo/test.txt
```

macOS 上建议直接使用 `/private/tmp`。`/tmp` 通常是指向 `/private/tmp` 的软链接，如果配置里允许的是 `/private/tmp`，但问题里让模型读取 `/tmp/...`，Filesystem Server 可能会按安全规则拒绝访问。

本机需要安装 Node.js 和 `npx`。应用启动时，Spring AI 会根据 `application.yaml` 自动通过 `npx` 拉起 Filesystem MCP Server。

macOS 上偶尔会看到 Netty DNS native resolver 的提示日志。它不是 MCP 工具调用失败的原因。这个 Demo 里直接用 `logging.level` 关掉这条日志，不要求读者按不同机器架构额外加 native 依赖。

## 启动项目

```bash
./mvnw spring-boot:run
```

## 测试接口

读取测试文件：

```bash
curl --get "http://localhost:8080/ask" \
  --data-urlencode "question=帮我读取 /private/tmp/spring-ai-mcp-demo/test.txt 的内容"
```

列出测试目录：

```bash
curl --get "http://localhost:8080/ask" \
  --data-urlencode "question=列出 /private/tmp/spring-ai-mcp-demo 目录下有哪些文件"
```

如果日志里出现 `Access denied - path outside allowed directories`，说明模型请求了白名单之外的路径。这个错误是 Filesystem MCP Server 的安全拦截，不是应用没接上 MCP。

## 对应代码

```text
pom.xml
    -> Spring Boot Web、Spring AI BOM、DeepSeek starter、MCP Client starter

src/main/resources/application.yaml
    -> deepseek-v4-flash 模型配置、MCP stdio filesystem 连接配置

src/main/java/com/example/springaideepseekdemo/controller/McpController.java
    -> 注入 ToolCallbackProvider，并通过 .tools(mcpTools) 注册 MCP 工具
```

## IDEA 配置 DeepSeek Key

如果用 IDEA 直接运行 `SpringaiDeepseekDemoApplication`：

1. 打开 Run/Debug Configurations；
2. 选择当前 Spring Boot 启动配置；
3. 在 Environment variables 里添加：

```text
DEEPSEEK_API_KEY=你的 DeepSeek API Key
```
