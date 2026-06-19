# 007 Spring AI 2.0.0 Code Review Skill Demo

对应文章：

`Spring AI 2.0.0 接 Skill：不是多写一段 Prompt，而是让 Agent 按流程干活`

## 运行环境

- JDK 17
- Spring Boot 4.1.0
- Spring AI 2.0.0
- DeepSeek Chat Model：`deepseek-v4-flash`
- Community package：`org.springaicommunity:spring-ai-agent-utils:0.10.0`
- Skill：`code-review-skill`

这个分支演示 Spring AI 2.0.0 怎么通过 `SkillsTool` 加载本地 `SKILL.md`，再把代码审查工具接入 `ChatClient.tools(...)`。

需要注意：`SkillsTool` 负责把 `SKILL.md` 交给模型，不会自动替你审查代码。真正执行基础检查的，是应用侧提供的 `CodeReviewTools`。

Prompt 放在 `application.yaml`：

- `app.code-review.prompt.system`：长期规则
- `app.code-review.prompt.user-template`：本次输入模板

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

## Spring AI 调用 Skill

这个接口会经过模型，让模型先加载 `code-review-skill`，再决定是否调用 Java Tool 输出审查报告：

```bash
curl -X POST "http://localhost:8080/skills/code-review/run" \
  -H "Content-Type: text/plain" \
  --data-binary 'public String getName(User user) { return user.getName(); }'
```

## 流式调用

如果想观察模型边生成边返回，可以调用流式接口：

```bash
curl -N -X POST "http://localhost:8080/skills/code-review/run/stream" \
  -H "Content-Type: text/plain" \
  --data-binary 'public String getName(User user) { return user.getName(); }'
```

## 本地兜底验证

如果只是想先验证 Java Tool 能不能产出基础报告，可以不经过模型，直接调用：

```bash
curl -X POST "http://localhost:8080/skills/code-review/review" \
  -H "Content-Type: text/plain" \
  --data-binary 'public String getName(User user) { return user.getName(); }'
```

这个接口只用于本地兜底验证，不代表完整的 `Skill + Tool Calling` 链路。主路径还是 `/skills/code-review/run`。
