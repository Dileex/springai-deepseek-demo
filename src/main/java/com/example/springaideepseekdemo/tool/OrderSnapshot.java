package com.example.springaideepseekdemo.tool;

public record OrderSnapshot(
		String orderNo,
		String customerName,
		String status,
		String paidAt,
		String shipCompany,
		String trackingNo,
		String latestTracking,
		String afterSalesPolicy,
		String suggestion,
		String evidence) {
}
