package com.example.springaideepseekdemo.toolcalling;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class OrderTools {

    @Tool(description = "根据订单ID查询订单物流信息，返回物流状态和预计送达时间")
    public String queryOrderLogistics(
            @ToolParam(description = "订单ID，5到20位数字") String orderId) {

        if (orderId == null || !orderId.matches("\\d{5,20}")) {
            return "订单ID格式不正确，订单ID应为5到20位数字";
        }

        if ("12345".equals(orderId)) {
            return "订单已发货，当前在北京分拨中心，预计明天送达";
        }

        return "未找到该订单的物流信息";
    }
}
