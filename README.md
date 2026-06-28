# Spring AI 2.0.0 Loop Engineering Agent

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
-> Runner 检查模型有没有把推测写成确定结论
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
      "typeName": "收到请求",
      "message": "收到日志分析请求，sessionId=s001"
    },
    {
      "type": "TASK_DISCOVERED",
      "typeName": "识别任务",
      "message": "traceId=TR-20260621-001, serviceName=order-service, exceptionType=NullPointerException, plannedActions=[findLogsByTraceId, getRecentDeployments, searchRunbook]"
    },
    {
      "type": "ACTIONS_PLANNED",
      "typeName": "规划动作",
      "message": "计划动作：[findLogsByTraceId, getRecentDeployments, searchRunbook]"
    },
    {
      "type": "LOOP_ITERATION_STARTED",
      "typeName": "开始循环",
      "message": "开始第 1 轮分析"
    },
    {
      "type": "MODEL_CALL_STARTED",
      "typeName": "调用模型",
      "message": "调用 ChatClient，并把 LogAnalysisTools 暴露给模型"
    },
    {
      "type": "VERIFICATION_PASSED",
      "typeName": "校验通过",
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

如果模型把“高度怀疑和发布有关”写成“确定由发布导致”，Runner 会触发第二轮反馈，要求它区分已确认事实和推测判断。

## 关键代码

- `src/main/java/com/example/springaideepseekdemo/agent/LogAgentRunner.java`
- `src/main/java/com/example/springaideepseekdemo/agent/LogTaskSignal.java`
- `src/main/java/com/example/springaideepseekdemo/controller/LogAgentController.java`
- `src/main/java/com/example/springaideepseekdemo/tool/LogAnalysisTools.java`
- `src/main/resources/application.yaml`
