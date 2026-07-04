package com.example.springaideepseekdemo.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class AccessPolicyTool {

	private final ToolLoopTrace trace;

	public AccessPolicyTool(ToolLoopTrace trace) {
		this.trace = trace;
	}

	@Tool(description = "根据权限申请问题查询公司生产数据权限制度片段")
	public String searchAccessPolicy(@ToolParam(description = "用户提出的权限申请问题") String question) {
		String policy;
		if (question != null && (question.contains("生产库") || question.contains("权限") || question.contains("只读"))) {
			policy = """
					权限制度片段：
					1. P0 级生产数据默认不允许直接开通权限，包括只读权限。
					2. 申请人必须属于相关项目组，并说明具体排障、核对或临时查询目的。
					3. P0 级生产数据权限需要直属负责人、DBA 和安全负责人审批。
					4. 通过审批后只开通最小只读权限，默认有效期 7 天，到期自动回收。
					5. 对制度口径仍有疑问时，联系安全负责人确认。
					""";
		}
		else {
			policy = "没有查询到明确的权限制度片段，请转人工确认。";
		}
		trace.add("searchAccessPolicy(question=%s) -> %s".formatted(question, firstLine(policy)));
		return policy;
	}

	private String firstLine(String text) {
		return text.lines().filter(line -> !line.isBlank()).findFirst().orElse(text);
	}

}
