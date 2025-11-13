package org.sprain.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class McpService {

    private final ChatClient chatClient;

    public McpService(@Qualifier("mcpChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 간단한 Tool 호출
     */
    public String simpleChat(String userMessage) {
        String result = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
        log.info("result: {}", result);
        return result;
    }

    /**
     * 시스템 프롬프트와 함께 Tool 호출
     */
    public String chatWithSystemPrompt(String userMessage) {
        return chatClient.prompt()
                .system("""
                        당신은 친절한 날씨 안내 AI입니다.
                        사용자의 질문에 대해 제공된 Tool을 활용하여 정확한 정보를 제공하세요.
                        답변은 한국어로 작성하고, 친근한 톤을 유지하세요.
                        """)
                .user(userMessage)
                .call()
                .content();
    }

    /**
     * 특정 Tool만 사용하도록 제한
     */
    public String chatWithSpecificTools(String userMessage, String... toolNames) {
        return chatClient.prompt()
                .user(userMessage)
                .functions(toolNames)
                .call()
                .content();
    }

    /**
     * 스트리밍 응답
     */
    public Flux<String> streamChat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    /**
     * 멀티턴 대화
     */
    public String multiTurnChat(List<ChatMessage> messages) {
        List<Message> messageList = new ArrayList<>();

        for (ChatMessage msg : messages) {
            if (msg.role().equalsIgnoreCase("user")) {
                messageList.add(new UserMessage(msg.content()));
            } else if (msg.role().equalsIgnoreCase("assistant")) {
                messageList.add(new AssistantMessage(msg.content()));
            } else if (msg.role().equalsIgnoreCase("system")) {
                messageList.add(new SystemMessage(msg.content()));
            }
        }

        Prompt prompt = new Prompt(messageList);
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    /**
     * 대화 히스토리를 포함한 채팅
     */
    public String chatWithHistory(List<ChatMessage> history, String newMessage) {
        List<Message> messageList = new ArrayList<>();

        for (ChatMessage msg : history) {
            if (msg.role().equalsIgnoreCase("user")) {
                messageList.add(new UserMessage(msg.content()));
            } else if (msg.role().equalsIgnoreCase("assistant")) {
                messageList.add(new AssistantMessage(msg.content()));
            } else if (msg.role().equalsIgnoreCase("system")) {
                messageList.add(new SystemMessage(msg.content()));
            }
        }

        messageList.add(new UserMessage(newMessage));

        Prompt prompt = new Prompt(messageList);
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    /**
     * 시스템 프롬프트 + 대화 히스토리
     */
    public String chatWithSystemAndHistory(
            String systemPrompt,
            List<ChatMessage> history,
            String newMessage
    ) {
        List<Message> messageList = new ArrayList<>();

        messageList.add(new SystemMessage(systemPrompt));

        for (ChatMessage msg : history) {
            if (msg.role().equalsIgnoreCase("user")) {
                messageList.add(new UserMessage(msg.content()));
            } else if (msg.role().equalsIgnoreCase("assistant")) {
                messageList.add(new AssistantMessage(msg.content()));
            }
        }

        messageList.add(new UserMessage(newMessage));

        Prompt prompt = new Prompt(messageList);
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    /**
     * ✅ 수정: 고급 옵션과 함께 채팅 (Ollama용)
     */
    public String chatWithOptions(
            String userMessage,
            Double temperature,
            Integer maxTokens
    ) {
        // OllamaOptions 빌더 사용
        OllamaOptions optionsBuilder = OllamaOptions.builder();

        if (temperature != null) {
            optionsBuilder.setTemperature(temperature);
        }
        if (maxTokens != null) {
            optionsBuilder.setMaxTokens(maxTokens);
        }

        return chatClient.prompt()
                .user(userMessage)
                .options(optionsBuilder.build())
                .call()
                .content();
    }

    /**
     * ✅ 추가: 더 많은 옵션 설정
     */
    public String chatWithAdvancedOptions(
            String userMessage,
            ChatOptions options
    ) {
        OllamaOptions optionsBuilder = OllamaOptions.builder();

        optionsBuilder.setTemperature(options.temperature());
        optionsBuilder.withNumPredict(options.maxTokens());
        optionsBuilder.withTopP(options.topP());
        optionsBuilder.withTopK(options.topK());

        return chatClient.prompt()
                .user(userMessage)
                .options(optionsBuilder.build())
                .call()
                .content();
    }

    /**
     * ✅ 추가: 전체 응답 정보 가져오기
     */
    public DetailedChatResponse chatWithFullResponse(String userMessage) {
        var chatResponse = chatClient.prompt()
                .user(userMessage)
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getContent();
        var metadata = chatResponse.getMetadata();

        Long totalTokens = null;
        Long promptTokens = null;
        Long completionTokens = null;

        if (metadata.getUsage() != null) {
            totalTokens = metadata.getUsage().getTotalTokens();
            promptTokens = metadata.getUsage().getPromptTokens();
            completionTokens = metadata.getUsage().getGenerationTokens();
        }

        return new DetailedChatResponse(
                content,
                totalTokens,
                promptTokens,
                completionTokens,
                chatResponse.getResult().getMetadata()
        );
    }

    /**
     * 대화 메시지 DTO
     */
    public record ChatMessage(String role, String content) {
    }

    /**
     * 채팅 옵션 DTO
     */
    public record ChatOptions(
            Double temperature,
            Integer maxTokens,
            Double topP,
            Integer topK
    ) {
        public ChatOptions {
            // 기본값 설정
            if (temperature == null) temperature = 0.7;
            if (maxTokens == null) maxTokens = 2000;
            if (topP == null) topP = 0.9;
            if (topK == null) topK = 40;
        }
    }

    /**
     * 상세 응답 DTO
     */
    public record DetailedChatResponse(
            String content,
            Long totalTokens,
            Long promptTokens,
            Long completionTokens,
            Object metadata
    ) {
    }
}