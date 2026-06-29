package com.example.springaideepseekdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.policy")
public record PolicyPromptProperties(String systemPrompt, String userTemplate) {
}
