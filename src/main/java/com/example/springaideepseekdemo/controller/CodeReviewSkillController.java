package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.config.CodeReviewPromptProperties;
import com.example.springaideepseekdemo.tool.CodeReviewTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/skills/code-review")
public class CodeReviewSkillController {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewSkillController.class);

    private final ChatClient chatClient;

    private final CodeReviewPromptProperties promptProperties;

    private final ToolCallback skillTool;

    private final CodeReviewTools codeReviewTools;

    public CodeReviewSkillController(ChatClient.Builder builder,
                                     CodeReviewPromptProperties promptProperties,
                                     ToolCallback skillTool,
                                     CodeReviewTools codeReviewTools) {
        this.chatClient = builder
                .defaultSystem(promptProperties.system())
                .build();
        this.promptProperties = promptProperties;
        this.skillTool = skillTool;
        this.codeReviewTools = codeReviewTools;
    }

    @PostMapping("/run")
    public String run(@RequestBody String code) {
        String source = normalizeCode(code);
        try {
            String content = chatClient.prompt()
                    .user(user -> user.text(promptProperties.userTemplate())
                            .param("code", source))
                    .tools(skillTool, codeReviewTools)
                    .call()
                    .content();
            if (content != null && content.contains("代码审查报告")) {
                return content;
            }
        } catch (Exception ex) {
            log.warn("Code review model call failed, fallback to local tool. reason={}", ex.getMessage());
        }
        return """
                模型没有在本轮稳定触发工具调用，已走应用侧兜底审查。

                %s
                """.formatted(codeReviewTools.reviewJavaCode(source));
    }

    @PostMapping(value = "/run/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> runStream(@RequestBody String code) {
        String source = normalizeCode(code);
        return chatClient.prompt()
                .user(user -> user.text(promptProperties.userTemplate())
                        .param("code", source))
                .tools(skillTool, codeReviewTools)
                .stream()
                .content()
                .onErrorResume(ex -> {
                    log.warn("Code review stream call failed, fallback to local tool. reason={}", ex.getMessage());
                    return Flux.just("""
                            模型没有在本轮稳定触发流式工具调用，已走应用侧兜底审查。

                            %s
                            """.formatted(codeReviewTools.reviewJavaCode(source)));
                });
    }

    @PostMapping("/review")
    public String review(@RequestBody String code) {
        return codeReviewTools.reviewJavaCode(normalizeCode(code));
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.strip();
    }
}
