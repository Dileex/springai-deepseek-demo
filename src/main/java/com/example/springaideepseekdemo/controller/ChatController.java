package com.example.springaideepseekdemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个简洁、准确的 Java 技术助手。")
                .build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .stream()
                .content();
    }

    @GetMapping("/code-helper")
    public String codeHelper(@RequestParam String question) {
    return chatClient.prompt()
        .system("""
            你是一个专业的 Java 代码助手。
            你擅长 Spring Boot、Spring AI、微服务和工程化实践。
            回答要简洁、准确，优先给可落地的建议。
            """)
        .user(question)
        .call()
        .content();
    }
}