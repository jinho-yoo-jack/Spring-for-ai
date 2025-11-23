package org.sprain.ai.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RagConfig {

    @Builder.Default
    private int topK = 5;

    @Builder.Default
    private double similarityThreshold = 0.7;

    @Builder.Default
    private boolean requireDocuments = false;

    @Builder.Default
    private boolean appendSources = true;

    private String filterExpression;

    @Builder.Default
    private String systemPromptTemplate = """
        다음은 검색된 관련 문서들입니다. 이 문서들을 참고하여 질문에 답변해주세요.
        
        **중요 규칙:**
        - 문서에 있는 정보만 사용하여 답변하세요.
        - 문서에 없는 내용은 "제공된 문서에서 해당 정보를 찾을 수 없습니다"라고 답변하세요.
        - 정확한 인용이 필요한 경우 문서 번호를 명시하세요 (예: [문서 1]).
        
        [사용자 질문]
        {query}
        
        [검색된 문서]
        {context}
        """;
}