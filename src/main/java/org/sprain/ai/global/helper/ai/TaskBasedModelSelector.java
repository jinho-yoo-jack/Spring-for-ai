package org.sprain.ai.global.helper.ai;

import org.sprain.ai.dto.TaskType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TaskBasedModelSelector {

    private final ChatModel claudeSonnet;
    private final ChatModel llama3;

    public TaskBasedModelSelector(
        @Qualifier("claudeChatModel") ChatModel claudeSonnet,
        @Qualifier("ollamaChatModel") ChatModel llama3
    ) {
        this.claudeSonnet = claudeSonnet;
        this.llama3 = llama3;
    }

    /**
     * 작업 유형에 따라 최적 모델 선택
     */
    public ChatModel selectModelForTask(TaskType taskType) {
        return switch (taskType) {
            // 복잡한 추론, 코딩
            case COMPLEX_REASONING, CODE_GENERATION -> claudeSonnet;

            // 간단한 질문, 분류
            case SIMPLE_QA, CLASSIFICATION -> claudeSonnet;

            // 대화, 창의적 글쓰기
            case CONVERSATION, CREATIVE_WRITING -> claudeSonnet;

            // 요약, 번역 (저렴하게)
            case SUMMARIZATION, TRANSLATION -> claudeSonnet;

            // 테스트, 개발 (무료)
            case TESTING, DEVELOPMENT -> llama3;

            // 기본
            default -> claudeSonnet;
        };
    }

    /**
     * 작업 수행
     */
    public String executeTask(TaskType taskType, String prompt) {
        ChatModel model = selectModelForTask(taskType);

        return ChatClient.builder(model).build().prompt().user(prompt).call().content();
    }
}


