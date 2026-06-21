package com.example.mcpagent.client;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

public class TravelExpenseAgentModel implements ChatModel {

	private static final String TOOL_CALL_ID = "call-check-lodging-policy";

	@Override
	public ChatResponse call(Prompt prompt) {
		ToolResponseMessage toolResponseMessage = findLastToolResponse(prompt);
		if (toolResponseMessage == null) {
			return requestLodgingPolicyToolCall();
		}
		return finalAnswer(toolResponseMessage);
	}

	@Override
	public ChatOptions getOptions() {
		return ToolCallingChatOptions.builder().build();
	}

	private ChatResponse requestLodgingPolicyToolCall() {
		AssistantMessage.ToolCall toolCall = new AssistantMessage.ToolCall(
				TOOL_CALL_ID,
				"function",
				"check_lodging_policy",
				"""
						{
						  "city": "上海",
						  "amount": 720,
						  "hasHotelInvoice": true,
						  "hasItinerary": true
						}
						""");

		AssistantMessage assistantMessage = AssistantMessage.builder()
			.content("")
			.toolCalls(List.of(toolCall))
			.build();

		return new ChatResponse(List.of(new Generation(assistantMessage)));
	}

	private ChatResponse finalAnswer(ToolResponseMessage toolResponseMessage) {
		String toolResult = toolResponseMessage.getResponses().stream()
			.filter(response -> response.name().equals("check_lodging_policy"))
			.findFirst()
			.map(ToolResponseMessage.ToolResponse::responseData)
			.orElse("");

		String answer = """
				我已经通过 MCP 工具 check_lodging_policy 查询了差旅住宿规则。

				工具返回结果：
				%s

				结论：上海住宿费 720 元，高于示例标准 600 元；发票和行程单齐全，但因为超标，需要补直属主管审批后再提交。
				""".formatted(toolResult);

		return new ChatResponse(List.of(new Generation(new AssistantMessage(answer))));
	}

	private ToolResponseMessage findLastToolResponse(Prompt prompt) {
		List<Message> instructions = prompt.getInstructions();
		for (int i = instructions.size() - 1; i >= 0; i--) {
			if (instructions.get(i) instanceof ToolResponseMessage toolResponseMessage) {
				return toolResponseMessage;
			}
		}
		return null;
	}

}
