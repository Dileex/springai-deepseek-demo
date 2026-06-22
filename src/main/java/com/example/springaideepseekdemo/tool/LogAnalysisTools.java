package com.example.springaideepseekdemo.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class LogAnalysisTools {

	private static final Logger log = LoggerFactory.getLogger(LogAnalysisTools.class);

	@Tool(description = "根据 traceId 查询同一条调用链路里的补充日志")
	public String findLogsByTraceId(
			@ToolParam(description = "日志里的 traceId，例如 TR-20260621-001") String traceId) {
		log.info("findLogsByTraceId tool called, traceId={}", traceId);

		if ("TR-20260621-001".equals(traceId)) {
			return """
					traceId=TR-20260621-001
					10:15:30.912 INFO  [user-service] query current user success, response body: {"user":null}
					10:15:31.103 INFO  [order-service] request create order, userContext=null
					10:15:31.240 ERROR [order-service] create order failed at UserContext.currentUserId
					""";
		}

		return "没有查到 traceId=" + traceId + " 的补充链路日志。";
	}

	@Tool(description = "查询服务最近一次发布记录，用于判断异常是否可能和发布变更有关")
	public String getRecentDeployments(
			@ToolParam(description = "服务名称，例如 order-service") String serviceName) {
		log.info("getRecentDeployments tool called, serviceName={}", serviceName);

		if ("order-service".equals(serviceName)) {
			return """
					service=order-service
					09:50 发布版本 2026.06.21-rc1
					变更点：UserContext.currentUserId 增加从网关上下文读取用户信息的逻辑。
					风险提示：上游返回空用户时，需要做空值保护。
					""";
		}

		return "没有查到 service=" + serviceName + " 的近期发布记录。";
	}

	@Tool(description = "根据异常关键字查询排障手册")
	public String searchRunbook(
			@ToolParam(description = "异常关键字，例如 NullPointerException 或 SQLTimeoutException") String keyword) {
		log.info("searchRunbook tool called, keyword={}", keyword);

		if (keyword != null && keyword.contains("NullPointerException")) {
			return """
					NullPointerException 排障手册：
					1. 先确认空对象来自入参、缓存、数据库，还是上游服务返回。
					2. 对关键上下文对象增加空值保护和清晰异常信息。
					3. 检查最近发布是否改动了对象构造、字段映射或网关上下文读取逻辑。
					4. 补充 null 输入、上游空响应、降级路径的单元测试或集成测试。
					""";
		}

		if (keyword != null && keyword.contains("SQLTimeoutException")) {
			return """
					SQLTimeoutException 排障手册：
					1. 先确认慢 SQL、索引、连接池和数据库负载。
					2. 查看同一时间段是否有批处理任务或流量突增。
					3. 必要时先限流或降级非核心查询。
					""";
		}

		return "没有命中明确的排障手册，请根据异常类型补充关键字。";
	}

}
