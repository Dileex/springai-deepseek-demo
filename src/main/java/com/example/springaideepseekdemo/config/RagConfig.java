package com.example.springaideepseekdemo.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class RagConfig {
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "demo.rag", name = "load-on-startup", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner loadDocumentsToVectorStore(VectorStore vectorStore) {
        return args -> {
            List<Document> chunks = loadAndSplitDocuments();

            vectorStore.add(chunks);

            System.out.println("已加载 " + chunks.size() + " 个文档片段到向量库");
        };
    }

    private List<Document> loadAndSplitDocuments() {
        Resource resource = new ClassPathResource("docs/leave-policy.txt");

        TextReader reader = new TextReader(resource);
        List<Document> documents = reader.get();

        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.split(documents);
    }
}
