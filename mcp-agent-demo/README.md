# Spring AI 2.0.0 MCP Agent Client Demo

这个目录只包含 Agent Client 代码。

对应的 MCP Server 在上一级项目：

```text
/Users/dilee/Projects/springai-deepseek-demo/src
```

Client 侧演示：

```text
Spring AI 2.0.0 MCP Client
        ↓ ToolCallbackProvider
ChatClient + tools(...)
        ↓
Agent 调用 MCP Tool 得到最终回答
```

Client 侧使用 DeepSeek：

```text
deepseek-v4-flash
```

启动 Client 前需要配置：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

MCP Server 不需要模型 API Key，它只负责暴露工具。

`mcp-agent-demo` 会用 DeepSeek 调用模型，并把远端 MCP Tool 交给 `ChatClient.tools(...)`。

## 1. 先启动 MCP Server

```bash
cd /Users/dilee/Projects/springai-deepseek-demo
./mvnw spring-boot:run
```

默认 endpoint：

```text
http://localhost:8080/mcp
```

如果 8080 被占用：

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=18080
```

## 2. 启动 Agent Client

默认连接 `http://localhost:8080/mcp`：

```bash
cd /Users/dilee/Projects/springai-deepseek-demo/mcp-agent-demo
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
./mvnw spring-boot:run \
  -Dspring-boot.run.main-class=com.example.mcpagent.client.McpAgentDemoClientApplication
```

如果 Server 跑在 18080：

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.main-class=com.example.mcpagent.client.McpAgentDemoClientApplication \
  -Dspring-boot.run.arguments=--spring.ai.mcp.client.streamable-http.connections.travel-expense.url=http://localhost:18080
```

正常输出会包含：

```text
Spring AI tools: [check_lodging_policy]
Spring AI agent result: 我已经通过 MCP 工具 check_lodging_policy 查询了差旅住宿规则。
```

最终回答里会包含：

```text
MANAGER_APPROVAL_REQUIRED
```

## 3. 代码结构

```text
client/McpAgentDemoClientApplication.java
```

关键写法是：

```java
String result = chatClient.prompt("...")
    .tools(toolCallbackProvider)
    .call()
    .content();
```

`tools(...)` 是 Spring AI 2.0.0 推荐写法；旧的 `toolCallbacks(...)` 已经不建议继续使用。

## IDEA 报包不存在怎么办

如果 IDEA 里 `org.springframework.ai.chat.client` 或 `org.springframework.ai.mcp.client` 报红，但命令行执行下面命令能通过：

```bash
./mvnw -DskipTests compile
```

说明 IDEA 没有把当前目录当成 Maven 项目导入。

处理方式：

```text
右键 mcp-agent-demo/pom.xml
-> Add as Maven Project
-> Maven 面板 Reload All Maven Projects
```
