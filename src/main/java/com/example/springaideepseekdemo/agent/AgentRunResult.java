package com.example.springaideepseekdemo.agent;

import java.util.List;

public record AgentRunResult(String answer, String status, int attempts, String stopReason, List<AgentEvent> events) {
}
