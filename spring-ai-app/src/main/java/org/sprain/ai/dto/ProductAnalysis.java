package org.sprain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductAnalysis(
    @JsonProperty("sentiment") String sentiment,
    @JsonProperty("score") int score,
    @JsonProperty("summary") String summary
) {}