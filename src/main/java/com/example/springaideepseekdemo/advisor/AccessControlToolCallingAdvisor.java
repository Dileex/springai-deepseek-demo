package com.example.springaideepseekdemo.advisor;

import com.example.springaideepseekdemo.tool.ToolLoopTrace;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.stereotype.Component;

@Component
public class AccessControlToolCallingAdvisor extends ToolCallingAdvisor {

	private final ToolLoopTrace trace;

	public AccessControlToolCallingAdvisor(ToolLoopTrace trace) {
		super(ToolCallingManager.builder().build(), DEFAULT_TOOL_EXECUTION_ELIGIBILITY_CHECKER, DEFAULT_ORDER, true);
		this.trace = trace;
	}

	@Override
	public String getName() {
		return "Access Control Tool Calling Advisor";
	}

	@Override
	protected ChatClientRequest doInitializeLoop(ChatClientRequest request, CallAdvisorChain chain) {
		trace.add("advisor.initialize -> 开始权限申请 Tool Loop");
		return request;
	}

	@Override
	protected ChatClientRequest doBeforeCall(ChatClientRequest request, CallAdvisorChain chain) {
		trace.add("advisor.beforeCall -> 准备调用模型");
		return request;
	}

	@Override
	protected ChatClientResponse doAfterCall(ChatClientResponse response, CallAdvisorChain chain) {
		trace.add("advisor.afterCall -> 收到模型响应");
		return response;
	}

	@Override
	protected ChatClientResponse doFinalizeLoop(ChatClientResponse response, CallAdvisorChain chain) {
		trace.add("advisor.finalize -> 权限申请 Tool Loop 结束");
		return response;
	}

}
