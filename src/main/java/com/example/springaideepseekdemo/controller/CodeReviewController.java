package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.dto.CodeReviewResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeReviewController {

    private final ChatClient chatClient;

    public CodeReviewController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                        你是一个资深 Java 代码审查助手。
                        只根据用户提供的代码做判断，不要编造不存在的上下文。
                        riskLevel 只能返回 LOW、MEDIUM、HIGH 三种值。
                        """)
                .build();
    }

    @PostMapping("/code/review")
    public CodeReviewResult review(@RequestBody String code) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        请审查下面这段 Java 代码。

                        代码：
                        {code}

                        判断要求：
                        - 如果可能导致线上异常，riskLevel 返回 HIGH
                        - 如果只是可读性或小问题，riskLevel 返回 LOW 或 MEDIUM
                        - suggestions 给出可执行的修改建议
                        - needHumanReview 表示是否需要人工确认后再上线
                        """)
                        .param("code", code))
                .call()
                .entity(CodeReviewResult.class);
    }
}
