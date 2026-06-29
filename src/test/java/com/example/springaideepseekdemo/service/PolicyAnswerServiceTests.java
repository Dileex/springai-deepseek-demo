package com.example.springaideepseekdemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.springaideepseekdemo.dto.PolicyAnswer;
import com.example.springaideepseekdemo.dto.PolicyFailureType;
import com.example.springaideepseekdemo.exception.PolicyToolFailureException;
import com.example.springaideepseekdemo.model.PolicyModelClient;
import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;

class PolicyAnswerServiceTests {

	@Test
	void shouldReturnSuccessWhenModelHasAnswer() {
		PolicyAnswerService service = new PolicyAnswerService(answering("需要提交行程单、发票和付款凭证。"));

		PolicyAnswer answer = service.ask("出差回来后报销需要哪些材料");

		assertThat(answer.success()).isTrue();
		assertThat(answer.answer()).contains("行程单");
		assertThat(answer.failureType()).isNull();
		assertThat(answer.retryable()).isFalse();
		assertThat(answer.needHumanReview()).isFalse();
	}

	@Test
	void shouldFallbackWhenQuestionIsBlank() {
		PolicyAnswerService service = new PolicyAnswerService(answering("不会被调用"));

		PolicyAnswer answer = service.ask(" ");

		assertThat(answer.success()).isFalse();
		assertThat(answer.failureType()).isEqualTo(PolicyFailureType.INVALID_REQUEST);
		assertThat(answer.retryable()).isFalse();
	}

	@Test
	void shouldFallbackWhenModelReturnsEmptyAnswer() {
		PolicyAnswerService service = new PolicyAnswerService(answering(" "));

		PolicyAnswer answer = service.ask("出差回来后报销需要哪些材料");

		assertThat(answer.success()).isFalse();
		assertThat(answer.failureType()).isEqualTo(PolicyFailureType.EMPTY_MODEL_RESPONSE);
		assertThat(answer.retryable()).isTrue();
		assertThat(answer.needHumanReview()).isTrue();
	}

	@Test
	void shouldMarkTransientFailureAsRetryable() {
		PolicyAnswerService service = new PolicyAnswerService(failing(new TransientAiException("timeout")));

		PolicyAnswer answer = service.ask("出差回来后报销需要哪些材料");

		assertThat(answer.success()).isFalse();
		assertThat(answer.failureType()).isEqualTo(PolicyFailureType.MODEL_TEMPORARY_FAILURE);
		assertThat(answer.retryable()).isTrue();
		assertThat(answer.needHumanReview()).isFalse();
	}

	@Test
	void shouldMarkNonTransientFailureAsManualReview() {
		PolicyAnswerService service = new PolicyAnswerService(failing(new NonTransientAiException("bad api key")));

		PolicyAnswer answer = service.ask("出差回来后报销需要哪些材料");

		assertThat(answer.success()).isFalse();
		assertThat(answer.failureType()).isEqualTo(PolicyFailureType.MODEL_NON_RETRYABLE_FAILURE);
		assertThat(answer.retryable()).isFalse();
		assertThat(answer.needHumanReview()).isTrue();
	}

	@Test
	void shouldHandleToolFailureSeparately() {
		RuntimeException cause = new RuntimeException("POLICY_SOURCE_TIMEOUT");
		PolicyAnswerService service = new PolicyAnswerService(failing(new PolicyToolFailureException("tool failed",
				cause)));

		PolicyAnswer answer = service.ask("资料库故障时怎么处理");

		assertThat(answer.success()).isFalse();
		assertThat(answer.failureType()).isEqualTo(PolicyFailureType.POLICY_TOOL_FAILURE);
		assertThat(answer.retryable()).isTrue();
		assertThat(answer.needHumanReview()).isTrue();
	}

	private PolicyModelClient answering(String answer) {
		return question -> answer;
	}

	private PolicyModelClient failing(RuntimeException exception) {
		return question -> {
			throw exception;
		};
	}

}
