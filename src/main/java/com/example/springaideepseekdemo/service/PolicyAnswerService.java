package com.example.springaideepseekdemo.service;

import com.example.springaideepseekdemo.dto.PolicyAnswer;
import com.example.springaideepseekdemo.dto.PolicyFailureType;
import com.example.springaideepseekdemo.exception.PolicyToolFailureException;
import com.example.springaideepseekdemo.model.PolicyModelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
public class PolicyAnswerService {

	private static final Logger log = LoggerFactory.getLogger(PolicyAnswerService.class);

	private final PolicyModelClient policyModelClient;

	public PolicyAnswerService(PolicyModelClient policyModelClient) {
		this.policyModelClient = policyModelClient;
	}

	public PolicyAnswer ask(String question) {
		if (question == null || question.isBlank()) {
			return PolicyAnswer.fallback("问题不能为空，请补充要查询的制度问题。", PolicyFailureType.INVALID_REQUEST, false,
					false);
		}

		try {
			String answer = policyModelClient.answer(question.strip());
			if (answer == null || answer.isBlank()) {
				return PolicyAnswer.fallback("当前没有生成可用回答，建议转人工确认。",
						PolicyFailureType.EMPTY_MODEL_RESPONSE, true, true);
			}
			return PolicyAnswer.success(answer);
		}
		catch (TransientAiException | ResourceAccessException ex) {
			log.warn("Policy answer temporary failure. type={}, retryable={}, reason={}",
					PolicyFailureType.MODEL_TEMPORARY_FAILURE, true, ex.getMessage());
			log.debug("Policy answer temporary failure detail", ex);
			return PolicyAnswer.fallback("AI 服务暂时不可用，可以稍后重试。",
					PolicyFailureType.MODEL_TEMPORARY_FAILURE, true, false);
		}
		catch (NonTransientAiException ex) {
			log.warn("Policy answer non-retryable failure. type={}, retryable={}, reason={}",
					PolicyFailureType.MODEL_NON_RETRYABLE_FAILURE, false, ex.getMessage());
			log.debug("Policy answer non-retryable failure detail", ex);
			return PolicyAnswer.fallback("AI 请求配置或参数异常，需要管理员检查。",
					PolicyFailureType.MODEL_NON_RETRYABLE_FAILURE, false, true);
		}
		catch (PolicyToolFailureException ex) {
			log.warn("Policy answer tool failure. type={}, retryable={}, reason={}",
					PolicyFailureType.POLICY_TOOL_FAILURE, true, ex.getMessage());
			log.debug("Policy answer tool failure detail", ex);
			return PolicyAnswer.fallback("制度资料服务暂时不可用，建议稍后再试或转人工确认。",
					PolicyFailureType.POLICY_TOOL_FAILURE, true, true);
		}
		catch (Exception ex) {
			log.warn("Policy answer unknown failure. type={}, retryable={}", PolicyFailureType.UNKNOWN_AI_FAILURE,
					false, ex);
			return PolicyAnswer.fallback("当前无法回答该制度问题，建议转人工确认。", PolicyFailureType.UNKNOWN_AI_FAILURE,
					false, true);
		}
	}

}
