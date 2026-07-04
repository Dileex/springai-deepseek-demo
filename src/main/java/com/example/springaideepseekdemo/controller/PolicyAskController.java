package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.dto.PolicyStreamEvent;
import com.example.springaideepseekdemo.service.PolicyAnswerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class PolicyAskController {

	private final PolicyAnswerService policyAnswerService;

	public PolicyAskController(PolicyAnswerService policyAnswerService) {
		this.policyAnswerService = policyAnswerService;
	}

	// 生产里的助手类接口通常走流式输出，成功和失败都统一写成 SSE 事件。
	@GetMapping(value = "/policy/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<PolicyStreamEvent> ask(@RequestParam(required = false) String question) {
		return policyAnswerService.streamAsk(question);
	}

}
