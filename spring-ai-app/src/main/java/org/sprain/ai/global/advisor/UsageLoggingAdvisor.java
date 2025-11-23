package org.sprain.ai.global.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

@Slf4j
public class UsageLoggingAdvisor implements BaseAdvisor {

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public String getName() {
        return "UsageLoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        startTime.set(System.currentTimeMillis());

        Prompt prompt = chatClientRequest.prompt();

        log.info("===== LLM Request =====");
        for (Message message : prompt.getInstructions()) {
            log.info("[{}]: {}", message.getMessageType(), message.getText());
        }

        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        long duration = System.currentTimeMillis() - startTime.get();
        startTime.remove();

        Usage usage = chatClientResponse.chatResponse().getMetadata().getUsage();

        log.info("===== LLM Response =====");
        log.info("Duration: {}ms", duration);
        log.info("Response: {}", chatClientResponse.context());
        log.info("Prompt Tokens: {}", usage.getPromptTokens());
        log.info("Completion Tokens: {}", usage.getCompletionTokens());
        log.info("Total Tokens: {}", usage.getTotalTokens());
        log.info("Estimated Cost: ${}", calculateCost(usage));
        log.info("========================");

        return chatClientResponse;
    }

    private double calculateCost(Usage usage) {
        // Claude Sonnet 4.5 가격
        double inputCost = usage.getPromptTokens() / 1_000_000.0 * 3.0;
        double outputCost = usage.getCompletionTokens() / 1_000_000.0 * 15.0;
        return inputCost + outputCost;
    }
}