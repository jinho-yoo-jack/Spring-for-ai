package org.sprain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.MessageRole;
import org.sprain.ai.entity.Conversation;
import org.sprain.ai.entity.Message;
import org.sprain.ai.repository.ConversationRepository;
import org.sprain.ai.repository.MessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConversationSummaryService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationSummaryService(@Qualifier("claudeChatClient") ChatClient chatClient,
                                      ConversationRepository conversationRepository,
                                      MessageRepository messageRepository) {
        this.chatClient = chatClient;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * 오래된 대화를 요약
     */
    @Transactional
    public String summarizeConversation(String conversationId) {
        // 1. 모든 메시지 로드
        List<Message> messages = messageRepository
            .findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (messages.size() < 10) {
            log.info("메시지가 너무 적어 요약하지 않음");
            return null;
        }

        // 2. 대화 내용을 텍스트로 변환
        String conversationText = messages.stream()
            .map(msg -> String.format("%s: %s",
                msg.getRole().name(), msg.getContent()))
            .collect(Collectors.joining("\n"));

        // 3. AI로 요약 생성
        String summaryPrompt = String.format("""
            다음 대화를 3-5문장으로 간결하게 요약해주세요.
            주요 주제와 핵심 내용만 포함하세요.
            
            대화 내용:
            %s
            
            요약:
            """, conversationText);

        String summary = chatClient.prompt()
            .user(summaryPrompt)
            .call()
            .content();

        // 4. 요약을 Conversation에 저장
        Conversation conversation = conversationRepository
            .findById(conversationId)
            .orElseThrow();

        conversation.setSummary(summary);
        conversationRepository.save(conversation);

        log.info("대화 요약 완료: {}", conversationId);
        return summary;
    }

    /**
     * 주기적으로 긴 대화를 요약
     */
    @Transactional
    public void summarizeLongConversations() {
        List<Conversation> conversations = conversationRepository.findAll();

        for (Conversation conv : conversations) {
            long messageCount = messageRepository.countByConversationId(conv.getId());

            // 30개 이상 메시지가 있고 요약이 없는 경우
            if (messageCount >= 30 && conv.getSummary() == null) {
                summarizeConversation(conv.getId());
            }
        }
    }

    /**
     * 슬라이딩 윈도우 전략
     * 오래된 메시지를 요약으로 대체
     */
    public List<Message> getMessagesWithSummary(String conversationId, int recentCount) {
        List<Message> allMessages = messageRepository
            .findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (allMessages.size() <= recentCount) {
            return allMessages;
        }

        // 오래된 메시지들
        List<Message> oldMessages = allMessages.subList(0,
            allMessages.size() - recentCount);

        // 최근 메시지들
        List<Message> recentMessages = allMessages.subList(
            allMessages.size() - recentCount, allMessages.size());

        // 오래된 메시지 요약
        String oldSummary = summarizeMessages(oldMessages);

        // 요약을 시스템 메시지로 추가
        Message summaryMessage = Message.builder()
            .conversationId(conversationId)
            .role(MessageRole.SYSTEM)
            .content("이전 대화 요약: " + oldSummary)
            .build();

        List<Message> result = new ArrayList<>();
        result.add(summaryMessage);
        result.addAll(recentMessages);

        return result;
    }

    private String summarizeMessages(List<Message> messages) {
        String conversationText = messages.stream()
            .map(msg -> String.format("%s: %s",
                msg.getRole().name(), msg.getContent()))
            .collect(Collectors.joining("\n"));

        return chatClient.prompt()
            .user("다음 대화를 간결하게 요약: " + conversationText)
            .call()
            .content();
    }
}
