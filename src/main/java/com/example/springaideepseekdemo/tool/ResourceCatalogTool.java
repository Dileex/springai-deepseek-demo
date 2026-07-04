package com.example.springaideepseekdemo.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ResourceCatalogTool {

	private final ToolLoopTrace trace;

	public ResourceCatalogTool(ToolLoopTrace trace) {
		this.trace = trace;
	}

	@Tool(description = "根据资源名称查询系统资源等级、数据类型和风险说明")
	public String getResourceRisk(@ToolParam(description = "资源名称，例如订单生产库") String resourceName) {
		String risk = resourceName != null && (resourceName.contains("订单") || resourceName.contains("order"))
				? """
						资源信息：
						资源编码：order-prod-db
						资源名称：订单生产库
						资源等级：P0
						数据类型：订单主表、支付状态、收货手机号、收货地址
						风险说明：生产数据库，包含用户敏感信息，只读权限也需要走审批。
						"""
				: "没有查询到明确的资源风险信息，请转人工确认资源名称。";
		trace.add("getResourceRisk(resourceName=%s) -> %s".formatted(resourceName, firstLine(risk)));
		return risk;
	}

	private String firstLine(String text) {
		return text.lines().filter(line -> !line.isBlank()).findFirst().orElse(text);
	}

}
