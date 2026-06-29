package com.example.springaideepseekdemo.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PolicyLookupTool {

	private static final Logger log = LoggerFactory.getLogger(PolicyLookupTool.class);

	@Tool(description = "根据用户问题查询企业内部制度片段")
	public String searchPolicyByQuestion(@ToolParam(description = "用户提出的制度问题") String question) {
		log.info("searchPolicyByQuestion tool called, question={}", question);

		if (question != null && question.contains("资料库故障")) {
			throw new IllegalStateException("POLICY_SOURCE_TIMEOUT");
		}

		if (question != null && question.contains("报销")) {
			return """
					制度片段：差旅报销需要在返程后 7 个自然日内提交。
					必填材料：行程单、发票、付款凭证。
					如果涉及客户拜访，还需要补充拜访记录或审批单号。
					""";
		}

		if (question != null && question.contains("年假")) {
			return """
					制度片段：员工入职满 1 年后可申请年假。
					年假申请需要提前 3 个工作日在系统提交，并由直属负责人审批。
					""";
		}

		return "没有检索到明确制度片段。";
	}

}
