# 007 Spring AI 2.0.0 Skill Demo

对应文章：

`Spring AI 2.0.0 实战：接入一个 SkillHub 风格的 PPT 生成 Skill`

## 运行环境

- JDK 17
- Spring Boot 4.1.0
- Spring AI 2.0.0
- DeepSeek Chat Model：`deepseek-v4-flash`
- Community package：`org.springaicommunity:spring-ai-agent-utils:0.4.2`

## 配置 DeepSeek Key

命令行运行：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

IDEA 运行：

```text
Run/Debug Configurations
→ SpringaiDeepseekDemoApplication
→ Environment variables
→ DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

## 启动

```bash
./mvnw spring-boot:run
```

## 普通调用

```bash
curl -X POST "http://localhost:8080/skills/ppt/run" \
  -H "Content-Type: text/plain" \
  --data-binary "帮我做一份《Java 团队如何落地 AI Agent》的 6 页分享 PPT"
```

## 流式调用

```bash
curl -N -X POST "http://localhost:8080/skills/ppt/stream" \
  -H "Content-Type: text/plain" \
  --data-binary "帮我做一份《Java 团队如何落地 AI Agent》的 6 页分享 PPT"
```
