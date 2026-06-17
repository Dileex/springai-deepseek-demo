# 006 Prompt

对应文章：

《Spring AI 2.0.0 Prompt 入门：别再把所有内容都塞进 user 了》

代码版本：

```text
branch: article/006-prompt
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
curl -X POST "http://localhost:8080/code/explain?filePath=UserService.java" \
  -H "Content-Type: text/plain" \
  --data-binary 'public String getName(User user) { return user.getName(); }'
```

流式输出：

```bash
curl -N -X POST "http://localhost:8080/code/explain/stream?filePath=UserService.java" \
  -H "Content-Type: text/plain" \
  --data-binary 'public String getName(User user) { return user.getName(); }'
```

如果要在 URL 里传中文 query 参数，需要先做 URL 编码。

## 对应代码

```text
pom.xml
    -> Spring Boot、Spring AI BOM、DeepSeek starter、Web 依赖

src/main/resources/application.yaml
    -> spring.ai.deepseek.chat.model、temperature 配置

src/main/java/com/example/springaideepseekdemo/controller/CodeExplainController.java
    -> system、user template、param、普通输出和流式输出的完整示例
```

## 本地验证

```bash
./mvnw -DskipTests compile
DEEPSEEK_API_KEY=dummy ./mvnw test
```
