package com.example.springaideepseekdemo.dto;

public enum PolicyFailureType {

	// 请求本身不合法，比如问题为空。
	INVALID_REQUEST,

	// 模型调用成功，但没有生成可用回答。
	EMPTY_MODEL_RESPONSE,

	// 模型或网络临时不可用，适合稍后重试。
	MODEL_TEMPORARY_FAILURE,

	// API Key、模型名、请求参数等配置类错误，重试通常没有意义。
	MODEL_NON_RETRYABLE_FAILURE,

	// 制度资料工具调用失败，比如资料库超时或不可用。
	POLICY_TOOL_FAILURE,

	// 没有被明确归类的 AI 调用异常。
	UNKNOWN_AI_FAILURE

}
