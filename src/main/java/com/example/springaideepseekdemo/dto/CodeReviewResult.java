package com.example.springaideepseekdemo.dto;

import java.util.List;

public record CodeReviewResult(
        String riskLevel,
        String summary,
        List<String> suggestions,
        boolean needHumanReview) {
}
