package com.example.springaideepseekdemo.model;

import reactor.core.publisher.Flux;

public interface PolicyModelClient {

	// 统一使用流式模型输出，避免普通返回和流式返回维护两套兜底逻辑。
	Flux<String> streamAnswer(String question);

}
