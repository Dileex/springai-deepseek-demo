package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.tool.LogAnalysisTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log-agent")
public class LogAgentController {

	private final ChatClient chatClient;

	private final LogAnalysisTools logAnalysisTools;

	@Value("${app.log-agent.prompt.system}")
	private String systemPrompt;

	@Value("${app.log-agent.prompt.user-template}")
	private String userTemplate;

	public LogAgentController(ChatClient.Builder builder, LogAnalysisTools logAnalysisTools) {
		this.chatClient = builder.build();
		this.logAnalysisTools = logAnalysisTools;
	}

	@PostMapping(value = "/analyze", consumes = "text/plain")
	public String analyze(@RequestBody String logText) {
		return chatClient.prompt()
			.system(systemPrompt)
			.user(userSpec -> userSpec.text(userTemplate).param("logText", logText))
			.tools(logAnalysisTools)
			.call()
			.content();
	}

}
