# 011 Spring AI 2.0.0 Observability 最小实战

对应文章：

`Spring AI 2.0.0 Observability 最小实战：AI 接口慢在哪、贵在哪，一眼看清`

## 运行环境

- JDK 17
- Spring Boot 4.1.0
- Spring AI 2.0.0
- DeepSeek API Key

这个实战使用 Spring AI 2.0.0 接入 DeepSeek，验证真实模型调用下的 Spring AI Observability、Actuator 和 Prometheus 指标。

Micrometer 可以理解为 Spring Boot 的指标采集层，Actuator / Prometheus 负责把这些指标暴露出来。

DeepSeek 配置使用 Spring AI 2.0.0 的官方属性：`spring.ai.model.chat` 和 `spring.ai.deepseek.*`。

启动前先配置：

```bash
export DEEPSEEK_API_KEY=<your-deepseek-api-key>
```

这个实战固定使用 `deepseek-v4-flash`，后面的配置和指标都按这个模型来看。

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
  "answer": "结论：不能直接报销。住宿费 720 元是否超标，需要先确认公司差旅标准；目前还缺少差旅标准或审批信息。",
  "elapsedMillis": 1820
}
```

中文参数建议使用 `curl -G --data-urlencode`，避免直接把中文拼进 URL query 导致 400。

## 查看指标

浏览器打开：

```text
http://localhost:8080/observability.html
```

如果页面上的指标还是 0，先确认已经调用过 `/travel/ask`，并且服务已经用最新代码重新启动。

这个页面会把核心指标整理成四块：

```text
ChatClient 总耗时
Advisor 链路
ChatModel 耗时
token 使用量
```

注意：这些耗时不是三段相加。ChatClient 是外层总耗时，Advisor 是中间处理链路，ChatModel 是里面真实请求模型的耗时。如果三者很接近，通常说明这次调用的大头在模型请求本身。

原始 Prometheus 指标仍然可以这样看：

```bash
curl -s "http://localhost:8080/actuator/prometheus" | grep "spring_ai\|gen_ai"
```

重点看四类指标：

```text
spring_ai_chat_client_seconds
spring_ai_advisor_seconds
gen_ai_client_operation_seconds
gen_ai_client_token_usage_total
```

本地验证时可以看到类似：

```text
spring_ai_chat_client_seconds_count{...,spring_ai_chat_client_stream="false",...} 1
spring_ai_advisor_seconds_count{...,spring_ai_advisor_name="Tool Calling Advisor",...} 1
gen_ai_client_operation_seconds_count{...,gen_ai_response_model="deepseek-v4-flash",gen_ai_system="deepseek"} 1
gen_ai_client_token_usage_total{...,gen_ai_token_type="input"} 86.0
gen_ai_client_token_usage_total{...,gen_ai_token_type="output"} 49.0
gen_ai_client_token_usage_total{...,gen_ai_token_type="total"} 135.0
```

注意：看到 `Tool Calling Advisor` 指标，不等于外部工具已经执行。这个实战没有配置任何 Tool，它只说明请求经过了 ChatClient 的默认 Advisor 链路。

## 代码说明

- `AiClientConfig`：创建带 `ObservationRegistry` 的 `ChatClient`，让 ChatClient 层产生观测指标。
- `application.yaml`：配置 DeepSeek API Key、模型名、Actuator 和 Prometheus。
- `TravelAssistantController`：暴露 `/travel/ask`，触发一次真实 DeepSeek 调用。
- `ObservabilityController`：把 Micrometer 指标整理成 `/observability/summary`。
- `observability.html`：展示更容易阅读的本地观测页面。

如果本机没有 `DEEPSEEK_API_KEY`，项目可以编译，但不能完成真实模型调用。
