package org.sprain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.sprain.ai.service.McpService.*;
import org.sprain.ai.service.McpService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/mcp/chat")
@RequiredArgsConstructor
public class McpChatController {

    private final McpService mcpService;

    /**
     * 기본 채팅
     */
    @PostMapping("/simple")
    public ChatResponse simpleChat(@RequestBody ChatRequest request) {
        String response = mcpService.simpleChat(request.message());
        return new ChatResponse(response);
    }

    /**
     * 시스템 프롬프트 포함
     */
    @PostMapping("/with-system")
    public ChatResponse chatWithSystem(@RequestBody ChatRequest request) {
        String response = mcpService.chatWithSystemPrompt(request.message());
        return new ChatResponse(response);
    }

    /**
     * 특정 Tool만 사용
     */
    @PostMapping("/with-tools")
    public ChatResponse chatWithTools(
            @RequestBody ChatRequest request,
            @RequestParam String[] tools
    ) {
        String response = mcpService.chatWithSpecificTools(
                request.message(),
                tools
        );
        return new ChatResponse(response);
    }

    /**
     * 멀티턴 대화
     */
    @PostMapping("/multi-turn")
    public ChatResponse multiTurnChat(@RequestBody MultiTurnRequest request) {
        String response = mcpService.multiTurnChat(request.messages());
        return new ChatResponse(response);
    }

    /**
     * 대화 히스토리 포함
     */
    @PostMapping("/with-history")
    public ChatResponse chatWithHistory(@RequestBody HistoryRequest request) {
        String response = mcpService.chatWithHistory(
                request.history(),
                request.message()
        );
        return new ChatResponse(response);
    }

    /**
     * 옵션과 함께 채팅
     */
    @PostMapping("/with-options")
    public ChatResponse chatWithOptions(@RequestBody ChatOptionsRequest request) {
        String response = mcpService.chatWithOptions(
                request.message(),
                request.temperature(),
                request.maxTokens()
        );
        return new ChatResponse(response);
    }

    /**
     * 고급 옵션과 함께 채팅
     */
    @PostMapping("/with-advanced-options")
    public ChatResponse chatWithAdvancedOptions(
            @RequestBody AdvancedOptionsRequest request
    ) {
        ChatOptions options = new ChatOptions(
                request.temperature(),
                request.maxTokens(),
                request.topP(),
                request.topK()
        );

        String response = mcpService.chatWithAdvancedOptions(
                request.message(),
                options
        );
        return new ChatResponse(response);
    }

    /**
     * 상세 정보 포함 응답
     */
    @PostMapping("/detailed")
    public DetailedChatResponse detailedChat(@RequestBody ChatRequest request) {
        return mcpService.chatWithFullResponse(request.message());
    }

    /**
     * 스트리밍 응답
     */
    @PostMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return mcpService.streamChat(request.message());
    }

    // DTO 정의
    public record ChatRequest(String message) {}

    public record ChatResponse(String response) {}

    public record MultiTurnRequest(List<ChatMessage> messages) {}

    public record HistoryRequest(List<ChatMessage> history, String message) {}

    public record ChatOptionsRequest(
            String message,
            Double temperature,
            Integer maxTokens
    ) {}

    public record AdvancedOptionsRequest(
            String message,
            Double temperature,
            Integer maxTokens,
            Double topP,
            Integer topK
    ) {}
}