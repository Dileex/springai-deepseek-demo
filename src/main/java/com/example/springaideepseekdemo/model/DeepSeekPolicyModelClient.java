package com.example.springaideepseekdemo.model;

import com.example.springaideepseekdemo.config.PolicyPromptProperties;
import com.example.springaideepseekdemo.exception.PolicyToolFailureException;
import com.example.springaideepseekdemo.tool.PolicyLookupTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.stereotype.Component;

@Component
public class DeepSeekPolicyModelClient implements PolicyModelClient {

	private final ChatClient chatClient;

	private final PolicyPromptProperties properties;

	private final PolicyLookupTool policyLookupTool;

	public DeepSeekPolicyModelClient(ChatClient.Builder builder, PolicyPromptProperties properties,
			PolicyLookupTool policyLookupTool) {
		this.chatClient = builder.defaultSystem(properties.systemPrompt()).build();
		this.properties = properties;
		this.policyLookupTool = policyLookupTool;
	}

	@Override
	public String answer(String question) {
		String userPrompt = properties.userTemplate().replace("{question}", question);

		try {
			return chatClient.prompt().user(userPrompt).tools(policyLookupTool).call().content();
		}
		catch (ToolExecutionException ex) {
			throw new PolicyToolFailureException("Policy lookup tool failed", ex);
		}
	}

}
