# Article 016: Spring AI 2.0.0 Tool Loop

对应文章：

```text
Spring AI 2.0.0 Tool Loop 实战：别让模型直接批准生产库权限
```

对应分支：

```text
article/016-tool-loop
```

这个示例演示的是一次 `ChatClient` 调用内部的工具调用循环：

```text
用户提交权限申请问题
-> AccessToolLoopController 接收请求
-> AccessToolLoopService 调用 ChatClient
-> ChatClient.tools(...) 暴露三个工具
-> ToolCallingAdvisor 参与处理模型返回的 tool call
-> ToolCallingManager 在应用侧执行工具
-> 工具结果回到模型上下文
-> 模型不再请求工具后输出最终答案
```

这个例子只演示一次 `ChatClient` 调用里的 tool loop：模型提出工具调用请求，Spring AI 在应用侧执行工具，再把结果回填给模型。

## 运行环境

```text
Java 17
Spring Boot 4.1.0
Spring AI 2.0.0
DeepSeek deepseek-v4-flash
```

启动前配置 DeepSeek Key：

```bash
export DEEPSEEK_API_KEY=你的 DeepSeek API Key
```

## 编译和测试

```bash
./mvnw -DskipTests compile
./mvnw test
```

## 启动

```bash
./mvnw spring-boot:run
```

## 请求接口

```bash
curl --get "http://localhost:8080/tool-loop/ask" \
  --data-urlencode "employeeId=E1002" \
  --data-urlencode "question=申请订单生产库只读权限，能直接开通吗？"
```

返回结构类似：

```json
{
  "employeeId": "E1002",
  "question": "申请订单生产库只读权限，能直接开通吗？",
  "answer": "不能直接开通。E1002 属于订单系统项目组，但订单生产库是 P0 级生产数据，包含用户敏感信息。按照制度，P0 级生产数据只读权限也需要直属负责人、DBA 和安全负责人审批，通过后只开通最小只读权限，默认有效期 7 天。",
  "toolEvents": [
    "getEmployeeProfile(employeeId=E1002) -> 员工 E1002：后端开发工程师，P6，订单系统项目组，不具备生产库审批人角色。",
    "getResourceRisk(resourceName=订单生产库) -> 资源信息：",
    "searchAccessPolicy(question=生产库只读权限申请规则) -> 权限制度片段："
  ]
}
```

`answer` 会受模型表达影响，不要求逐字一致。

`toolEvents` 来自本地示例里的 `ToolLoopTrace`，用于观察工具调用顺序。生产里更适合接日志、Micrometer Observation、TraceId 和 APM。

## 关键代码

```text
src/main/java/com/example/springaideepseekdemo/config/AccessAssistantProperties.java
src/main/java/com/example/springaideepseekdemo/controller/AccessToolLoopController.java
src/main/java/com/example/springaideepseekdemo/dto/AccessToolLoopResponse.java
src/main/java/com/example/springaideepseekdemo/service/AccessToolLoopService.java
src/main/java/com/example/springaideepseekdemo/tool/EmployeeProfileTool.java
src/main/java/com/example/springaideepseekdemo/tool/ResourceCatalogTool.java
src/main/java/com/example/springaideepseekdemo/tool/AccessPolicyTool.java
src/main/java/com/example/springaideepseekdemo/tool/ToolLoopTrace.java
src/main/resources/application.yaml
```

## 和文章的对应关系

```text
一次 ChatClient 调用：AccessToolLoopService
三个工具：EmployeeProfileTool + ResourceCatalogTool + AccessPolicyTool
工具暴露：ChatClient.tools(employeeProfileTool, resourceCatalogTool, accessPolicyTool)
工具调用轨迹：ToolLoopTrace
请求入口：GET /tool-loop/ask
```
