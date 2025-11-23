package org.sprain.ai.dto;

import java.util.List;

public record RagResponse(String answer, List<DocumentSource> sources) {
}
