package org.sprain.ai.controller;

import lombok.RequiredArgsConstructor;
import org.sprain.ai.service.FunctionCallingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/function")
@RequiredArgsConstructor
public class FunctionCallingController {

    private final FunctionCallingService functionCallingService;

    /**
     * Function Calling 테스트
     * POST /api/function/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = functionCallingService.chat(message);

        return ResponseEntity.ok(Map.of(
            "message", message,
            "response", response
        ));
    }
}
