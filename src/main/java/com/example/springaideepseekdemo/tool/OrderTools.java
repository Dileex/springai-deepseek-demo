package com.example.springaideepseekdemo.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class OrderTools {

	private static final Logger log = LoggerFactory.getLogger(OrderTools.class);

	@McpTool(name = "query_order_snapshot", description = "根据订单号查询订单状态、物流进度和售后处理建议",
			annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true,
					openWorldHint = false))
	public OrderSnapshot queryOrderSnapshot(
			@McpToolParam(description = "订单号，例如：ORDER-20260621-1001", required = true) String orderNo) {

		log.info("queryOrderSnapshot tool called, orderNo={}", orderNo);

		return readOrders().stream()
			.filter(order -> order.orderNo().equalsIgnoreCase(orderNo))
			.findFirst()
			.orElseGet(() -> new OrderSnapshot(orderNo, "", "NOT_FOUND", "", "", "", "", "",
					"没有查到该订单，请先核对订单号或让用户补充手机号后四位。", "示例订单数据中不存在该订单号。"));
	}

	private List<OrderSnapshot> readOrders() {
		ClassPathResource resource = new ClassPathResource("demo-data/orders.csv");
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			return reader.lines().skip(1).map(this::parseOrder).toList();
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to read demo order file: " + resource.getPath(), ex);
		}
	}

	private OrderSnapshot parseOrder(String line) {
		String[] values = Arrays.copyOf(line.split("\\|", -1), 10);
		return new OrderSnapshot(values[0], values[1], values[2], values[3], values[4], values[5], values[6],
				values[7], values[8], values[9]);
	}

}
