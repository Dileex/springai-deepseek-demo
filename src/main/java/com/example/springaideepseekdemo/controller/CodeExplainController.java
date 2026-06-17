package com.example.springaideepseekdemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class CodeExplainController {

    private final ChatClient chatClient;

    public CodeExplainController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                        你是一个资深 Java 工程师，负责解释和审查 Java/Spring 代码。
                        请用简洁中文回答，先给结论，再解释原因。
                        如果代码存在风险，请指出触发条件和建议改法。
                        如果信息不足，请说明还需要补充什么，不要编造项目背景。
                        不要输出与代码无关的泛泛建议。
                        输出格式：
                        1. 代码功能概述
                        2. 关键逻辑解释
                        3. 潜在问题或优化建议
                        """)
                .build();
    }

    @PostMapping("/code/explain")
    public String explain(
            @RequestParam(defaultValue = "Java") String language,
            @RequestParam(defaultValue = "unknown") String filePath,
            @RequestParam(defaultValue = "可读性、性能、潜在 bug") String focus,
            @RequestBody String code) {

        return chatClient.prompt()
                .user(u -> u.text("""
                        请解释以下 {language} 代码。

                        文件路径：{filePath}

                        代码内容：
                        {code}

                        重点关注：{focus}
                        """)
                        .param("language", language)
                        .param("filePath", filePath)
                        .param("code", code)
                        .param("focus", focus))
                .call()
                .content();
    }

    @PostMapping(value = "/code/explain/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> explainStream(
            @RequestParam(defaultValue = "Java") String language,
            @RequestParam(defaultValue = "unknown") String filePath,
            @RequestParam(defaultValue = "可读性、性能、潜在 bug") String focus,
            @RequestBody String code) {

        return chatClient.prompt()
                .user(u -> u.text("""
                        请解释以下 {language} 代码。

                        文件路径：{filePath}

                        代码内容：
                        {code}

                        重点关注：{focus}
                        """)
                        .param("language", language)
                        .param("filePath", filePath)
                        .param("code", code)
                        .param("focus", focus))
                .stream()
                .content();
    }

}
