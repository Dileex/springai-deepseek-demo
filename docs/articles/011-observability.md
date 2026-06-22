# 011 Spring AI 2.0.0 Observability Demo

对应文章：

`Spring AI 2.0.0 Observability 入门：一次 AI 调用慢在哪、贵在哪？`

## 运行环境

- JDK 17
- Spring Boot 4.1.0
- Spring AI 2.0.0

这个 Demo 使用本地 `ObservedFakeChatModel`，不需要外部模型 API Key。这样可以先验证 Spring AI Observability、Actuator 和 Prometheus 指标是否正常。

## 启动

```bash
./mvnw spring-boot:run
```

如果本地 8080 被占用，可以换端口：

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=18081
```

下面命令以 8080 为例。

## 调用业务接口

```bash
curl -sG "http://localhost:8080/travel/ask" \
  --data-urlencode "question=上海住宿费720元，有发票和行程单，可以直接报销吗？"
```

预期返回类似：

```json
{
  "question": "上海住宿费720元，有发票和行程单，可以直接报销吗？",
  "answer": "这次上海住宿费 720 元，高于示例标准 600 元。\n发票和行程单齐全，所以材料不是主要问题。\n真正需要关注的是超标审批：建议补直属主管审批后再提交。\n",
  "elapsedMillis": 14
}
```

中文参数建议使用 `curl -G --data-urlencode`，避免直接把中文拼进 URL query 导致 400。

## 查看指标

```bash
curl -s "http://localhost:8080/actuator/prometheus" | grep "spring_ai\|gen_ai"
```

重点看三类指标：

```text
spring_ai_chat_client_seconds
gen_ai_client_operation_seconds
gen_ai_client_token_usage_total
```

本地验证时可以看到类似：

```text
spring_ai_chat_client_seconds_count{...,spring_ai_chat_client_stream="false",...} 1
gen_ai_client_operation_seconds_count{...,gen_ai_response_model="observability-demo-model",gen_ai_system="demo"} 1
gen_ai_client_token_usage_total{...,gen_ai_token_type="input"} 1213.0
gen_ai_client_token_usage_total{...,gen_ai_token_type="output"} 38.0
gen_ai_client_token_usage_total{...,gen_ai_token_type="total"} 1251.0
```

## 代码说明

- `AiClientConfig`：创建带 `ObservationRegistry` 的 `ChatClient`，让 ChatClient 层产生观测指标。
- `ObservedFakeChatModel`：本地模拟 ChatModel，同时用 Spring AI 的 `ChatModelObservationDocumentation.CHAT_MODEL_OPERATION` 记录模型调用指标。
- `TravelAssistantController`：暴露 `/travel/ask`，用于触发一次 ChatClient 调用。

换成真实模型时，Actuator / Prometheus 的观测出口不变。真实模型需要额外配置对应 provider 的依赖和 API Key。
