package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.dto.PolicyAnswer;
import com.example.springaideepseekdemo.service.PolicyAnswerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PolicyAskController {

	private final PolicyAnswerService policyAnswerService;

	public PolicyAskController(PolicyAnswerService policyAnswerService) {
		this.policyAnswerService = policyAnswerService;
	}

	@GetMapping("/policy/ask")
	public PolicyAnswer ask(@RequestParam String question) {
		return policyAnswerService.ask(question);
	}

}
