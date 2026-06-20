package com.example.springaideepseekdemo.evaluation;

import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class RefundPolicyRuleEvaluator implements Evaluator {

    private static final Pattern REFUND_TIME_PATTERN = Pattern.compile("\\d+\\s*[-到至]\\s*\\d+\\s*(个)?(工作日|天)");

    @Override
    public EvaluationResponse evaluate(EvaluationRequest evaluationRequest) {
        String context = doGetSupportingData(evaluationRequest);
        String answer = evaluationRequest.getResponseContent() == null ? "" : evaluationRequest.getResponseContent();

        if (answer.isBlank()) {
            return failed("模型回答为空，无法通过评估。", "EMPTY_ANSWER");
        }

        if (containsRefundTime(answer) && !containsRefundTime(context)) {
            return failed("回答里出现了资料未提供的退款时效，属于资料外事实。", "UNSUPPORTED_REFUND_TIME");
        }

        if (!answer.toUpperCase(Locale.ROOT).contains("ORDER_LOCKED")) {
            return failed("回答没有覆盖关键错误码 ORDER_LOCKED。", "MISSING_ORDER_LOCKED");
        }

        return new EvaluationResponse(true, 1.0f, "回答覆盖了关键事实，且没有命中当前规则检查的资料外退款时效。", Map.of(
                "evaluator", "RefundPolicyRuleEvaluator",
                "reason", "PASS"
        ));
    }

    private boolean containsRefundTime(String text) {
        return REFUND_TIME_PATTERN.matcher(text).find();
    }

    private EvaluationResponse failed(String feedback, String reason) {
        return new EvaluationResponse(false, 0.0f, feedback, Map.of(
                "evaluator", "RefundPolicyRuleEvaluator",
                "reason", reason
        ));
    }
}
