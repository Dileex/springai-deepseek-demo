package com.example.springaideepseekdemo.agent;

public record AgentEvent(String type, String typeName, String message) {

	public AgentEvent(String type, String message) {
		this(type, resolveTypeName(type), message);
	}

	private static String resolveTypeName(String type) {
		return switch (type) {
			case "RECEIVED_INPUT" -> "收到请求";
			case "TASK_DISCOVERED" -> "识别任务";
			case "ACTIONS_PLANNED" -> "规划动作";
			case "LOOP_ITERATION_STARTED" -> "开始循环";
			case "MODEL_CALL_STARTED" -> "调用模型";
			case "VERIFICATION_PASSED" -> "校验通过";
			case "VERIFICATION_FAILED" -> "校验失败";
			case "LOOP_STOPPED" -> "循环停止";
			default -> "未知事件";
		};
	}

}
