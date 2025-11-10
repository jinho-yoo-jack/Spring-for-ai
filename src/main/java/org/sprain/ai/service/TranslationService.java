package org.sprain.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    private final ChatClient chatClient;

    public TranslationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String translate(String text, String targetLanguage) {
        return chatClient.prompt()
            .system("당신은 전문 번역가입니다. 주어진 텍스트를 자연스럽게 번역해주세요.")
            .user("다음 텍스트를 " + targetLanguage + "로 번역해주세요: " + text)
            .call()
            .content();
    }
}