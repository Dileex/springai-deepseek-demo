package com.example.springaideepseekdemo.config;

import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class SkillToolConfig {

    @Bean
    public ToolCallback skillTool() {
        return SkillsTool.builder()
                .addSkillsResource(new ClassPathResource("skills"))
                .build();
    }

}
