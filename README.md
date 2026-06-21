# Spring AI MCP Server Demo

这个仓库按文章分支保存 Spring AI 示例代码。

当前示例对应：

`Spring Boot 4.1.0 实现 MCP Server：让 Java 方法变成 Agent 可调用工具`

## 这个示例做什么

用 Spring Boot 4.1.0 + Spring AI 2.0.0 暴露一个 Streamable HTTP MCP Server。

示例把 Java 方法：

```text
TravelExpenseTools#checkLodgingPolicy
```

注册成 MCP Tool：

```text
check_lodging_policy
```

MCP Client 连接 `http://localhost:8080/mcp` 后，可以通过 `tools/list` 发现工具，再通过 `tools/call` 调用工具。

## 运行

```bash
./mvnw -DskipTests compile
./mvnw spring-boot:run
```

这个示例不需要配置大模型 API Key。

MCP Server 只负责暴露工具；模型和 Agent 什么时候调用工具，是 MCP Client 侧的事情。

## 验证

支持 Streamable HTTP 的 MCP Client 可以连接：

```text
http://localhost:8080/mcp
```

工具参数示例：

```json
{
  "city": "上海",
  "amount": 720,
  "hasHotelInvoice": true,
  "hasItinerary": true
}
```

预期结果里会包含：

```json
{
  "decision": "MANAGER_APPROVAL_REQUIRED",
  "overLimit": true,
  "missingMaterials": false
}
```
