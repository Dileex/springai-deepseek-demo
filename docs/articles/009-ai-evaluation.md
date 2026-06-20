# 009 Spring AI 2.0.0 AI Evaluation Demo

对应文章：

`Spring AI 2.0.0 评估最小 Demo：别再靠肉眼看答案了`

## 运行环境

- JDK 17
- Spring Boot 4.1.0
- Spring AI 2.0.0
- DeepSeek Chat Model：`deepseek-v4-flash`

## 启动

本地规则评估接口不需要真实模型 Key，也可以先启动：

```bash
./mvnw spring-boot:run
```

如果要调用模型评估接口，先配置：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
./mvnw spring-boot:run
```

IDEA 运行时，把 `DEEPSEEK_API_KEY` 配到：

```text
Run/Debug Configurations
→ SpringaiDeepseekDemoApplication
→ Environment variables
```

## 查看固定评估用例

```bash
curl "http://localhost:8080/evaluation/cases"
```

## 本地规则评估

默认评估一个带“资料外退款时效”的坏答案：

```bash
curl -X POST "http://localhost:8080/evaluation/rule" \
  -H "Content-Type: application/json" \
  -d '{}'
```

预期结果：

```json
{
  "pass": false,
  "score": 0.0,
  "feedback": "回答里出现了资料未提供的退款时效，属于资料外事实。",
  "metadata": {
    "evaluator": "RefundPolicyRuleEvaluator",
    "reason": "UNSUPPORTED_REFUND_TIME"
  }
}
```

换成一个好答案：

```bash
curl -X POST "http://localhost:8080/evaluation/rule" \
  -H "Content-Type: application/json" \
  -d '{"answer":"不能取消。订单进入仓库拣货流程后会返回 ORDER_LOCKED，表示订单已锁定。"}'
```

## 模型事实评估

这个接口会调用 `FactCheckingEvaluator`，需要真实 `DEEPSEEK_API_KEY`：

```bash
curl -X POST "http://localhost:8080/evaluation/fact-check" \
  -H "Content-Type: application/json" \
  -d '{}'
```

注意：`FactCheckingEvaluator` 本质上也是一次模型调用，适合放在集成测试、回归评估或离线评测流程里，不适合替代所有规则校验。
