package com.example.springaideepseekdemo.model;

import java.util.List;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class ObservedFakeChatModel implements ChatModel {

	private static final DefaultChatModelObservationConvention OBSERVATION_CONVENTION =
			new DefaultChatModelObservationConvention();

	private final ObservationRegistry observationRegistry;

	public ObservedFakeChatModel(ObservationRegistry observationRegistry) {
		this.observationRegistry = observationRegistry;
	}

	@Override
	public ChatResponse call(Prompt prompt) {
		ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
			.prompt(prompt)
			.provider("demo")
			.build();

		return ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
			.observation(OBSERVATION_CONVENTION, OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {
				ChatResponse response = buildResponse(prompt);
				observationContext.setResponse(response);
				return response;
			});
	}

	private ChatResponse buildResponse(Prompt prompt) {
		String question = prompt.getContents();
		String answer = """
				这次上海住宿费 720 元，高于示例标准 600 元。
				发票和行程单齐全，所以材料不是主要问题。
				真正需要关注的是超标审批：建议补直属主管审批后再提交。
				""";

		int promptTokens = estimateTokens(question) + 1200;
		int completionTokens = estimateTokens(answer);
		ChatResponseMetadata metadata = ChatResponseMetadata.builder()
			.model("observability-demo-model")
			.usage(new DefaultUsage(promptTokens, completionTokens))
			.build();

		return new ChatResponse(List.of(new Generation(new AssistantMessage(answer))), metadata);
	}

	private int estimateTokens(String text) {
		return Math.max(1, text.length() / 2);
	}

}
