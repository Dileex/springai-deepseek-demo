# Article 015: Spring AI 2.0.0 Error Fallback

对应文章：

```text
Spring AI 2.0.0 错误兜底实战：别让 AI 接口失败时只剩 500
```

对应分支：

```text
article/015-error-fallback
```

这个示例实现的是一个内部制度问答接口：

```text
用户提交制度问题
-> PolicyAskController 接收请求
-> PolicyAnswerService 统一处理成功、空回答、模型异常、工具异常
-> DeepSeekPolicyModelClient 调用 ChatClient
-> PolicyLookupTool 提供制度片段
-> 接口返回稳定的 PolicyAnswer，而不是直接把异常变成 500
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

## 请求接口

```bash
curl "http://localhost:8080/policy/ask?question=出差回来后报销需要哪些材料"
```

成功时返回结构类似：

```json
{
  "success": true,
  "answer": "根据制度片段，出差报销需要在返程后 7 个自然日内提交，材料包括行程单、发票和付款凭证。",
  "failureType": null,
  "retryable": false,
  "needHumanReview": false
}
```

如果模型临时不可用，返回结构类似：

```json
{
  "success": false,
  "answer": "AI 服务暂时不可用，可以稍后重试。",
  "failureType": "MODEL_TEMPORARY_FAILURE",
  "retryable": true,
  "needHumanReview": false
}
```

如果制度工具失败，返回结构类似：

```json
{
  "success": false,
  "answer": "制度资料服务暂时不可用，建议稍后再试或转人工确认。",
  "failureType": "POLICY_TOOL_FAILURE",
  "retryable": true,
  "needHumanReview": true
}
```

## 关键代码

```text
src/main/java/com/example/springaideepseekdemo/config/PolicyPromptProperties.java
src/main/java/com/example/springaideepseekdemo/controller/PolicyAskController.java
src/main/java/com/example/springaideepseekdemo/dto/PolicyAnswer.java
src/main/java/com/example/springaideepseekdemo/dto/PolicyFailureType.java
src/main/java/com/example/springaideepseekdemo/exception/PolicyToolFailureException.java
src/main/java/com/example/springaideepseekdemo/model/PolicyModelClient.java
src/main/java/com/example/springaideepseekdemo/model/DeepSeekPolicyModelClient.java
src/main/java/com/example/springaideepseekdemo/service/PolicyAnswerService.java
src/main/java/com/example/springaideepseekdemo/tool/PolicyLookupTool.java
src/main/resources/application.yaml
```

## 和文章的对应关系

文章里的核心点，对应代码如下：

```text
稳定返回结构：PolicyAnswer + PolicyFailureType
模型调用：DeepSeekPolicyModelClient
工具调用：PolicyLookupTool + ChatClient.tools(...)
错误分类：PolicyAnswerService
重试配置：spring.ai.retry
工具异常策略：spring.ai.tools.throw-exception-on-error
```
