package com.example.mcpagent.client;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderAskController {

	private final OrderAgentService orderAgentService;

	public OrderAskController(OrderAgentService orderAgentService) {
		this.orderAgentService = orderAgentService;
	}

	@PostMapping("/ask")
	public AskResponse ask(@RequestBody(required = false) String question) {
		return new AskResponse(this.orderAgentService.ask(question));
	}

	public record AskResponse(String answer) {
	}

}
