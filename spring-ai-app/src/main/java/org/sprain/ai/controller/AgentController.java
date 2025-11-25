package org.sprain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.sprain.ai.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * 기본 ReAct Agent 실행
     * POST /api/agent/solve
     *
     * Request Body:
     * {
     *   "goal": "서울 날씨를 확인하고 기온이 20도 이상이면 '덥다'고 알려줘"
     * }
     */
    @PostMapping("/solve")
    public ResponseEntity<Map<String, String>> solve(@RequestBody Map<String, String> request) {
        String goal = request.get("goal");
        String result = agentService.solveWithAgent(goal);

        return ResponseEntity.ok(Map.of(
                "goal", goal,
                "result", result
        ));
    }

    /**
     * 제한된 도구만 사용하는 Agent
     * POST /api/agent/solve/limited
     *
     * Request Body:
     * {
     *   "goal": "10 + 20 * 3을 계산해줘",
     *   "tools": ["calculator"]
     * }
     */
    @PostMapping("/solve/limited")
    public ResponseEntity<Map<String, Object>> solveWithLimitedTools(
            @RequestBody Map<String, Object> request) {
        String goal = (String) request.get("goal");
        @SuppressWarnings("unchecked")
        List<String> tools = (List<String>) request.get("tools");

        String result = agentService.solveWithLimitedTools(
                goal,
                tools.toArray(new String[0])
        );

        return ResponseEntity.ok(Map.of(
                "goal", goal,
                "tools", tools,
                "result", result
        ));
    }

    /**
     * 다단계 작업 실행
     * POST /api/agent/task
     *
     * Request Body:
     * {
     *   "task": "현재 시간을 확인하고, 사용자 1234의 정보를 조회해줘"
     * }
     */
    @PostMapping("/task")
    public ResponseEntity<Map<String, String>> executeTask(
            @RequestBody Map<String, String> request) {
        String task = request.get("task");
        String result = agentService.executeMultiStepTask(task);

        return ResponseEntity.ok(Map.of(
                "task", task,
                "result", result
        ));
    }

    /**
     * 대화형 Agent (메모리 포함)
     * POST /api/agent/conversation/{conversationId}
     *
     * Request Body:
     * {
     *   "message": "안녕하세요"
     * }
     */
    @PostMapping("/conversation/{conversationId}")
    public ResponseEntity<Map<String, String>> conversationalAgent(
            @PathVariable String conversationId,
            @RequestBody Map<String, String> request) {
        String message = request.get("message");
        String result = agentService.conversationalAgent(conversationId, message);

        return ResponseEntity.ok(Map.of(
                "conversationId", conversationId,
                "message", message,
                "result", result
        ));
    }

    /**
     * 계획-실행 Agent
     * POST /api/agent/plan-execute
     *
     * Request Body:
     * {
     *   "goal": "서울과 부산의 날씨를 비교하고 어느 곳이 더 따뜻한지 알려줘"
     * }
     */
    @PostMapping("/plan-execute")
    public ResponseEntity<Map<String, String>> planAndExecute(
            @RequestBody Map<String, String> request) {
        String goal = request.get("goal");
        String result = agentService.planAndExecute(goal);

        return ResponseEntity.ok(Map.of(
                "goal", goal,
                "result", result
        ));
    }

    /**
     * 자기 반성 Agent
     * POST /api/agent/reflect
     *
     * Request Body:
     * {
     *   "problem": "복잡한 수학 문제를 풀어줘",
     *   "maxIterations": 3
     * }
     */
    @PostMapping("/reflect")
    public ResponseEntity<Map<String, Object>> solveWithReflection(
            @RequestBody Map<String, Object> request) {
        String problem = (String) request.get("problem");
        Integer maxIterations = request.get("maxIterations") != null
                ? (Integer) request.get("maxIterations")
                : 3;

        String result = agentService.solveWithReflection(problem, maxIterations);

        return ResponseEntity.ok(Map.of(
                "problem", problem,
                "maxIterations", maxIterations,
                "result", result
        ));
    }

    /**
     * Agent 상태 확인
     * GET /api/agent/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "message", "Agent service is running"
        ));
    }

    /**
     * 사용 가능한 도구 목록
     * GET /api/agent/tools
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, List<String>>> getAvailableTools() {
        List<String> tools = List.of(
                "getWeather - 날씨 조회",
                "calculator - 계산",
                "getUserInfo - 사용자 정보 조회",
                "getCurrentTime - 현재 시간",
                "sendEmail - 이메일 발송"
        );

        return ResponseEntity.ok(Map.of(
                "tools", tools,
                "count", List.of(String.valueOf(tools.size()))
        ));
    }
}