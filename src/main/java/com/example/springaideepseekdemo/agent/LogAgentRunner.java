package com.example.springaideepseekdemo.agent;

import com.example.springaideepseekdemo.tool.LogAnalysisTools;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LogAgentRunner {

	private static final int MAX_ATTEMPTS = 2;

	private static final Pattern TRACE_ID_PATTERN = Pattern.compile("traceId=([A-Za-z0-9\\-]+)");

	private final ChatClient chatClient;

	private final LogAnalysisTools logAnalysisTools;

	@Value("${app.log-agent.prompt.system}")
	private String systemPrompt;

	@Value("${app.log-agent.prompt.user-template}")
	private String userTemplate;

	public LogAgentRunner(ChatClient.Builder builder, LogAnalysisTools logAnalysisTools) {
		this.chatClient = builder.build();
		this.logAnalysisTools = logAnalysisTools;
	}

	public AgentRunResult run(AgentRunRequest request) {
		List<AgentEvent> events = new ArrayList<>();
		events.add(new AgentEvent("RECEIVED_INPUT", "收到日志分析请求，sessionId=" + request.sessionId()));

		String feedback = "";
		String answer = "";

		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			events.add(new AgentEvent("LOOP_ITERATION_STARTED", "开始第 " + attempt + " 轮分析"));
			events.add(new AgentEvent("MODEL_CALL_STARTED", "调用 ChatClient，并把 LogAnalysisTools 暴露给模型"));

			answer = callModel(request.input(), feedback);

			VerificationResult verification = verify(request.input(), answer);
			if (verification.passed()) {
				events.add(new AgentEvent("VERIFICATION_PASSED", verification.message()));
				events.add(new AgentEvent("LOOP_STOPPED", "达到停止条件：回答已覆盖关键证据"));
				return new AgentRunResult(answer, "COMPLETED", attempt, "VERIFICATION_PASSED", events);
			}

			events.add(new AgentEvent("VERIFICATION_FAILED", verification.message()));
			feedback = verification.message();
		}

		events.add(new AgentEvent("LOOP_STOPPED", "达到最大轮次，交给人工继续判断"));
		return new AgentRunResult(answer, "NEED_MANUAL_REVIEW", MAX_ATTEMPTS, "MAX_ATTEMPTS_REACHED", events);
	}

	private String callModel(String logText, String feedback) {
		String userText = userTemplate.replace("{logText}", logText);
		if (!feedback.isBlank()) {
			userText = userText + """

					上一轮回答没有通过校验，原因如下：
					%s

					请重新分析，并确保回答覆盖关键证据。
					""".formatted(feedback);
		}

		return chatClient.prompt().system(systemPrompt).user(userText).tools(logAnalysisTools).call().content();
	}

	private VerificationResult verify(String logText, String answer) {
		List<String> missing = new ArrayList<>();

		if (!answer.contains("总体结论")) {
			missing.add("缺少总体结论");
		}
		if (!answer.contains("证据")) {
			missing.add("缺少证据说明");
		}
		if (!answer.contains("下一步")) {
			missing.add("缺少下一步建议");
		}

		String traceId = extractTraceId(logText);
		if (traceId != null && !answer.contains(traceId)) {
			missing.add("没有引用 traceId " + traceId);
		}

		if (logText.contains("NullPointerException") && !answer.contains("NullPointerException")) {
			missing.add("没有指出 NullPointerException");
		}

		if (logText.contains("UserContext") && !answer.contains("UserContext")) {
			missing.add("没有引用 UserContext 这个关键类");
		}

		if (missing.isEmpty()) {
			return new VerificationResult(true, "回答覆盖了关键结论、证据和下一步建议");
		}

		return new VerificationResult(false, "校验未通过：" + String.join("；", missing));
	}

	private String extractTraceId(String logText) {
		Matcher matcher = TRACE_ID_PATTERN.matcher(logText);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private record VerificationResult(boolean passed, String message) {
	}

}
