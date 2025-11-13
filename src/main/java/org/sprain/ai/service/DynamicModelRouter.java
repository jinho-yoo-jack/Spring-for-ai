package org.sprain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.ModelSelectionCriteria;
import org.sprain.ai.dto.Priority;
import org.sprain.ai.dto.TaskType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import static org.sprain.ai.config.model.ModelHelper.getModelName;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicModelRouter {

    private final TaskBasedModelSelector taskSelector;
    private final CostBasedModelSelector costSelector;
    private final PerformanceBasedModelSelector perfSelector;

    /**
     * 다중 조건 기반 모델 선택
     */
    public ChatModel routeModel(ModelSelectionCriteria criteria) {
        log.info("모델 선택: {}", criteria);

        // 1. 예산 제약 확인
        if (criteria.maxCost() != null) {
            ChatModel budgetModel = costSelector.selectModelByBudget(criteria.maxCost());
            log.info("예산 기반 선택: {}", getModelName(budgetModel));
            return budgetModel;
        }

        // 2. 레이턴시 요구사항 확인
        if (criteria.maxLatencyMs() != null) {
            ChatModel fastModel = perfSelector.selectModelByLatency(criteria.maxLatencyMs());
            log.info("속도 기반 선택: {}", getModelName(fastModel));
            return fastModel;
        }

        // 3. 작업 유형에 따라 선택
        if (criteria.taskType() != null) {
            ChatModel taskModel = taskSelector.selectModelForTask(criteria.taskType());
            log.info("작업 유형 기반 선택: {}", getModelName(taskModel));
            return taskModel;
        }

        // 4. 품질 우선도에 따라 선택
        return switch (criteria.priority()) {
            case COST -> costSelector.selectModelByBudget(0.01);
            case SPEED -> perfSelector.selectFastestModel();
            case QUALITY -> perfSelector.selectBestQualityModel();
            case BALANCED -> perfSelector.selectBalancedModel();
        };
    }

    /**
     * 스마트 라우팅 (자동 분석)
     */
    public ChatModel smartRoute(String userInput) {
        // 입력 분석
        int estimatedTokens = estimateTokens(userInput);
        TaskType taskType = analyzeTaskType(userInput);

        // 조건 생성
        ModelSelectionCriteria criteria = ModelSelectionCriteria.builder()
            .taskType(taskType)
            .estimatedTokens(estimatedTokens)
            .priority(Priority.BALANCED)
            .build();

        return routeModel(criteria);
    }

    private int estimateTokens(String text) {
        // 간단한 토큰 추정 (1 단어 ≈ 1.3 토큰)
        return (int) (text.split("\\s+").length * 1.3);
    }

    private TaskType analyzeTaskType(String input) {
        String lower = input.toLowerCase();

        if (lower.contains("코드") || lower.contains("code")) {
            return TaskType.CODE_GENERATION;
        }
        if (lower.contains("요약") || lower.contains("summarize")) {
            return TaskType.SUMMARIZATION;
        }
        if (lower.contains("번역") || lower.contains("translate")) {
            return TaskType.TRANSLATION;
        }
        if (lower.contains("분류") || lower.contains("classify")) {
            return TaskType.CLASSIFICATION;
        }

        // 기본값
        return TaskType.CONVERSATION;
    }
}