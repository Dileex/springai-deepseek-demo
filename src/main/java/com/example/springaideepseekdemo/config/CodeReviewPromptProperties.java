package com.example.springaideepseekdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.code-review.prompt")
public class CodeReviewPromptProperties {

    private String system = "";

    private String userTemplate = "";

    public String system() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String userTemplate() {
        return userTemplate;
    }

    public void setUserTemplate(String userTemplate) {
        this.userTemplate = userTemplate;
    }
}
