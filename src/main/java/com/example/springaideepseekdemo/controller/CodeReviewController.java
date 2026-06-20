package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.dto.CodeReviewResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeReviewController {

    private final ChatClient chatClient;
    private final String userTemplate;

    public CodeReviewController(ChatClient.Builder builder,
            @Value("${app.code-review.system-prompt}") String systemPrompt,
            @Value("${app.code-review.user-template}") String userTemplate) {
        this.chatClient = builder
                .defaultSystem(systemPrompt)
                .build();
        this.userTemplate = userTemplate;
    }

    @PostMapping("/code/review")
    public CodeReviewResult review(@RequestBody String code) {
        return chatClient.prompt()
                .user(u -> u.text(userTemplate)
                        .param("code", code))
                .call()
                .entity(CodeReviewResult.class);
    }
}
