package com.example.springaideepseekdemo.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {

	@Bean
	ChatClient chatClient(ChatModel chatModel, ObservationRegistry observationRegistry) {
		return ChatClient.create(chatModel, observationRegistry);
	}

}
