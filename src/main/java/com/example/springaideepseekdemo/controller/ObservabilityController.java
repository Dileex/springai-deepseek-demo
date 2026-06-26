package com.example.springaideepseekdemo.controller;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/observability")
public class ObservabilityController {

	private final MeterRegistry meterRegistry;

	public ObservabilityController(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@GetMapping("/summary")
	public ObservabilitySummary summary() {
		return new ObservabilitySummary(
				metric("spring.ai.chat.client", "spring_ai_chat_client_seconds"),
				advisorMetrics(),
				metric("gen_ai.client.operation", "gen_ai_client_operation_seconds"),
				new TokenUsage(token("input"), token("output"), token("total")));
	}

	private MetricSummary metric(String micrometerName, String prometheusName) {
		Timer timer = findTimer(micrometerName, prometheusName);
		if (timer == null) {
			return MetricSummary.empty(micrometerName);
		}
		long count = timer.count();
		double totalMillis = timer.totalTime(TimeUnit.MILLISECONDS);
		double maxMillis = timer.max(TimeUnit.MILLISECONDS);
		double displayMillis = maxMillis > 0 || count == 0 ? maxMillis : totalMillis / count;
		return new MetricSummary(micrometerName, count, totalMillis, displayMillis);
	}

	private List<AdvisorMetric> advisorMetrics() {
		return this.meterRegistry.find("spring.ai.advisor")
			.timers()
			.stream()
			.map(timer -> {
				long count = timer.count();
				double totalMillis = timer.totalTime(TimeUnit.MILLISECONDS);
				double maxMillis = timer.max(TimeUnit.MILLISECONDS);
				double displayMillis = maxMillis > 0 || count == 0 ? maxMillis : totalMillis / count;
				return new AdvisorMetric(advisorName(timer), count, totalMillis, displayMillis);
			})
			.sorted(Comparator.comparing(AdvisorMetric::name))
			.toList();
	}

	private String advisorName(Timer timer) {
		return timer.getId()
			.getTags()
			.stream()
			.filter(tag -> "spring.ai.advisor.name".equals(tag.getKey()) || "spring_ai_advisor_name".equals(tag.getKey()))
			.map(Tag::getValue)
			.findFirst()
			.orElse("unknown");
	}

	private double token(String type) {
		Counter counter = findCounter("gen_ai.client.token.usage", "gen_ai_client_token_usage_total", type);
		return counter == null ? 0.0 : counter.count();
	}

	private Timer findTimer(String micrometerName, String prometheusName) {
		Timer timer = this.meterRegistry.find(micrometerName).timer();
		return timer != null ? timer : this.meterRegistry.find(prometheusName).timer();
	}

	private Counter findCounter(String micrometerName, String prometheusName, String tagValue) {
		Counter counter = findCounterByNameAndTagValue(micrometerName, tagValue);
		return counter != null ? counter : findCounterByNameAndTagValue(prometheusName, tagValue);
	}

	private Counter findCounterByNameAndTagValue(String name, String tagValue) {
		return this.meterRegistry.find(name)
			.counters()
			.stream()
			.filter(counter -> counter.getId().getTags().stream().anyMatch(tag -> tagValue.equals(tag.getValue())))
			.findFirst()
			.orElse(null);
	}

	public record ObservabilitySummary(
			MetricSummary chatClient,
			List<AdvisorMetric> advisors,
			MetricSummary chatModel,
			TokenUsage tokenUsage) {
	}

	public record MetricSummary(String name, long count, double totalMillis, double maxMillis) {

		static MetricSummary empty(String name) {
			return new MetricSummary(name, 0, 0.0, 0.0);
		}

	}

	public record AdvisorMetric(String name, long count, double totalMillis, double maxMillis) {
	}

	public record TokenUsage(double input, double output, double total) {
	}

}
