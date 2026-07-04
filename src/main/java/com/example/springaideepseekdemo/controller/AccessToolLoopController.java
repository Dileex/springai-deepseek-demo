package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.dto.AccessToolLoopResponse;
import com.example.springaideepseekdemo.service.AccessToolLoopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessToolLoopController {

	private final AccessToolLoopService accessToolLoopService;

	public AccessToolLoopController(AccessToolLoopService accessToolLoopService) {
		this.accessToolLoopService = accessToolLoopService;
	}

	@GetMapping("/tool-loop/ask")
	public AccessToolLoopResponse ask(@RequestParam(defaultValue = "E1002") String employeeId,
			@RequestParam(defaultValue = "申请订单生产库只读权限，能直接开通吗？") String question) {
		return accessToolLoopService.ask(employeeId, question);
	}

}
