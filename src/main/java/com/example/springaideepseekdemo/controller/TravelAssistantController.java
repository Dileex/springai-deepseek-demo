package com.example.springaideepseekdemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TravelAssistantController {

	private final ChatClient chatClient;

	public TravelAssistantController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/travel/ask")
	public TravelAnswer ask(@RequestParam(defaultValue = "上海住宿费 720 元，有发票和行程单，可以直接报销吗？") String question) {
		long startedAt = System.nanoTime();
		String answer = this.chatClient.prompt(question).call().content();
		long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
		return new TravelAnswer(question, answer, elapsedMillis);
	}

	public record TravelAnswer(String question, String answer, long elapsedMillis) {
	}

}
