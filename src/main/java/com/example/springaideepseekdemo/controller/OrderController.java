package com.example.springaideepseekdemo.toolcalling;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final ChatClient chatClient;
    private final OrderTools orderTools;

    public OrderController(ChatClient.Builder builder, OrderTools orderTools) {
        this.chatClient = builder.build();
        this.orderTools = orderTools;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .tools(orderTools)
                .call()
                .content();
    }
}
