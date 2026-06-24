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

示例里的 system prompt、用户问题模板、默认问题和期望工具名放在 `application.yaml` 的 `demo.agent` 下。

Java 代码负责装配 `ChatClient`、接入工具，并通过 `/ask` 暴露一个可访问接口。

## 1. 先启动 MCP Server

```bash
cd /Users/dilee/Projects/springai-deepseek-demo
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

## 3. 代码结构

```text
client/McpAgentDemoClientApplication.java
client/OrderAgentService.java
client/OrderAskController.java
```

关键写法是：

```java
String result = chatClient.prompt()
    .user(prompt)
    .tools(toolCallbackProvider)
    .call()
    .content();
```

在 ChatClient 这层，示例直接使用 `tools(...)` 接入 `ToolCallbackProvider`。

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
