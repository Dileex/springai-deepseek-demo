package com.example.springaideepseekdemo.tool;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ToolLoopTrace {

	private final ThreadLocal<List<String>> events = ThreadLocal.withInitial(ArrayList::new);

	public void reset() {
		events.set(new ArrayList<>());
	}

	public void add(String event) {
		events.get().add(event);
	}

	public List<String> snapshot() {
		return List.copyOf(events.get());
	}

	public void clear() {
		events.remove();
	}

}
