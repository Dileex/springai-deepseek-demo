package com.example.springaideepseekdemo.dto;

import java.util.List;

public record AccessToolLoopResponse(String employeeId, String question, String answer, List<String> toolEvents) {
}
