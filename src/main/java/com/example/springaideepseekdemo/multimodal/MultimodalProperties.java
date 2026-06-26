package com.example.springaideepseekdemo.multimodal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.multimodal")
public record MultimodalProperties(String imagePrompt) {
}
