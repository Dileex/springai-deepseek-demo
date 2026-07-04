package com.example.springaideepseekdemo.tool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ToolLoopTraceTests {

	@Test
	void shouldRecordAndClearToolEvents() {
		ToolLoopTrace trace = new ToolLoopTrace();

		trace.reset();
		trace.add("getEmployeeProfile");
		trace.add("getResourceRisk");
		trace.add("searchAccessPolicy");

		assertThat(trace.snapshot()).containsExactly("getEmployeeProfile", "getResourceRisk", "searchAccessPolicy");

		trace.clear();

		assertThat(trace.snapshot()).isEmpty();
	}

}
