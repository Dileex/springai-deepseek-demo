package com.example.springaideepseekdemo.service;

import com.example.springaideepseekdemo.config.AccessAssistantProperties;
import com.example.springaideepseekdemo.dto.AccessToolLoopResponse;
import com.example.springaideepseekdemo.advisor.AccessControlToolCallingAdvisor;
import com.example.springaideepseekdemo.tool.AccessPolicyTool;
import com.example.springaideepseekdemo.tool.EmployeeProfileTool;
import com.example.springaideepseekdemo.tool.ResourceCatalogTool;
import com.example.springaideepseekdemo.tool.ToolLoopTrace;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AccessToolLoopService {

	private final ChatClient chatClient;

	private final AccessAssistantProperties properties;

	private final EmployeeProfileTool employeeProfileTool;

	private final ResourceCatalogTool resourceCatalogTool;

	private final AccessPolicyTool accessPolicyTool;

	private final AccessControlToolCallingAdvisor accessControlToolCallingAdvisor;

	private final ToolLoopTrace trace;

	public AccessToolLoopService(ChatClient.Builder builder, AccessAssistantProperties properties,
			EmployeeProfileTool employeeProfileTool, ResourceCatalogTool resourceCatalogTool,
			AccessPolicyTool accessPolicyTool, AccessControlToolCallingAdvisor accessControlToolCallingAdvisor,
			ToolLoopTrace trace) {
		this.chatClient = builder.defaultSystem(properties.systemPrompt()).build();
		this.properties = properties;
		this.employeeProfileTool = employeeProfileTool;
		this.resourceCatalogTool = resourceCatalogTool;
		this.accessPolicyTool = accessPolicyTool;
		this.accessControlToolCallingAdvisor = accessControlToolCallingAdvisor;
		this.trace = trace;
	}

	public AccessToolLoopResponse ask(String employeeId, String question) {
		String userPrompt = properties.userTemplate()
			.replace("{employeeId}", employeeId)
			.replace("{question}", question);

		trace.reset();
		try {
			String answer = chatClient.prompt()
				.user(userPrompt)
				.advisors(accessControlToolCallingAdvisor)
				.tools(employeeProfileTool, resourceCatalogTool, accessPolicyTool)
				.call()
				.content();
			return new AccessToolLoopResponse(employeeId, question, answer, trace.snapshot());
		}
		finally {
			trace.clear();
		}
	}

}
