package com.example.springaideepseekdemo.tool;

public record LodgingPolicyResult(
		String city,
		double amount,
		double standard,
		boolean overLimit,
		boolean missingMaterials,
		String decision,
		String policyBasis) {
}
