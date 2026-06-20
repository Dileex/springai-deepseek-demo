package com.example.springaideepseekdemo.controller;

import com.example.springaideepseekdemo.evaluation.RefundPolicyRuleEvaluator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/evaluation")
public class AiEvaluationController {

    private static final String QUESTION = "订单进入拣货流程后还能取消吗？";

    private static final String CONTEXT = """
            订单进入仓库拣货流程后，取消接口会返回 ORDER_LOCKED。
            ORDER_LOCKED 表示订单已锁定，不能继续取消。
            """;

    private static final String GOOD_ANSWER = "不能取消。订单进入仓库拣货流程后会返回 ORDER_LOCKED，表示订单已锁定。";

    private static final String BAD_ANSWER = "不能取消。订单已经进入仓库处理阶段，取消后退款一般需要 7-15 个工作日到账。";

    private static final String FACT_CHECK_PROMPT = """
            Evaluate whether the claim is fully supported by the document.
            Respond with exactly one word: yes or no.

            Document:
            {document}

            Claim:
            {claim}
            """;

    private final RefundPolicyRuleEvaluator ruleEvaluator;

    private final ChatClient.Builder chatClientBuilder;

    public AiEvaluationController(RefundPolicyRuleEvaluator ruleEvaluator, ChatClient.Builder chatClientBuilder) {
        this.ruleEvaluator = ruleEvaluator;
        this.chatClientBuilder = chatClientBuilder;
    }

    @GetMapping("/cases")
    public EvaluationCases cases() {
        return new EvaluationCases(QUESTION, CONTEXT, GOOD_ANSWER, BAD_ANSWER);
    }

    @PostMapping("/rule")
    public EvaluationResult rule(@RequestBody(required = false) EvaluationInput input) {
        EvaluationRequest request = buildRequest(input);
        EvaluationResponse response = ruleEvaluator.evaluate(request);
        return EvaluationResult.from(response);
    }

    @PostMapping("/fact-check")
    public EvaluationResult factCheck(@RequestBody(required = false) EvaluationInput input) {
        EvaluationRequest request = buildRequest(input);
        FactCheckingEvaluator evaluator = FactCheckingEvaluator.builder(chatClientBuilder)
                .evaluationPrompt(FACT_CHECK_PROMPT)
                .build();
        EvaluationResponse response = evaluator.evaluate(request);
        return EvaluationResult.from(response);
    }

    private EvaluationRequest buildRequest(EvaluationInput input) {
        String answer = input != null && input.answer() != null && !input.answer().isBlank()
                ? input.answer()
                : BAD_ANSWER;
        return new EvaluationRequest(QUESTION, List.of(new Document(CONTEXT)), answer);
    }

    public record EvaluationInput(String answer) {
    }

    public record EvaluationCases(String question, String context, String goodAnswer, String badAnswer) {
    }

    public record EvaluationResult(boolean pass, float score, String feedback, Map<String, Object> metadata) {

        static EvaluationResult from(EvaluationResponse response) {
            return new EvaluationResult(response.isPass(), response.getScore(), response.getFeedback(), response.getMetadata());
        }
    }
}
