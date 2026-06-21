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

这个 Demo 不需要配置大模型 API Key。`mcp-agent-demo` 里的 `TravelExpenseAgentModel` 是本地模拟模型，只用于稳定触发工具调用流程。

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

如果 `8080` 被占用：

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=18080
```

## 2. 启动 Agent Client

默认连接 `http://localhost:8080/mcp`：

```bash
cd /Users/dilee/Projects/springai-deepseek-demo/mcp-agent-demo
./mvnw -DskipTests compile
./mvnw spring-boot:run \
  -Dspring-boot.run.main-class=com.example.mcpagent.client.McpAgentDemoClientApplication
```

如果 Server 跑在 `18080`：

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

## IDEA 注意点

根项目和 `mcp-agent-demo` 是两个 Maven 项目。

如果 IDEA 里 Client 代码报 `org.springframework.ai.chat.client` 不存在，通常是 `mcp-agent-demo/pom.xml` 没有被导入。

在 Maven 面板确认同时存在：

```text
springai-deepseek-demo
mcp-agent-demo
```

如果没有，右键 `mcp-agent-demo/pom.xml`，选择 `Add as Maven Project`。
