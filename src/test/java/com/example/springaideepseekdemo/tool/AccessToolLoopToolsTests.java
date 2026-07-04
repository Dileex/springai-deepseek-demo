package com.example.springaideepseekdemo.tool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessToolLoopToolsTests {

	@Test
	void shouldQueryEmployeeProfileResourceRiskAndAccessPolicy() {
		ToolLoopTrace trace = new ToolLoopTrace();
		EmployeeProfileTool employeeProfileTool = new EmployeeProfileTool(trace);
		ResourceCatalogTool resourceCatalogTool = new ResourceCatalogTool(trace);
		AccessPolicyTool accessPolicyTool = new AccessPolicyTool(trace);

		trace.reset();
		String profile = employeeProfileTool.getEmployeeProfile("E1002");
		String resourceRisk = resourceCatalogTool.getResourceRisk("订单生产库");
		String policy = accessPolicyTool.searchAccessPolicy("申请订单生产库只读权限，能直接开通吗？");

		assertThat(profile).contains("P6", "订单系统项目组");
		assertThat(resourceRisk).contains("order-prod-db", "P0", "用户敏感信息");
		assertThat(policy).contains("不允许直接开通", "DBA", "安全负责人", "7 天");
		assertThat(trace.snapshot()).hasSize(3);
		assertThat(trace.snapshot().get(0)).startsWith("getEmployeeProfile");
		assertThat(trace.snapshot().get(1)).startsWith("getResourceRisk");
		assertThat(trace.snapshot().get(2)).startsWith("searchAccessPolicy");
	}

}
