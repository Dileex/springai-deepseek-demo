package com.example.springaideepseekdemo.dto;

public record PolicyStreamEvent(
		// 本次流式事件是否表示成功内容。
		boolean success,
		// 事件类型：CONTENT 表示模型输出片段，FALLBACK 表示兜底结果。
		String type,
		// 当前片段内容或兜底文案。
		String content,
		// 失败类型。成功片段为空。
		PolicyFailureType failureType,
		// 调用方是否适合稍后重试。
		boolean retryable,
		// 是否建议人工确认。
		boolean needHumanReview) {

	public static PolicyStreamEvent content(String content) {
		return new PolicyStreamEvent(true, "CONTENT", content, null, false, false);
	}

	public static PolicyStreamEvent fallback(String content, PolicyFailureType failureType, boolean retryable,
			boolean needHumanReview) {
		return new PolicyStreamEvent(false, "FALLBACK", content, failureType, retryable, needHumanReview);
	}

}
