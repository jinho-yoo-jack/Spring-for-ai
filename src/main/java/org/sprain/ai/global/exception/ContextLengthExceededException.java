package org.sprain.ai.global.exception;

import org.sprain.ai.global.GlobalException;

public class ContextLengthExceededException extends RuntimeException {
    public ContextLengthExceededException() {
    }
    public ContextLengthExceededException(String message) {
        super(message);
    }
}
