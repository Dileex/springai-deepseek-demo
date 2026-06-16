# 004 Spring AI MCP

对应文章：

《Spring AI 接入 MCP：让模型调用外部工具服务》

代码版本：

```text
branch: article/004-mcp
```

## 运行前准备

设置 DeepSeek API Key：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

准备 MCP Filesystem Server 的测试目录：

```bash
mkdir -p /tmp/spring-ai-mcp-demo
echo "Hello from Spring AI MCP Demo" > /tmp/spring-ai-mcp-demo/test.txt
```

本机需要安装 Node.js 和 `npx`。应用启动时，Spring AI 会根据 `application.yaml` 自动通过 `npx` 拉起 Filesystem MCP Server。

## 启动项目

```bash
./mvnw spring-boot:run
```

## 测试接口

读取测试文件：

```bash
curl --get "http://localhost:8080/ask" \
  --data-urlencode "question=帮我读取 /tmp/spring-ai-mcp-demo/test.txt 的内容"
```

列出测试目录：

```bash
curl --get "http://localhost:8080/ask" \
  --data-urlencode "question=列出 /tmp/spring-ai-mcp-demo 目录下有哪些文件"
```

如果日志里出现 `Access denied - path outside allowed directories`，说明模型请求了白名单之外的路径。这个错误是 Filesystem MCP Server 的安全拦截，不是应用没接上 MCP。

## 对应代码

```text
pom.xml
    -> Spring Boot Web、Spring AI BOM、DeepSeek starter、MCP Client starter

src/main/resources/application.yaml
    -> DeepSeek 模型配置、MCP stdio filesystem 连接配置

src/main/java/com/example/springaideepseekdemo/controller/McpController.java
    -> 注入 ToolCallbackProvider，并在 ChatClient 中注册 MCP 工具
```

## IDEA 配置 DeepSeek Key

如果用 IDEA 直接运行 `SpringaiDeepseekDemoApplication`：

1. 打开 Run/Debug Configurations；
2. 选择当前 Spring Boot 启动配置；
3. 在 Environment variables 里添加：

```text
DEEPSEEK_API_KEY=你的 DeepSeek API Key
```
