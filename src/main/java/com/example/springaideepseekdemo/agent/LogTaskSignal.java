package com.example.springaideepseekdemo.agent;

import java.util.ArrayList;
import java.util.List;

public record LogTaskSignal(String traceId, String serviceName, String exceptionType, List<String> plannedActions) {

	public String summary() {
		return "traceId=%s, serviceName=%s, exceptionType=%s, plannedActions=%s"
			.formatted(valueOrUnknown(this.traceId), valueOrUnknown(this.serviceName), valueOrUnknown(this.exceptionType),
					this.plannedActions);
	}

	public static LogTaskSignal from(String logText) {
		String traceId = extract(logText, "traceId=([A-Za-z0-9\\-]+)");
		String serviceName = extract(logText, "\\[([a-zA-Z0-9\\-]+),traceId=");
		String exceptionType = extract(logText, "(\\w+(?:Exception|Error))");

		List<String> plannedActions = new ArrayList<>();
		if (traceId != null) {
			plannedActions.add("findLogsByTraceId");
		}
		if (serviceName != null) {
			plannedActions.add("getRecentDeployments");
		}
		if (exceptionType != null) {
			plannedActions.add("searchRunbook");
		}
		if (plannedActions.isEmpty()) {
			plannedActions.add("manualReview");
		}

		return new LogTaskSignal(traceId, serviceName, exceptionType, List.copyOf(plannedActions));
	}

	private static String extract(String text, String regex) {
		var matcher = java.util.regex.Pattern.compile(regex).matcher(text);
		return matcher.find() ? matcher.group(1) : null;
	}

	private static String valueOrUnknown(String value) {
		return value == null ? "UNKNOWN" : value;
	}

}
