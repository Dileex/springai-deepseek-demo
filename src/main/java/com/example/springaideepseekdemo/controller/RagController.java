package com.example.springaideepseekdemo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    private final Resource runbook;

    public RagController(VectorStore vectorStore, ChatClient.Builder builder,
                         @Value("classpath:rag/incident-runbook.txt") Resource runbook) {
        this.vectorStore = vectorStore;
        this.chatClient = builder.build();
        this.runbook = runbook;
    }

    @PostMapping("/ingest")
    public IngestResponse ingest() throws IOException {
        List<Document> documents = loadRunbookDocuments();
        vectorStore.add(documents);
        return new IngestResponse(documents.size(), documents.stream()
                .map(DocumentItem::from)
                .toList());
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam(defaultValue = "支付接口偶发 500，应该先看什么？") String question) {
        List<Document> documents = retrieve(question);
        return new SearchResponse(question, documents.stream()
                .map(DocumentItem::from)
                .toList());
    }

    @GetMapping("/ask")
    public AskResponse ask(@RequestParam(defaultValue = "支付接口偶发 500，应该先看什么？") String question) {
        List<Document> documents = retrieve(question);
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String answer = chatClient.prompt()
                .system("""
                        你是一个 Java 系统排障助手。
                        只能根据给定的排障资料回答。
                        如果资料不足，直接说明资料不足，不要编造没有出现的流程、时间和负责人。
                        """)
                .user(spec -> spec.text("""
                        用户问题：
                        {question}

                        排障资料：
                        {context}
                        """)
                        .param("question", question)
                        .param("context", context))
                .call()
                .content();

        return new AskResponse(question, answer, documents.stream()
                .map(DocumentItem::from)
                .toList());
    }

    private List<Document> retrieve(String question) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(3)
                .filterExpression("system == 'incident' && status == 'published'")
                .build());
    }

    private List<Document> loadRunbookDocuments() throws IOException {
        String content = runbook.getContentAsString(StandardCharsets.UTF_8);
        return Arrays.stream(content.split("(?m)^---\\s*$"))
                .map(String::trim)
                .filter(block -> !block.isBlank())
                .map(this::toDocument)
                .toList();
    }

    private Document toDocument(String block) {
        String[] parts = block.split("\\R\\R", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Runbook block must contain metadata and body separated by a blank line.");
        }

        Map<String, Object> metadata = parseMetadata(parts[0]);
        String id = metadata.remove("id").toString();
        String text = parts[1].trim();
        return new Document(id, text, metadata);
    }

    private Map<String, Object> parseMetadata(String metadataText) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        for (String line : metadataText.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            String[] keyValue = line.split(":", 2);
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Invalid runbook metadata line: " + line);
            }
            metadata.put(keyValue[0].trim(), keyValue[1].trim());
        }
        return metadata;
    }

    public record IngestResponse(int count, List<DocumentItem> documents) {
    }

    public record SearchResponse(String question, List<DocumentItem> matches) {
    }

    public record AskResponse(String question, String answer, List<DocumentItem> references) {
    }

    public record DocumentItem(String id, String text, Double score, Map<String, Object> metadata) {

        static DocumentItem from(Document document) {
            return new DocumentItem(document.getId(), document.getText(), document.getScore(), document.getMetadata());
        }
    }
}
