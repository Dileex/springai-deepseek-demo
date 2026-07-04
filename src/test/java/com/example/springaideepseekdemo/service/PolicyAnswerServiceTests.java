package com.example.springaideepseekdemo.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.example.springaideepseekdemo.dto.PolicyFailureType;
import com.example.springaideepseekdemo.dto.PolicyStreamEvent;
import com.example.springaideepseekdemo.exception.PolicyToolFailureException;
import com.example.springaideepseekdemo.model.PolicyModelClient;
import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

class PolicyAnswerServiceTests {

	@Test
	void shouldReturnStreamContentAsStructuredEvent() {
		PolicyAnswerService service = new PolicyAnswerService(streaming("需要提交行程单、发票和付款凭证。"));

		List<PolicyStreamEvent> events = service.streamAsk("出差回来后报销需要哪些材料").collectList().block();

		assertThat(events).hasSize(1);
		PolicyStreamEvent event = events.get(0);
		assertThat(event.success()).isTrue();
		assertThat(event.type()).isEqualTo("CONTENT");
		assertThat(event.content()).contains("行程单");
		assertThat(event.failureType()).isNull();
		assertThat(event.retryable()).isFalse();
		assertThat(event.needHumanReview()).isFalse();
	}

	@Test
	void shouldReturnStreamFallbackWhenQuestionIsBlank() {
		PolicyAnswerService service = new PolicyAnswerService(streaming("不会被调用"));

		List<PolicyStreamEvent> events = service.streamAsk(" ").collectList().block();

		assertThat(events).hasSize(1);
		PolicyStreamEvent event = events.get(0);
		assertThat(event.success()).isFalse();
		assertThat(event.type()).isEqualTo("FALLBACK");
		assertThat(event.content()).isEqualTo("问题不能为空，请补充要查询的制度问题。");
		assertThat(event.failureType()).isEqualTo(PolicyFailureType.INVALID_REQUEST);
		assertThat(event.retryable()).isFalse();
		assertThat(event.needHumanReview()).isFalse();
	}

	@Test
	void shouldReturnStreamFallbackWhenModelReturnsNoContent() {
		PolicyAnswerService service = new PolicyAnswerService(question -> Flux.empty());

		List<PolicyStreamEvent> events = service.streamAsk("出差回来后报销需要哪些材料").collectList().block();

		assertThat(events).hasSize(1);
		PolicyStreamEvent event = events.get(0);
		assertThat(event.success()).isFalse();
		assertThat(event.failureType()).isEqualTo(PolicyFailureType.EMPTY_MODEL_RESPONSE);
		assertThat(event.retryable()).isTrue();
		assertThat(event.needHumanReview()).isTrue();
	}

	@Test
	void shouldReturnStreamFallbackWhenModelFailsTemporarily() {
		PolicyAnswerService service = new PolicyAnswerService(failing(new TransientAiException("timeout")));

		List<PolicyStreamEvent> events = service.streamAsk("出差回来后报销需要哪些材料").collectList().block();

		assertThat(events).hasSize(1);
		PolicyStreamEvent event = events.get(0);
		assertThat(event.success()).isFalse();
		assertThat(event.type()).isEqualTo("FALLBACK");
		assertThat(event.content()).isEqualTo("AI 服务暂时不可用，可以稍后重试。");
		assertThat(event.failureType()).isEqualTo(PolicyFailureType.MODEL_TEMPORARY_FAILURE);
		assertThat(event.retryable()).isTrue();
		assertThat(event.needHumanReview()).isFalse();
	}

	@Test
	void shouldReturnStreamFallbackWhenToolFails() {
		RuntimeException cause = new RuntimeException("POLICY_SOURCE_TIMEOUT");
		PolicyAnswerService service = new PolicyAnswerService(failing(new PolicyToolFailureException("tool failed",
				cause)));

		List<PolicyStreamEvent> events = service.streamAsk("资料库故障时怎么处理").collectList().block();

		assertThat(events).hasSize(1);
		PolicyStreamEvent event = events.get(0);
		assertThat(event.success()).isFalse();
		assertThat(event.type()).isEqualTo("FALLBACK");
		assertThat(event.content()).isEqualTo("制度资料服务暂时不可用，建议稍后再试或转人工确认。");
		assertThat(event.failureType()).isEqualTo(PolicyFailureType.POLICY_TOOL_FAILURE);
		assertThat(event.retryable()).isTrue();
		assertThat(event.needHumanReview()).isTrue();
	}

	@Test
	void shouldReturnStreamFallbackWhenHttp401Happens() {
		PolicyAnswerService service = new PolicyAnswerService(failing(httpError(401, "Unauthorized")));

		List<PolicyStreamEvent> events = service.streamAsk("出差回来后报销需要哪些材料").collectList().block();

		assertThat(events).hasSize(1);
		PolicyStreamEvent event = events.get(0);
		assertThat(event.success()).isFalse();
		assertThat(event.failureType()).isEqualTo(PolicyFailureType.MODEL_NON_RETRYABLE_FAILURE);
		assertThat(event.retryable()).isFalse();
		assertThat(event.needHumanReview()).isTrue();
	}

	@Test
	void shouldReturnStreamFallbackWhenHttp429Happens() {
		PolicyAnswerService service = new PolicyAnswerService(failing(httpError(429, "Too Many Requests")));

		List<PolicyStreamEvent> events = service.streamAsk("出差回来后报销需要哪些材料").collectList().block();

		assertThat(events).hasSize(1);
		PolicyStreamEvent event = events.get(0);
		assertThat(event.success()).isFalse();
		assertThat(event.failureType()).isEqualTo(PolicyFailureType.MODEL_TEMPORARY_FAILURE);
		assertThat(event.retryable()).isTrue();
		assertThat(event.needHumanReview()).isFalse();
	}

	private PolicyModelClient streaming(String content) {
		return question -> Flux.just(content);
	}

	private PolicyModelClient failing(RuntimeException exception) {
		return question -> Flux.error(exception);
	}

	private WebClientResponseException httpError(int statusCode, String statusText) {
		return WebClientResponseException.create(statusCode, statusText, HttpHeaders.EMPTY, new byte[0], null);
	}

}
