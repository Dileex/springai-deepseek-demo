# 010 MCP Server Demo

这个示例对应文章：

```text
Spring Boot 4.1.0 实现 MCP Server：让 Java 方法变成 Agent 可调用工具
```

目录分工：

```text
src/              MCP Server
mcp-agent-demo/   Agent Client
```

## 示例目标

用 Spring Boot 4.1.0 + Spring AI 2.0.0 跑通这条链路：

```text
Java 方法
  -> MCP Tool
  -> Streamable HTTP MCP Server
  -> Spring AI MCP Client
  -> ToolCallbackProvider
  -> ChatClient.tools(...)
  -> Agent 得到最终回答
```

## 运行环境

- JDK 17+
- Maven
- DeepSeek API Key

MCP Server 不需要模型 API Key，它只负责暴露订单查询工具。

Agent Client 使用 DeepSeek：

```text
deepseek-v4-flash
```

启动 Client 前需要配置：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

Agent 的 system prompt、用户问题模板、默认问题和期望工具名放在 `mcp-agent-demo/src/main/resources/application.yaml` 的 `demo.agent` 下。

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

## IDEA 注意点

根项目和 `mcp-agent-demo` 是两个 Maven 项目。

如果 IDEA 里 Client 代码报 `org.springframework.ai.chat.client` 不存在，通常是 `mcp-agent-demo/pom.xml` 没有被导入。

在 Maven 面板确认同时存在：

```text
springai-deepseek-demo
mcp-agent-demo
```

如果没有，右键 `mcp-agent-demo/pom.xml`，选择 `Add as Maven Project`。
