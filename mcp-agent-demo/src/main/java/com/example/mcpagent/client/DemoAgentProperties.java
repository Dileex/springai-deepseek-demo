package com.example.mcpagent.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.agent")
public record DemoAgentProperties(
		String expectedToolName,
		String systemPrompt,
		String userPromptTemplate,
		String defaultQuestion) {
}
