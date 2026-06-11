# 001 DeepSeek Quickstart

对应文章：

《10 分钟跑通第一个 Spring AI + DeepSeek 项目》

代码版本：

```text
branch: article/001-deepseek-quickstart
tag:    v001-deepseek-quickstart
```

## 运行前准备

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

## 启动项目

```bash
./mvnw spring-boot:run
```

## 测试接口

```bash
curl "http://localhost:8080/chat/ask?question=用一句话介绍 Spring AI"
```

流式接口：

```bash
curl -N "http://localhost:8080/chat/stream?question=用三句话介绍 Spring AI"
```

代码助手接口：

```bash
curl "http://localhost:8080/chat/code-helper?question=Spring Boot Controller 怎么写"
```

## 对应代码

```text
pom.xml
    -> Spring Boot、Spring AI BOM、DeepSeek starter、Web 依赖

src/main/resources/application.yaml
    -> DeepSeek API Key、模型和 temperature 配置

src/main/java/com/example/springaideepseekdemo/controller/ChatController.java
    -> 普通问答、流式问答、代码助手接口
```
