package com.example.springaideepseekdemo.tool;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class TravelExpenseTools {

	@McpTool(name = "check_lodging_policy", description = "检查员工住宿费用是否需要主管审批",
			annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true,
					openWorldHint = false))
	public LodgingPolicyResult checkLodgingPolicy(
			@McpToolParam(description = "出差城市，例如：上海、北京、杭州", required = true) String city,
			@McpToolParam(description = "住宿金额，单位：元", required = true) double amount,
			@McpToolParam(description = "是否已经提供酒店发票", required = true) boolean hasHotelInvoice,
			@McpToolParam(description = "是否已经提供行程单", required = true) boolean hasItinerary) {

		double standard = cityStandard(city);
		boolean overLimit = amount > standard;
		boolean missingMaterials = !hasHotelInvoice || !hasItinerary;

		String decision;
		if (missingMaterials) {
			decision = "MATERIAL_MISSING";
		}
		else if (overLimit) {
			decision = "MANAGER_APPROVAL_REQUIRED";
		}
		else {
			decision = "CAN_SUBMIT";
		}

		return new LodgingPolicyResult(
				city,
				amount,
				standard,
				overLimit,
				missingMaterials,
				decision,
				"住宿费需要酒店发票和行程单；超出城市标准时，需要直属主管审批。");
	}

	private double cityStandard(String city) {
		return switch (city) {
			case "北京", "上海", "深圳" -> 600;
			case "杭州", "广州", "南京" -> 500;
			default -> 400;
		};
	}

}
