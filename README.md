# Spring AI 2.0.0 Loop Engineering Demo

这个分支对应文章：

```text
Spring AI 2.0.0 实战 Loop Engineering：让 Agent 不只是调用工具，而是把事做完
```

示例做的是一个 Java 日志分析 Agent：

```text
用户贴一段异常日志
-> LogAgentRunner 接收请求
-> ChatClient + tools(...) 驱动模型和工具调用
-> ToolCallingAdvisor 处理工具调用循环
-> Runner 校验回答是否覆盖关键证据
-> 不通过则带反馈再跑一轮
-> 达到通过条件或最大轮次后停止
```

## 环境

```text
Java 17
Spring Boot 4.1.0
Spring AI 2.0.0
DeepSeek deepseek-v4-flash
```

启动前配置：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

## 编译

```bash
./mvnw -DskipTests compile
```

## 启动

```bash
./mvnw spring-boot:run
```

## 测试

```bash
curl -X POST "http://localhost:8080/agents/log/run" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "u001",
    "sessionId": "s001",
    "input": "2026-06-21 10:15:31.240 ERROR [order-service,traceId=TR-20260621-001] c.e.order.OrderController - create order failed\njava.lang.NullPointerException: Cannot invoke \"com.example.User.getId()\" because \"user\" is null\n    at com.example.order.UserContext.currentUserId(UserContext.java:42)\n    at com.example.order.OrderService.createOrder(OrderService.java:88)\n    at com.example.order.OrderController.create(OrderController.java:31)"
  }'
```

返回结构：

```json
{
  "answer": "模型生成的日志分析结果",
  "status": "COMPLETED",
  "attempts": 1,
  "stopReason": "VERIFICATION_PASSED",
  "events": [
    {
      "type": "RECEIVED_INPUT",
      "message": "收到日志分析请求，sessionId=s001"
    },
    {
      "type": "LOOP_ITERATION_STARTED",
      "message": "开始第 1 轮分析"
    },
    {
      "type": "VERIFICATION_PASSED",
      "message": "回答覆盖了关键结论、证据和下一步建议"
    }
  ]
}
```

控制台能看到类似日志，说明 Spring AI 的 Tool Calling 链路被触发：

```text
findLogsByTraceId tool called, traceId=TR-20260621-001
getRecentDeployments tool called, serviceName=order-service
searchRunbook tool called, keyword=NullPointerException
```

## 关键代码

- `src/main/java/com/example/springaideepseekdemo/agent/LogAgentRunner.java`
- `src/main/java/com/example/springaideepseekdemo/controller/LogAgentController.java`
- `src/main/java/com/example/springaideepseekdemo/tool/LogAnalysisTools.java`
- `src/main/resources/application.yaml`
