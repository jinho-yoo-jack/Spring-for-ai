package org.sprain.ai.global.exception.custom;

public class ContextLengthExceededException extends RuntimeException {
    public ContextLengthExceededException() {
    }
    public ContextLengthExceededException(String message) {
        super(message);
    }
}
