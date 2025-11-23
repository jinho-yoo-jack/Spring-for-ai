package org.sprain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.config.RagConfig;
import org.sprain.ai.global.advisor.AdvancedRagAdvisor;
import org.sprain.ai.global.advisor.UsageLoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagAdvisorAppliedService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    public String ask(String question) {
        log.info("RAG 질문: {}", question);

        RagConfig config = RagConfig.builder()
            .topK(10)
            .similarityThreshold(0.75)
            .requireDocuments(false)
            .appendSources(true)
            .build();

        ChatClient chatClient = chatClientBuilder
            .defaultAdvisors(
                new UsageLoggingAdvisor(),
                new AdvancedRagAdvisor(vectorStore, config)
            )
            .build();

        return chatClient.prompt()
            .user(question)
            .call()
            .content();
    }
}