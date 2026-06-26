# 014 Multimodal LLaVA Demo

这个示例对应文章：

```text
Spring AI 2.0.0 接多模态：本地 LLaVA 先做图片描述
```

## 示例目标

用 Spring AI 2.0.0 + Ollama + `llava:7b` 跑通图片输入链路：

```text
图片
  -> Spring AI media()
  -> Ollama
  -> llava:7b
  -> 图片描述文本
```

## 运行环境

- JDK 17+
- Maven
- Ollama
- 本地已拉取 `llava:7b`

拉取模型：

```bash
ollama pull llava:7b
```

确认 Ollama 服务正常：

```bash
curl http://localhost:11434/api/tags
```

## 启动项目

```bash
cd /Users/dilee/Projects/springai-deepseek-demo
./mvnw -DskipTests compile
./mvnw spring-boot:run
```

## 调用接口

准备一张本地图片，然后调用：

```bash
curl -X POST "http://localhost:8080/multimodal/describe" \
  -F "file=@/Users/dilee/Desktop/test.png"
```

返回示例：

```json
{
  "description": "图片是一张技术文章封面，上方有深色标题栏，中间是大号中文标题，下方用卡片展示 Image、LLaVA、Text 的流程。部分小字可能看不清。"
}
```

返回内容取决于上传图片。
