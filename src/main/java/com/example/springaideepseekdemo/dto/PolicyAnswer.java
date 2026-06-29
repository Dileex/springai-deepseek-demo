package com.example.springaideepseekdemo.dto;

public record PolicyAnswer(boolean success, String answer, PolicyFailureType failureType, boolean retryable,
		boolean needHumanReview) {

	public static PolicyAnswer success(String answer) {
		return new PolicyAnswer(true, answer, null, false, false);
	}

	public static PolicyAnswer fallback(String answer, PolicyFailureType failureType, boolean retryable,
			boolean needHumanReview) {
		return new PolicyAnswer(false, answer, failureType, retryable, needHumanReview);
	}

}
