package org.sprain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.global.helper.function.ClaudeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final ChatClient.Builder chatClientBuilder;
    private final ClaudeTools claudeTools;

    /**
     * ReAct Agent
     */
    public String solveWithAgent(String goal) {
        log.info("Agent 목표: {}", goal);

        // Agent System Prompt
        String systemPrompt = """
            당신은 자율적으로 문제를 해결하는 AI 에이전트입니다.
            목표를 달성하기 위해 다음과 같이 행동하세요:
            
            1. Thought: 다음에 무엇을 해야 할지 생각하세요
            2. Action: 필요한 도구를 사용하세요
            3. Observation: 결과를 확인하세요
            4. 필요하면 1-3을 반복하세요
            5. Answer: 충분한 정보를 얻으면 최종 답변하세요
            
            사용 가능한 도구:
            - getWeather: 날씨 조회
            - calculator: 계산
            - getUserInfo: 사용자 정보 조회
            - getCurrentTime: 현재 시간
            """;

        ChatClient chatClient = chatClientBuilder
            .defaultSystem(systemPrompt)
            .defaultTools(claudeTools)  // 모든 도구 제공
            .build();

        String response = chatClient.prompt()
            .user(goal)
            .call()
            .content();

        log.info("Agent 완료");
        return response;
    }

    /**
     * 특정 도구만 사용하는 ReAct Agent
     */
    public String solveWithLimitedTools(String goal, String... toolNames) {
        log.info("제한된 도구로 Agent 실행: {}", String.join(", ", toolNames));

        String systemPrompt = """
            당신은 주어진 도구만을 사용하여 문제를 해결하는 AI 에이전트입니다.
            사용 가능한 도구만으로 목표를 달성하세요.
            """;

        ChatClient chatClient = chatClientBuilder
            .defaultSystem(systemPrompt)
            .build();

        return chatClient.prompt()
            .user(goal)
            .toolNames(toolNames)  // 특정 도구만 지정
            .call()
            .content();
    }

    /**
     * 다단계 작업 Agent
     */
    public String executeMultiStepTask(String task) {
        log.info("다단계 작업: {}", task);

        String systemPrompt = """
            당신은 복잡한 작업을 여러 단계로 나누어 수행하는 에이전트입니다.
            작업을 다음과 같이 처리하세요:
            
            1. 작업을 작은 단계들로 분해
            2. 각 단계를 순서대로 실행
            3. 중간 결과를 다음 단계에 활용
            4. 모든 단계 완료 후 최종 보고
            """;

        ChatClient chatClient = chatClientBuilder
            .defaultSystem(systemPrompt)
            .defaultTools(claudeTools)  // 모든 도구 사용 가능
            .build();

        return chatClient.prompt()
            .user(task)
            .call()
            .content();
    }

    /**
     * 대화형 Agent (메모리 포함)
     */
    public String conversationalAgent(String conversationId, String userMessage) {
        log.info("대화형 Agent - ID: {}, 메시지: {}", conversationId, userMessage);

        String systemPrompt = """
            당신은 사용자와 대화하며 도구를 사용하는 AI 어시스턴트입니다.
            이전 대화 내용을 기억하고 문맥에 맞게 응답하세요.
            필요한 경우 도구를 사용하여 정확한 정보를 제공하세요.
            """;

        ChatClient chatClient = chatClientBuilder
            .defaultSystem(systemPrompt)
            .defaultTools(claudeTools)
            .build();

        return chatClient.prompt()
            .user(userMessage)
            .call()
            .content();
    }

    /**
     * 계획-실행 Agent
     */
    public String planAndExecute(String goal) {
        log.info("계획-실행 Agent: {}", goal);

        // 1단계: 계획 수립
        String planningPrompt = """
            다음 목표를 달성하기 위한 구체적인 실행 계획을 수립하세요.
            각 단계를 명확히 나열하고, 어떤 도구를 사용할지 명시하세요.
            
            목표: %s
            """.formatted(goal);

        ChatClient planningClient = chatClientBuilder
            .defaultSystem("당신은 효율적인 계획을 수립하는 AI입니다.")
            .build();

        String plan = planningClient.prompt()
            .user(planningPrompt)
            .call()
            .content();

        log.info("수립된 계획:\n{}", plan);

        // 2단계: 계획 실행
        String executionPrompt = """
            다음 계획을 실행하세요:
            
            %s
            
            원래 목표: %s
            """.formatted(plan, goal);

        ChatClient executionClient = chatClientBuilder
            .defaultSystem("당신은 계획을 정확히 실행하는 AI 에이전트입니다.")
            .defaultTools(claudeTools)
            .build();

        String result = executionClient.prompt()
            .user(executionPrompt)
            .call()
            .content();

        log.info("실행 완료");
        return result;
    }

    /**
     * 자기 반성 Agent (Self-Reflection)
     */
    public String solveWithReflection(String problem, int maxIterations) {
        log.info("자기 반성 Agent 시작 (최대 {}회 반복)", maxIterations);

        String systemPrompt = """
            당신은 자신의 답변을 검증하고 개선하는 AI 에이전트입니다.
            
            1. 문제 해결 시도
            2. 답변 검증 및 개선점 도출
            3. 필요시 다시 시도
            4. 만족스러운 답변일 때 최종 제출
            """;

        ChatClient chatClient = chatClientBuilder
            .defaultSystem(systemPrompt)
            .defaultTools(claudeTools)
            .build();

        String currentAttempt = problem;
        String result = "";

        for (int i = 0; i < maxIterations; i++) {
            log.info("반복 {}/{}", i + 1, maxIterations);

            result = chatClient.prompt()
                .user(currentAttempt)
                .call()
                .content();

            // 검증 프롬프트
            String verificationPrompt = """
                다음 답변을 검증하세요:
                
                질문: %s
                답변: %s
                
                이 답변이 정확하고 완전한가요? 
                개선이 필요하면 구체적으로 어떤 점을 개선해야 하는지 설명하세요.
                만족스럽다면 "APPROVED"라고 답하세요.
                """.formatted(problem, result);

            String verification = chatClient.prompt()
                .user(verificationPrompt)
                .call()
                .content();

            if (verification.contains("APPROVED")) {
                log.info("답변 승인됨 ({}회 반복)", i + 1);
                break;
            }

            currentAttempt = """
                이전 시도:
                %s
                
                개선 필요사항:
                %s
                
                원래 문제: %s
                
                개선된 답변을 제시하세요.
                """.formatted(result, verification, problem);
        }

        return result;
    }
}