# Spring AI 2.0.0 MCP Agent Demo

这个目录是一个独立 Maven 示例项目，演示：

```text
Spring Boot 4.1.0 MCP Server
        ↓ Streamable HTTP
Spring AI 2.0.0 MCP Client
        ↓ ToolCallbackProvider
ChatClient + tools(...)
        ↓
Agent 调用 MCP Tool 得到最终回答
```

这个示例不需要配置大模型 API Key。`TravelExpenseAgentModel` 是一个本地模拟模型，只用于稳定触发 Spring AI 的 Tool Calling 流程，验证 MCP Tool 能被 Agent 侧通过 `ChatClient.tools(...)` 使用。

## 1. 启动 MCP Server

```bash
cd /Users/dilee/Projects/springai-deepseek-demo/mcp-agent-demo
./mvnw spring-boot:run -Dspring-boot.run.main-class=com.example.mcpagent.server.McpAgentDemoServerApplication
```

默认 endpoint：

```text
http://localhost:8080/mcp
```

如果 8080 被占用：

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.main-class=com.example.mcpagent.server.McpAgentDemoServerApplication \
  -Dspring-boot.run.arguments=--server.port=18080
```

## 2. 启动 Agent Client

默认连接 `http://localhost:8080/mcp`：

```bash
./mvnw spring-boot:run -Dspring-boot.run.main-class=com.example.mcpagent.client.McpAgentDemoClientApplication
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
server/McpAgentDemoServerApplication.java
server/tool/TravelExpenseTools.java
server/tool/LodgingPolicyResult.java
client/McpAgentDemoClientApplication.java
client/TravelExpenseAgentModel.java
```

关键写法是：

```java
String result = chatClient.prompt("...")
    .tools(toolCallbackProvider)
    .call()
    .content();
```

`tools(...)` 是 Spring AI 2.0.0 推荐写法；旧的 `toolCallbacks(...)` 已经不建议继续使用。
