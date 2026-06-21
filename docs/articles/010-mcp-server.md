# 010 MCP Server Demo

这个分支对应文章：

`Spring Boot 4.1.0 实现 MCP Server：让 Java 方法变成 Agent 可调用工具`

示例目标很单一：用 Spring Boot 4.1.0 + Spring AI 2.0.0 暴露一个 Streamable HTTP MCP Server，把 Java 方法 `checkLodgingPolicy` 注册成 MCP Tool。

## 运行环境

- JDK 17+
- Maven

这个 Demo 不需要配置大模型 API Key。MCP Server 只负责暴露工具，模型和 Agent 调用逻辑在 MCP Client 侧。

## 启动

```bash
./mvnw -DskipTests compile
./mvnw spring-boot:run
```

启动后默认端口是 `8080`，MCP Endpoint 是：

```text
http://localhost:8080/mcp
```

## MCP Client 配置

支持 Streamable HTTP 的 MCP Client 可以按下面的思路配置：

```json
{
  "mcpServers": {
    "travel-expense": {
      "type": "streamable-http",
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

不同客户端字段名可能略有差异，核心是地址和传输协议要匹配。

## 暴露的工具

工具名：

```text
check_lodging_policy
```

参数：

```json
{
  "city": "上海",
  "amount": 720,
  "hasHotelInvoice": true,
  "hasItinerary": true
}
```

预期工具结果：

```json
{
  "city": "上海",
  "amount": 720.0,
  "standard": 600.0,
  "overLimit": true,
  "missingMaterials": false,
  "decision": "MANAGER_APPROVAL_REQUIRED",
  "policyBasis": "住宿费需要酒店发票和行程单；超出城市标准时，需要直属主管审批。"
}
```
