# 007 Structured Output

对应文章：

《Spring AI 结构化输出：别再让模型随便吐一段文本了》

代码版本：

```text
branch: article/007-structured-output
Spring Boot: 4.1.0
Spring AI: 2.0.0
Model: deepseek-v4-flash
```

## 运行前准备

如果在终端启动：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

如果在 IDEA 启动：

```text
Run/Debug Configurations
→ Environment variables
→ DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

## 启动项目

```bash
./mvnw spring-boot:run
```

## 测试接口

```bash
curl -X POST "http://localhost:8080/code/review" \
  -H "Content-Type: text/plain" \
  --data-binary 'public String getName(User user) { return user.getName(); }'
```

返回结果是 JSON 结构，字段来自 `CodeReviewResult`：

```json
{
  "riskLevel": "HIGH",
  "summary": "user 可能为空，直接调用 getName() 会触发空指针异常",
  "suggestions": [
    "在调用 user.getName() 前增加空值判断"
  ],
  "needHumanReview": true
}
```

## 对应代码

```text
pom.xml
    -> Spring Boot、Spring AI BOM、DeepSeek starter、Web 依赖

src/main/resources/application.yaml
    -> spring.ai.deepseek.chat.model、temperature 配置

src/main/java/com/example/springaideepseekdemo/dto/CodeReviewResult.java
    -> 结构化输出对象

src/main/java/com/example/springaideepseekdemo/controller/CodeReviewController.java
    -> .entity(CodeReviewResult.class) 结构化输出示例
```

## 本地验证

```bash
./mvnw -DskipTests compile
DEEPSEEK_API_KEY=dummy ./mvnw test
```
