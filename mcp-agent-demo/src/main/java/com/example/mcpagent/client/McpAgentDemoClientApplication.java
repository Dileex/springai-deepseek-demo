package com.example.mcpagent.client;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(DemoAgentProperties.class)
public class McpAgentDemoClientApplication {

	private static final String MCP_SERVER_URL_PROPERTY =
			"spring.ai.mcp.client.streamable-http.connections.order-service.url";

	private static final String DEFAULT_MCP_SERVER_URL = "http://localhost:8080";

	public static void main(String[] args) {
		String serverUrl = resolveMcpServerUrl(args);
		checkMcpServerReachable(serverUrl);

		SpringApplication application = new SpringApplication(McpAgentDemoClientApplication.class);
		application.setDefaultProperties(Map.of(MCP_SERVER_URL_PROPERTY, serverUrl));
		application.run(args);
	}

	@Bean
	ApplicationRunner printTools(ToolCallbackProvider toolCallbackProvider, DemoAgentProperties demoAgentProperties) {
		return args -> {
			ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
			System.out.println("Spring AI tools: " + Arrays.stream(tools)
				.map(tool -> tool.getToolDefinition().name())
				.toList());

			if (Arrays.stream(tools)
				.noneMatch(tool -> tool.getToolDefinition().name().equals(demoAgentProperties.expectedToolName()))) {
				System.out.println("Expected tool was not found: " + demoAgentProperties.expectedToolName());
			}
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
