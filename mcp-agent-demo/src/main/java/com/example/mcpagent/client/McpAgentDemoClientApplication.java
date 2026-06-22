package com.example.mcpagent.client;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpAgentDemoClientApplication {

	private static final String MCP_SERVER_URL_PROPERTY =
			"spring.ai.mcp.client.streamable-http.connections.travel-expense.url";

	private static final String DEFAULT_MCP_SERVER_URL = "http://localhost:8080";

	public static void main(String[] args) {
		String serverUrl = resolveMcpServerUrl(args);
		checkMcpServerReachable(serverUrl);

		SpringApplication application = new SpringApplication(McpAgentDemoClientApplication.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.setDefaultProperties(Map.of(MCP_SERVER_URL_PROPERTY, serverUrl));
		application.run(args);
	}

	@Bean
	CommandLineRunner runAgent(ChatClient.Builder builder, ToolCallbackProvider toolCallbackProvider) {
		return args -> {
			ChatClient chatClient = builder
				.defaultSystem("""
						你是一个差旅报销助手。
						回答前优先使用可用工具查询规则，不要自己编造报销政策。
						""")
				.build();

			ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
			System.out.println("Spring AI tools: " + Arrays.stream(tools)
				.map(tool -> tool.getToolDefinition().name())
				.toList());

			if (Arrays.stream(tools).noneMatch(tool -> tool.getToolDefinition().name().equals("check_lodging_policy"))) {
				throw new IllegalStateException("Spring AI tool check_lodging_policy was not found");
			}

			String result = chatClient.prompt("""
					帮我判断：上海住宿费 720 元，有发票和行程单，可以直接提交报销吗？
					请使用可用工具查询规则后再回答。
					""")
				.tools(toolCallbackProvider)
				.call()
				.content();

			if (!result.contains("MANAGER_APPROVAL_REQUIRED")) {
				throw new IllegalStateException("Unexpected Spring AI MCP agent result: " + result);
			}

			System.out.println("Spring AI agent result: " + result);
		};
	}

	private static void checkMcpServerReachable(String serverUrl) {
		URI uri = URI.create(serverUrl);
		int port = uri.getPort() == -1 ? defaultPort(uri.getScheme()) : uri.getPort();

		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(uri.getHost(), port), 1500);
		}
		catch (Exception ex) {
			System.err.printf("""

                    MCP Server 连接不上：%s

                    请先启动服务端：
                    cd /Users/dilee/Projects/springai-deepseek-demo
                    ./mvnw spring-boot:run

                    如果服务端换了端口，例如 18080，运行 Client 时加上：
                    --spring.ai.mcp.client.streamable-http.connections.travel-expense.url=http://localhost:18080
                    %n""", serverUrl);
			System.exit(2);
		}
	}

	private static String resolveMcpServerUrl(String[] args) {
		String prefix = "--" + MCP_SERVER_URL_PROPERTY + "=";
		return Arrays.stream(args)
			.filter(arg -> arg.startsWith(prefix))
			.map(arg -> arg.substring(prefix.length()))
			.findFirst()
			.orElse(DEFAULT_MCP_SERVER_URL);
	}

	private static int defaultPort(String scheme) {
		return "https".equalsIgnoreCase(scheme) ? 443 : 80;
	}

}
