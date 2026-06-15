# 003 Spring AI Tool Calling

对应文章：

《Spring AI Tool Calling 入门：让 AI 能调用你的业务方法》

代码版本：

```text
branch: article/003-tool-calling
tag:    v003-tool-calling
```

## 运行前准备

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

当前示例沿用项目里的 DeepSeek 配置：

```yaml
spring:
  ai:
    model:
      chat: deepseek
    deepseek:
      chat:
        options:
          model: deepseek-v4-flash
          temperature: 0.2
```

## 启动项目

```bash
./mvnw spring-boot:run
```

## 测试接口

```bash
curl "http://localhost:8080/tool-calling/order-logistics"
```

也可以自定义问题：

```bash
curl --get "http://localhost:8080/tool-calling/order-logistics" \
  --data-urlencode "question=帮我查一下订单 12345 的物流信息"
```

如果 Tool Calling 生效，模型会请求调用 `queryOrderLogistics` 工具，再基于工具返回结果回答。

## 对应代码

```text
src/main/java/com/example/springaideepseekdemo/toolcalling/OrderTools.java
    -> 使用 @Tool 和 @ToolParam 暴露订单物流查询工具

src/main/java/com/example/springaideepseekdemo/toolcalling/ToolCallingController.java
    -> 使用 .tools(orderTools) 把工具注册到本次 ChatClient 调用

src/main/resources/application.yaml
    -> DeepSeek API Key、模型和 temperature 配置
```

## 说明

这个示例只使用模拟数据，不连接真实数据库。

真实项目里，可以把 `OrderTools#queryOrderLogistics` 里的模拟逻辑替换成自己的 `OrderService`。

第一次接 Tool Calling，建议先从查询类工具开始。会修改数据的工具必须在后端做权限校验、参数校验、二次确认和日志记录。
