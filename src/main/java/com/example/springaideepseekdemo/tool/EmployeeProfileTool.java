package com.example.springaideepseekdemo.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class EmployeeProfileTool {

	private final ToolLoopTrace trace;

	public EmployeeProfileTool(ToolLoopTrace trace) {
		this.trace = trace;
	}

	@Tool(description = "根据员工编号查询员工岗位、部门、项目组和权限身份")
	public String getEmployeeProfile(@ToolParam(description = "员工编号，例如 E1001") String employeeId) {
		String profile = switch (employeeId) {
			case "E1001" -> "员工 E1001：后端负责人，P7，订单系统项目组，具备生产库审批人角色。";
			case "E1002" -> "员工 E1002：后端开发工程师，P6，订单系统项目组，不具备生产库审批人角色。";
			default -> "未查询到员工信息，请转人工确认员工编号。";
		};
		trace.add("getEmployeeProfile(employeeId=%s) -> %s".formatted(employeeId, profile));
		return profile;
	}

}
