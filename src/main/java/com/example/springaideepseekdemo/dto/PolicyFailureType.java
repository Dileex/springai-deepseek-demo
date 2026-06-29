package com.example.springaideepseekdemo.dto;

public enum PolicyFailureType {

	INVALID_REQUEST,

	EMPTY_MODEL_RESPONSE,

	MODEL_TEMPORARY_FAILURE,

	MODEL_NON_RETRYABLE_FAILURE,

	POLICY_TOOL_FAILURE,

	UNKNOWN_AI_FAILURE

}
