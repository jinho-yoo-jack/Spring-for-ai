package org.sprain.ai.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiLlmService {
    private final List<ChatClient> chatClients;
    private Map<String, ChatClient> chatClientGroups = new HashMap<>();

    @PostConstruct
    public void init() {
        chatClients.forEach(client -> {
            String provider = client.getClass().getSimpleName();
            chatClientGroups.put(provider, client);
        });
    }

    /**
     * 특정 LLM 제공자를 선택하여 질문
     */
    public String askWithProvider(String provider, String question) {
        ChatClient client = selectChatClient(provider);

        return client.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
      * 두 LLM 모델에 동시에 질문하고 결과 비교
     */
    public Map<String, String> compareModels(String question) {
        Map<String, String> results = new HashMap<>();

        try {
            String ollamaResponse = ollamaChatClient.prompt()
                    .user(question)
                    .call()
                    .content();
            results.put("ollama", ollamaResponse);
        } catch (Exception e) {
            log.error("Ollama 응답 실패: ", e);
            results.put("ollama", "Error: " + e.getMessage());
        }

        try {
            String claudeResponse = claudeChatClient.prompt()
                    .user(question)
                    .call()
                    .content();
            results.put("claude", claudeResponse);
        } catch (Exception e) {
            log.error("Claude 응답 실패: ", e);
            results.put("claude", "Error: " + e.getMessage());
        }

        return results;
    }

    /**
     * 작업 유형에 따라 최적의 모델 선택
     */
    public String smartRoute(String taskType, String question) {
        return switch (taskType.toLowerCase()) {
            case "creative", "story", "creative-writing" -> {
                log.info("창의적 작업 -> Claude 사용");
                yield claudeChatClient.prompt()
                        .user(question)
                        .call()
                        .content();
            }
            case "quick", "simple", "translation" -> {
                log.info("빠른 작업 -> Ollama 사용");
                yield ollamaChatClient.prompt()
                        .user(question)
                        .call()
                        .content();
            }
            case "analysis", "complex" -> {
                log.info("복잡한 분석 -> Claude 사용");
                yield claudeChatClient.prompt()
                        .user(question)
                        .call()
                        .content();
            }
            default -> {
                log.info("기본 -> Ollama 사용");
                yield ollamaChatClient.prompt()
                        .user(question)
                        .call()
                        .content();
            }
        };
    }

    /**
     * 스트리밍 응답 (선택한 제공자)
     */
    public Flux<String> streamWithProvider(String provider, String question) {
        ChatClient client = selectChatClient(provider);

        return client.prompt()
                .user(question)
                .stream()
                .content();
    }

    /**
     * 다중 모델 투표 시스템 (앙상블)
     */
    public Map<String, Object> ensembleVote(String question) {
        Map<String, String> responses = compareModels(question);

        // 응답 길이, 품질 등을 기반으로 최종 답변 선택
        String finalAnswer = selectBestResponse(responses);

        return Map.of(
                "finalAnswer", finalAnswer,
                "allResponses", responses
        );
    }

    private ChatClient selectChatClient(String provider) {
        return switch (provider.toLowerCase()) {
            case "claude", "anthropic" -> claudeChatClient;
            case "ollama" -> ollamaChatClient;
            default -> throw new IllegalArgumentException("지원하지 않는 제공자: " + provider);
        };
    }

    private String selectBestResponse(Map<String, String> responses) {
        // 간단한 예시: 더 긴 응답을 선택 (실제로는 더 복잡한 로직 필요)
        return responses.values().stream()
                .max((a, b) -> Integer.compare(a.length(), b.length()))
                .orElse("응답 없음");
    }
}
