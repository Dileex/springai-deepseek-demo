package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.agent.AgentRunRequest;
import com.example.springaideepseekdemo.agent.AgentRunResult;
import com.example.springaideepseekdemo.agent.LogAgentRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents/log")
public class LogAgentController {

	private final LogAgentRunner runner;

	public LogAgentController(LogAgentRunner runner) {
		this.runner = runner;
	}

	@PostMapping("/run")
	public AgentRunResult run(@RequestBody AgentRunRequest request) {
		return runner.run(request);
	}

}
