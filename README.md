# Spring AI 2.0.0 Loop Engineering Demo

这个分支对应文章：

```text
Spring AI 2.0.0 实战 Loop Engineering：让 Agent 不只是调用工具，而是把事做完
```

示例做的是一个 Java 日志分析 Agent：

```text
用户贴一段异常日志
-> Agent 识别异常、traceId、服务名
-> 按需要调用 Java Tool 查询链路日志、发布记录和排障手册
-> 汇总成排查结论
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
curl -X POST "http://localhost:8080/log-agent/analyze" \
  -H "Content-Type: text/plain" \
  --data-binary '2026-06-21 10:15:31.240 ERROR [order-service,traceId=TR-20260621-001] c.e.order.OrderController - create order failed
java.lang.NullPointerException: Cannot invoke "com.example.User.getId()" because "user" is null
    at com.example.order.UserContext.currentUserId(UserContext.java:42)
    at com.example.order.OrderService.createOrder(OrderService.java:88)
    at com.example.order.OrderController.create(OrderController.java:31)'
```

控制台能看到类似日志，说明 Spring AI 的 Tool Calling 链路被触发：

```text
findLogsByTraceId tool called, traceId=TR-20260621-001
getRecentDeployments tool called, serviceName=order-service
searchRunbook tool called, keyword=NullPointerException
```

## 关键代码

- `src/main/java/com/example/springaideepseekdemo/controller/LogAgentController.java`
- `src/main/java/com/example/springaideepseekdemo/tool/LogAnalysisTools.java`
- `src/main/resources/application.yaml`
