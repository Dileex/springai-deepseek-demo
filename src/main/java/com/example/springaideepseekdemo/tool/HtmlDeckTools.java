package com.example.springaideepseekdemo.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class HtmlDeckTools {

    private static final String TEMPLATE_PATH = "skills/guizang-ppt-skill/assets/template.html";

    @Tool(description = "基于 guizang-ppt-skill 生成一个单文件 HTML 横向翻页 PPT。参数 topic 是 PPT 主题。不需要外部 API Key")
    public String generateHtmlDeck(String topic) {
        try {
            Path outputPath = createHtmlDeck(topic);
            return """
                    HTML 演示稿已生成。
                    文件路径：%s
                    打开方式：浏览器直接打开这个 HTML 文件即可横向翻页。
                    """.formatted(outputPath);
        } catch (Exception e) {
            return "HTML 演示稿生成失败：" + e.getMessage();
        }
    }

    public Path createHtmlDeck(String topic) throws IOException {
        String safeTitle = sanitizeTitle(topic);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path outputDir = Path.of(System.getProperty("java.io.tmpdir"), "spring-ai-skill-demo", timestamp);
        Files.createDirectories(outputDir);

        String template = readTemplate();
        String html = template
                .replace("[必填] 替换为 PPT 标题 · Deck Title", safeTitle + " · HTML Deck")
                .replace("<!-- SLIDES_HERE -->", buildSlides(safeTitle));

        Path outputPath = outputDir.resolve("index.html");
        Files.writeString(outputPath, html, StandardCharsets.UTF_8);
        return outputPath;
    }

    private String readTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String buildSlides(String title) {
        return """
                <section class="slide dark hero" data-theme="dark">
                  <div class="chrome">
                    <div class="left"><span>Guizang PPT Skill</span><span class="sep"></span><span>HTML Deck</span></div>
                    <div class="right"><span>01 / 03</span></div>
                  </div>
                  <div class="fill center">
                    <div class="kicker">SPRING AI 2.0.0 · SKILL DEMO</div>
                    <h1 class="display-zh">%s</h1>
                    <p class="lead" style="max-width:58vw;margin-top:3vh">用 guizang-ppt-skill 生成一个不依赖外部 API Key 的单文件 HTML 演示稿。</p>
                  </div>
                  <div class="foot"><div class="title">Spring AI Skill Demo</div><div>Generated HTML</div></div>
                </section>

                <section class="slide light" data-theme="light">
                  <div class="chrome">
                    <div class="left"><span>How It Works</span><span class="sep"></span><span>Workflow</span></div>
                    <div class="right"><span>02 / 03</span></div>
                  </div>
                  <div class="split">
                    <div class="col">
                      <div class="kicker">SKILL AS A PLAYBOOK</div>
                      <h2 class="h1-zh">Skill 不是接口调用，而是一套生成规则</h2>
                      <p class="body-zh">Spring AI 通过社区 SkillsTool 读取 SKILL.md。Java Tool 负责把模板落到本地文件，最终生成浏览器可直接打开的 HTML deck。</p>
                    </div>
                    <div class="callout">
                      <div class="q-big">SKILL.md 给模型看，Java Tool 负责真正落地。</div>
                      <span class="cite">Spring AI + SkillsTool + guizang-ppt-skill</span>
                    </div>
                  </div>
                  <div class="foot"><div class="title">No external PPT API key required</div><div>02</div></div>
                </section>

                <section class="slide dark" data-theme="dark">
                  <div class="chrome">
                    <div class="left"><span>Result</span><span class="sep"></span><span>Local File</span></div>
                    <div class="right"><span>03 / 03</span></div>
                  </div>
                  <div class="fill center">
                    <div class="kicker">OUTPUT</div>
                    <h2 class="h1-zh">生成物是一个 HTML 文件</h2>
                    <p class="lead" style="max-width:62vw;margin-top:3vh">它不是原生 .pptx，但胜在轻量、可编辑、浏览器可预览，适合做 Agent Skill 入门 Demo。</p>
                  </div>
                  <div class="foot"><div class="title">Open index.html in browser</div><div>03</div></div>
                </section>
                """.formatted(escapeHtml(title));
    }

    private String sanitizeTitle(String topic) {
        String value = topic == null ? "" : topic.strip();
        if (value.isBlank()) {
            return "Spring AI Skill Demo";
        }
        return value.length() > 60 ? value.substring(0, 60) : value;
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
