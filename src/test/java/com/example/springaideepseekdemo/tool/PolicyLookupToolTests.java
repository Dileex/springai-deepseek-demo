package com.example.springaideepseekdemo.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PolicyLookupToolTests {

	@Test
	void shouldReturnExpensePolicySnippet() {
		PolicyLookupTool tool = new PolicyLookupTool();

		String result = tool.searchPolicyByQuestion("出差回来后报销需要哪些材料");

		assertThat(result).contains("行程单", "发票", "付款凭证");
	}

	@Test
	void shouldReturnSafeEmptySnippetWhenPolicyNotFound() {
		PolicyLookupTool tool = new PolicyLookupTool();

		String result = tool.searchPolicyByQuestion("公司下午茶怎么申请");

		assertThat(result).isEqualTo("没有检索到明确制度片段。");
	}

	@Test
	void shouldThrowWhenPolicySourceFails() {
		PolicyLookupTool tool = new PolicyLookupTool();

		assertThatThrownBy(() -> tool.searchPolicyByQuestion("资料库故障时怎么处理"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("POLICY_SOURCE_TIMEOUT");
	}

}
