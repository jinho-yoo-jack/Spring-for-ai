package org.sprain.ai.dto;

public record ModelSelectionCriteria(
    TaskType taskType,
    Integer estimatedTokens,
    Double maxCost,
    Integer maxLatencyMs,
    Priority priority
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TaskType taskType;
        private Integer estimatedTokens;
        private Double maxCost;
        private Integer maxLatencyMs;
        private Priority priority = Priority.BALANCED;

        public Builder taskType(TaskType taskType) {
            this.taskType = taskType;
            return this;
        }

        public Builder estimatedTokens(Integer tokens) {
            this.estimatedTokens = tokens;
            return this;
        }

        public Builder maxCost(Double cost) {
            this.maxCost = cost;
            return this;
        }

        public Builder maxLatencyMs(Integer ms) {
            this.maxLatencyMs = ms;
            return this;
        }

        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public ModelSelectionCriteria build() {
            return new ModelSelectionCriteria(
                taskType, estimatedTokens, maxCost, maxLatencyMs, priority
            );
        }
    }
}
