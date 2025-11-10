package org.sprain.ai.global;

import org.sprain.ai.dto.ApiResponse;
import org.sprain.ai.global.exception.ContextLengthExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(ContextLengthExceededException.class)
    public ResponseEntity<ApiResponse<?>> contextLengthExceededException(Exception e) {
        return ResponseEntity.status(500)
            .body(ApiResponse.failure(e.getMessage()));
    }
}
