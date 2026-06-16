# 006 Prompt

对应文章：

《Spring AI Prompt 入门：别再把所有内容都塞进 user 了》

代码版本：

```text
branch: article/006-prompt
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
curl -X POST "http://localhost:8080/code/explain?filePath=UserService.java&focus=空指针风险" \
  -H "Content-Type: text/plain" \
  --data-binary 'public String getName(User user) { return user.getName(); }'
```

## 对应代码

```text
pom.xml
    -> Spring Boot、Spring AI BOM、DeepSeek starter、Web 依赖

src/main/resources/application.yaml
    -> DEEPSEEK_API_KEY、deepseek-v4-flash、temperature 配置

src/main/java/com/example/springaideepseekdemo/controller/CodeExplainController.java
    -> system、user template、param 的完整示例
```
