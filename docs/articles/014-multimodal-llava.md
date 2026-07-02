# 014 Multimodal LLaVA Demo

这个示例对应文章：

```text
Spring AI 2.0.0 接多模态：本地 Ollama 先做图片描述
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

这个分支只实现 `/multimodal/describe` 一个接口。

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
cd /path/to/springai-deepseek-demo
./mvnw -DskipTests compile
./mvnw spring-boot:run
```

## 调用接口

准备一张本地图片，然后调用：

```bash
curl -X POST "http://localhost:8080/multimodal/describe" \
  -F "file=@docs/assets/014-llava-basic-shapes.png"
```

返回示例：

```json
{
  "description": "图像中有三个简单的图形区域，左边偏红色，中间偏蓝色，右边偏绿色，整体像是在展示一个从左到右的流程。部分文字看不清楚。"
}
```

返回内容取决于上传图片。

验证时重点看：

```text
Ollama 里是否有 llava:7b
接口是否返回 description
description 是否确实和上传图片相关
```

`llava:7b` 本地跑起来方便，但识别效果不要期待太高。

测试图里是红色圆形、蓝色方形、绿色三角形和箭头。它通常能看出颜色和大概顺序，但形状描述不一定准。

如果发现它只返回“文字看不清”，先确认服务已经用最新配置重启。新版提示词会要求模型先描述图形、颜色和布局，再说明文字是否清晰。

如果仍然返回英文或描述不准，这就是 `llava:7b` 对指令遵循和视觉识别不稳定的表现。

所以这个示例主要验证图片输入链路，不适合当作中文 OCR 或复杂截图理解方案。
