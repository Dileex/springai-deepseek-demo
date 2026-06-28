# Article 012: Spring AI 2.0.0 Loop Engineering

对应文章：

```text
Spring AI 2.0.0 实战 Loop Engineering：让 Agent 不只是调用工具，而是把事做完
```

对应分支：

```text
article/012-loop-engineering
```

这个示例实现的是一个 Java 日志分析 Agent：

```text
用户提交异常日志
-> LogAgentRunner 提取 traceId、服务名、异常类型
-> ChatClient 暴露 LogAnalysisTools 给模型
-> Spring AI 处理工具调用
-> Runner 校验回答是否覆盖关键证据
-> Runner 检查模型有没有把推测写成确定结论
-> 不通过则带反馈再跑一轮
-> 通过或达到最大轮次后停止
```

## 运行环境

```text
Java 17
Spring Boot 4.1.0
Spring AI 2.0.0
DeepSeek deepseek-v4-flash
```

启动前配置 DeepSeek Key：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

## 编译启动

```bash
./mvnw -DskipTests compile
./mvnw spring-boot:run
```

## 请求接口

```bash
curl -X POST "http://localhost:8080/agents/log/run" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "u001",
    "sessionId": "s001",
    "input": "2026-06-21 10:15:31.240 ERROR [order-service,traceId=TR-20260621-001] c.e.order.OrderController - create order failed\njava.lang.NullPointerException: Cannot invoke \"com.example.User.getId()\" because \"user\" is null\n    at com.example.order.UserContext.currentUserId(UserContext.java:42)\n    at com.example.order.OrderService.createOrder(OrderService.java:88)\n    at com.example.order.OrderController.create(OrderController.java:31)"
  }'
```

## 返回重点

返回结构里重点看两块：

```text
answer：模型生成的排障结论
events：Agent 执行过程
```

`events` 里会看到类似流程：

```text
收到请求
识别任务
规划动作
开始循环
调用模型
校验通过
循环停止
```

控制台能看到工具调用日志：

```text
findLogsByTraceId tool called, traceId=TR-20260621-001
getRecentDeployments tool called, serviceName=order-service
searchRunbook tool called, keyword=NullPointerException
```

看到这些日志，说明模型确实触发了 Spring AI Tool Calling 链路。

如果模型把“高度怀疑和发布有关”写成“确定由发布导致”，Runner 会触发第二轮反馈，要求它区分已确认事实和推测判断。

## 关键代码

```text
src/main/java/com/example/springaideepseekdemo/agent/AgentRunRequest.java
src/main/java/com/example/springaideepseekdemo/agent/AgentRunResult.java
src/main/java/com/example/springaideepseekdemo/agent/AgentEvent.java
src/main/java/com/example/springaideepseekdemo/agent/LogTaskSignal.java
src/main/java/com/example/springaideepseekdemo/agent/LogAgentRunner.java
src/main/java/com/example/springaideepseekdemo/controller/LogAgentController.java
src/main/java/com/example/springaideepseekdemo/tool/LogAnalysisTools.java
src/main/resources/application.yaml
```

## 和文章的对应关系

文章里的四步，对应代码如下：

```text
建任务单：LogTaskSignal
查证据：LogAnalysisTools + ChatClient.tools(...)
做验收：LogAgentRunner.verify(...)
决定下一步：LogAgentRunner.run(...)
```

`application.yaml` 里放 Prompt，Java 代码主要负责组装流程、暴露工具、记录事件和校验结果。
