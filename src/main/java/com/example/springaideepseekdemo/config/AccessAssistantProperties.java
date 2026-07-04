package com.example.springaideepseekdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.access")
public record AccessAssistantProperties(String systemPrompt, String userTemplate) {
}
