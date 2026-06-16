package com.example.springaideepseekdemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;

    public McpController(ChatClient.Builder builder, ToolCallbackProvider mcpTools) {
        this.chatClient = builder
                .defaultSystem("""
                        你是一个文件读取助手。
                        只能通过工具访问 /private/tmp/spring-ai-mcp-demo 目录。
                        当用户说 test.txt 或测试目录时，都指 /private/tmp/spring-ai-mcp-demo。
                        不要请求访问 /、用户主目录、项目源码目录或其他目录。
                        """)
                .build();
        this.mcpTools = mcpTools;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .tools(mcpTools)
                .call()
                .content();
    }
}
