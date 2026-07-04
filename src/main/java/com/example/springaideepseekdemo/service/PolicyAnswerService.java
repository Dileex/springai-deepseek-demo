package com.example.springaideepseekdemo.service;

import java.util.List;
import java.util.function.Predicate;

import com.example.springaideepseekdemo.dto.PolicyFailureType;
import com.example.springaideepseekdemo.dto.PolicyStreamEvent;
import com.example.springaideepseekdemo.exception.PolicyToolFailureException;
import com.example.springaideepseekdemo.model.PolicyModelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import reactor.core.publisher.Flux;

@Service
public class PolicyAnswerService {

	private static final Logger log = LoggerFactory.getLogger(PolicyAnswerService.class);

	private static final FailureDecision MODEL_TEMPORARY_FAILURE = new FailureDecision("AI 服务暂时不可用，可以稍后重试。",
			PolicyFailureType.MODEL_TEMPORARY_FAILURE, true, false);

	private static final FailureDecision MODEL_NON_RETRYABLE_FAILURE = new FailureDecision("AI 请求配置或参数异常，需要管理员检查。",
			PolicyFailureType.MODEL_NON_RETRYABLE_FAILURE, false, true);

	private static final FailureDecision POLICY_TOOL_FAILURE = new FailureDecision("制度资料服务暂时不可用，建议稍后再试或转人工确认。",
			PolicyFailureType.POLICY_TOOL_FAILURE, true, true);

	private static final FailureDecision UNKNOWN_AI_FAILURE = new FailureDecision("当前无法回答该制度问题，建议转人工确认。",
			PolicyFailureType.UNKNOWN_AI_FAILURE, false, true);

	private static final List<FailureRule> FAILURE_RULES = List.of(
			FailureRule.of(POLICY_TOOL_FAILURE, PolicyToolFailureException.class, ToolExecutionException.class),
			FailureRule.of(POLICY_TOOL_FAILURE, PolicyAnswerService::isPolicyToolFailure),
			FailureRule.of(MODEL_TEMPORARY_FAILURE, TransientAiException.class, ResourceAccessException.class),
			FailureRule.of(MODEL_TEMPORARY_FAILURE, PolicyAnswerService::isRetryableHttpFailure),
			FailureRule.of(MODEL_NON_RETRYABLE_FAILURE, NonTransientAiException.class),
			FailureRule.of(MODEL_NON_RETRYABLE_FAILURE, PolicyAnswerService::isNonRetryableHttpFailure));

	private final PolicyModelClient policyModelClient;

	public PolicyAnswerService(PolicyModelClient policyModelClient) {
		this.policyModelClient = policyModelClient;
	}

	// 流式接口不要让异常直接中断连接，统一转换成一条 FALLBACK 事件。
	public Flux<PolicyStreamEvent> streamAsk(String question) {
		if (question == null || question.isBlank()) {
			return Flux.just(invalidRequest().toPolicyStreamEvent());
		}

		try {
			return policyModelClient.streamAnswer(question.strip())
				.map(PolicyStreamEvent::content)
				.switchIfEmpty(Flux.just(emptyModelResponse().toPolicyStreamEvent()))
				.onErrorResume(this::streamFallback);
		}
		catch (Exception ex) {
			return streamFallback(ex);
		}
	}

	private Flux<PolicyStreamEvent> streamFallback(Throwable ex) {
		FailureDecision decision = toFailureDecision(ex);
		logFailure("stream", decision, ex);
		return Flux.just(decision.toPolicyStreamEvent());
	}

	private FailureDecision toFailureDecision(Throwable ex) {
		return FAILURE_RULES.stream()
			.filter(rule -> rule.matches(ex))
			.map(FailureRule::decision)
			.findFirst()
			.orElse(UNKNOWN_AI_FAILURE);
	}

	private FailureDecision invalidRequest() {
		return new FailureDecision("问题不能为空，请补充要查询的制度问题。", PolicyFailureType.INVALID_REQUEST, false, false);
	}

	private FailureDecision emptyModelResponse() {
		return new FailureDecision("当前没有生成可用回答，建议转人工确认。", PolicyFailureType.EMPTY_MODEL_RESPONSE, true,
				true);
	}

	private void logFailure(String scene, FailureDecision decision, Throwable ex) {
		log.warn("Policy {} failure. type={}, retryable={}, exceptionClass={}, causeChain={}, reason={}", scene,
				decision.failureType(), decision.retryable(), ex.getClass().getName(), causeChain(ex),
				ex.getMessage());
		log.debug("Policy {} failure detail", scene, ex);
	}

	private record FailureDecision(String answer, PolicyFailureType failureType, boolean retryable,
			boolean needHumanReview) {

		private PolicyStreamEvent toPolicyStreamEvent() {
			return PolicyStreamEvent.fallback(answer, failureType, retryable, needHumanReview);
		}

	}

	private record FailureRule(FailureDecision decision, List<Class<? extends Throwable>> exceptionTypes,
			Predicate<Throwable> matcher) {

		private FailureRule(FailureDecision decision, Predicate<Throwable> matcher) {
			this(decision, List.of(), matcher);
		}

		@SafeVarargs
		private static FailureRule of(FailureDecision decision, Class<? extends Throwable>... exceptionTypes) {
			return new FailureRule(decision, List.of(exceptionTypes), null);
		}

		private static FailureRule of(FailureDecision decision, Predicate<Throwable> matcher) {
			return new FailureRule(decision, matcher);
		}

		private boolean matches(Throwable ex) {
			// Reactor / WebClient 往往会包一层异常，所以这里顺着 cause 链一起匹配。
			return contains(ex, throwable -> exceptionTypes.stream().anyMatch(type -> type.isInstance(throwable)))
					|| (matcher != null && contains(ex, matcher));
		}

	}

	private static boolean contains(Throwable ex, Predicate<Throwable> matcher) {
		Throwable current = ex;
		while (current != null) {
			if (matcher.test(current)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private static boolean isRetryableHttpFailure(Throwable ex) {
		if (!(ex instanceof WebClientResponseException webClientException)) {
			return false;
		}
		HttpStatusCode statusCode = webClientException.getStatusCode();
		return statusCode.value() == 429 || statusCode.is5xxServerError();
	}

	private static boolean isPolicyToolFailure(Throwable ex) {
		return ex instanceof IllegalStateException && "POLICY_SOURCE_TIMEOUT".equals(ex.getMessage());
	}

	private static boolean isNonRetryableHttpFailure(Throwable ex) {
		if (!(ex instanceof WebClientResponseException webClientException)) {
			return false;
		}
		HttpStatusCode statusCode = webClientException.getStatusCode();
		return statusCode.is4xxClientError() && statusCode.value() != 429;
	}

	private static String causeChain(Throwable ex) {
		StringBuilder builder = new StringBuilder();
		Throwable current = ex;
		while (current != null) {
			if (!builder.isEmpty()) {
				builder.append(" <- ");
			}
			builder.append(current.getClass().getSimpleName());
			current = current.getCause();
		}
		return builder.toString();
	}

}
