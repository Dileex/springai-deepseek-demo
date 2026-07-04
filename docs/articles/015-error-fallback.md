# Article 015: Spring AI 2.0.0 Error Fallback

对应文章：

```text
Spring AI 2.0.0 错误兜底实战：别让 AI 接口失败时只剩 500
```

对应分支：

```text
article/015-error-fallback
```

这个示例实现的是一个流式内部制度问答接口：

```text
用户提交制度问题
-> PolicyAskController 暴露 SSE 接口
-> PolicyAnswerService 把成功片段和失败兜底都包装成 PolicyStreamEvent
-> DeepSeekPolicyModelClient 使用 ChatClient.tools(...).stream().content()
-> PolicyLookupTool 作为 Spring AI Tool 被模型调用
-> 出错时返回 FALLBACK 事件，而不是让流直接断掉或变成 500
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

## 编译和测试

```bash
./mvnw -DskipTests compile
./mvnw test
```

## 启动

```bash
./mvnw spring-boot:run
```

## 流式请求

```bash
curl -N --get "http://localhost:8080/policy/ask" \
  --data-urlencode "question=出差回来后报销需要哪些材料"
```

`-N` 关闭 curl 缓冲，方便看到 SSE 逐段返回。

`--data-urlencode` 会把中文参数做 URL 编码，避免请求还没进 Controller 就被 Web 容器拒掉。

正常内容片段类似：

```text
data:{"success":true,"type":"CONTENT","content":"根据制度片段，","failureType":null,"retryable":false,"needHumanReview":false}
```

空问题会返回兜底事件：

```bash
curl -N "http://localhost:8080/policy/ask"
```

```text
data:{"success":false,"type":"FALLBACK","content":"问题不能为空，请补充要查询的制度问题。","failureType":"INVALID_REQUEST","retryable":false,"needHumanReview":false}
```

工具失败也返回同样结构：

```bash
curl -N --get "http://localhost:8080/policy/ask" \
  --data-urlencode "question=资料库故障时怎么处理"
```

```text
data:{"success":false,"type":"FALLBACK","content":"制度资料服务暂时不可用，建议稍后再试或转人工确认。","failureType":"POLICY_TOOL_FAILURE","retryable":true,"needHumanReview":true}
```

当前示例把 4xx 客户端错误按不可重试处理。如果供应商用 `429` 表示临时限流，并且业务上希望短重试，可以按 Spring AI 配置把 `429` 放到 `spring.ai.retry.on-http-codes`。

## 关键代码

```text
src/main/java/com/example/springaideepseekdemo/config/PolicyPromptProperties.java
src/main/java/com/example/springaideepseekdemo/controller/PolicyAskController.java
src/main/java/com/example/springaideepseekdemo/dto/PolicyFailureType.java
src/main/java/com/example/springaideepseekdemo/dto/PolicyStreamEvent.java
src/main/java/com/example/springaideepseekdemo/exception/PolicyToolFailureException.java
src/main/java/com/example/springaideepseekdemo/model/PolicyModelClient.java
src/main/java/com/example/springaideepseekdemo/model/DeepSeekPolicyModelClient.java
src/main/java/com/example/springaideepseekdemo/service/PolicyAnswerService.java
src/main/java/com/example/springaideepseekdemo/tool/PolicyLookupTool.java
src/test/java/com/example/springaideepseekdemo/service/PolicyAnswerServiceTests.java
src/main/resources/application.yaml
```

## 和文章的对应关系

```text
流式返回结构：PolicyStreamEvent + PolicyFailureType
模型调用：DeepSeekPolicyModelClient + ChatClient.tools(...).stream().content()
资料查询：PolicyLookupTool + @Tool
错误分类：PolicyAnswerService 里的 FailureRule + FailureDecision
HTTP 状态分类：401/403 等 4xx 归为不可重试，429/5xx 归为临时失败
重试配置：spring.ai.retry
工具异常配置：spring.ai.tools.throw-exception-on-error=true
```
