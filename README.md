# Spring AI MCP Server + Agent Client Demo

这个仓库对应文章：

```text
Spring Boot 4.1.0 实现 MCP Server：让 Java 方法变成 Agent 可调用工具
```

目录分工：

```text
src/              Spring Boot 4.1.0 MCP Server
mcp-agent-demo/   Spring AI 2.0.0 Agent Client
```

根项目把 Java 方法 `queryOrderSnapshot` 暴露成 MCP Tool：

```text
query_order_snapshot
```

`mcp-agent-demo` 只负责连接这个 MCP Server，把远端工具转成 `ToolCallbackProvider`，再通过：

```java
chatClient.prompt("...")
    .tools(toolCallbackProvider)
    .call()
    .content();
```

让 Agent 侧使用工具。

Agent 的 system prompt、用户问题模板、默认问题和期望工具名放在 `mcp-agent-demo/src/main/resources/application.yaml` 的 `demo.agent` 下。

Java 代码只负责装配 `ChatClient`、接入工具，并通过 `/ask` 暴露一个可访问接口。

Client 侧使用 DeepSeek：

```text
deepseek-v4-flash
```

所以启动 Client 前需要配置：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

## 1. 启动 MCP Server

```bash
cd /Users/dilee/Projects/springai-deepseek-demo
./mvnw -DskipTests compile
./mvnw spring-boot:run
```

默认 endpoint：

```text
http://localhost:8080/mcp
```

## 2. 启动 Agent Client

默认连接 `http://localhost:8080/mcp`：

```bash
cd /Users/dilee/Projects/springai-deepseek-demo/mcp-agent-demo
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
./mvnw -DskipTests compile
./mvnw spring-boot:run \
  -Dspring-boot.run.main-class=com.example.mcpagent.client.McpAgentDemoClientApplication
```

正常启动后，Client 会监听：

```text
http://localhost:8081
```

启动日志会包含：

```text
Spring AI tools: [query_order_snapshot]
```

访问 `/ask`：

```bash
curl -X POST "http://localhost:8081/ask" \
  -H "Content-Type: text/plain" \
  --data-binary '我的订单 ORDER-20260621-1001 怎么还没到？能帮我催一下吗？'
```

MCP Server 侧会看到工具调用日志：

```text
queryOrderSnapshot tool called, orderNo=ORDER-20260621-1001
```

接口会返回：

```json
{
  "answer": "您的订单已发货，顺丰单号 SF1234567890，目前已到达上海浦东集散中心..."
}
```

这里不是本地模拟模型。

MCP Server 不需要模型 API Key，它只暴露工具。

`mcp-agent-demo` 作为 Agent Client，会用 DeepSeek 调用模型，再通过 `ChatClient.tools(...)` 触发远端 MCP Tool。

## IDEA 打开方式

如果在 IDEA 里看到 `mcp-agent-demo` 下的 `org.springframework.ai.chat.client` 等包报红，通常不是代码问题，而是子项目 Maven 没有导入。

确认 Maven 面板里有两个项目：

```text
springai-deepseek-demo
mcp-agent-demo
```

如果只看到根项目，右键 `mcp-agent-demo/pom.xml`，选择 `Add as Maven Project`，或者在 Maven 面板点击 Reload All Maven Projects。
