package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.tool.CodeReviewTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/skills/code-review")
public class CodeReviewSkillController {

    private static final String SYSTEM_PROMPT = """
            你是一个 Java 代码审查助手。

            如果用户提交 Java 代码并要求审查，请先调用 Skill 工具加载 code-review-skill。
            加载技能书后，再按照技能书里的审查顺序调用 reviewJavaCode 工具。

            输出要求：
            - 用中文回答；
            - 优先指出 bug、安全风险、边界条件和缺失测试；
            - 不要只做泛泛总结。
            """;

    private final ChatClient chatClient;

    private final ToolCallback skillTool;

    private final CodeReviewTools codeReviewTools;

    public CodeReviewSkillController(ChatClient.Builder builder, ToolCallback skillTool, CodeReviewTools codeReviewTools) {
        this.chatClient = builder.build();
        this.skillTool = skillTool;
        this.codeReviewTools = codeReviewTools;
    }

    @PostMapping("/run")
    public String run(@RequestBody String code) {
        try {
            String content = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user("请审查下面这段 Java 代码：\n\n" + code)
                    .tools(skillTool, codeReviewTools)
                    .call()
                    .content();
            if (content != null && content.contains("代码审查报告")) {
                return content;
            }
        } catch (Exception ignored) {
            // Keep the demo runnable when the model key is unavailable or the provider does not trigger tools.
        }
        return """
                模型没有在本轮稳定触发工具调用，已走应用侧兜底审查。

                %s
                """.formatted(codeReviewTools.reviewJavaCode(code));
    }

    @PostMapping("/review")
    public String review(@RequestBody String code) {
        return codeReviewTools.reviewJavaCode(code);
    }
}
