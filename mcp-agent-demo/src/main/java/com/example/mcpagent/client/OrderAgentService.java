package com.example.mcpagent.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OrderAgentService {

	private final ChatClient chatClient;

	private final ToolCallbackProvider toolCallbackProvider;

	private final DemoAgentProperties demoAgentProperties;

	public OrderAgentService(ChatClient.Builder builder, ToolCallbackProvider toolCallbackProvider,
			DemoAgentProperties demoAgentProperties) {
		this.chatClient = builder.defaultSystem(demoAgentProperties.systemPrompt()).build();
		this.toolCallbackProvider = toolCallbackProvider;
		this.demoAgentProperties = demoAgentProperties;
	}

	public String ask(String question) {
		String userQuestion = StringUtils.hasText(question) ? question : demoAgentProperties.defaultQuestion();
		String prompt = demoAgentProperties.userPromptTemplate().formatted(userQuestion);
		return this.chatClient.prompt()
			.user(prompt)
			.tools(this.toolCallbackProvider)
			.call()
			.content();
	}

}
