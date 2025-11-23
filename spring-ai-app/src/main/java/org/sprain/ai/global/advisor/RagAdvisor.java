package org.sprain.ai.global.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RagAdvisor implements BaseAdvisor {

    private final VectorStore vectorStore;
    private final int topK;
    private final double similarityThreshold;
    private final String systemPromptTemplate;

    public RagAdvisor(VectorStore vectorStore) {
        this(vectorStore, 5, 0.7, DEFAULT_SYSTEM_PROMPT);
    }

    public RagAdvisor(VectorStore vectorStore, int topK, double similarityThreshold) {
        this(vectorStore, topK, similarityThreshold, DEFAULT_SYSTEM_PROMPT);
    }

    public RagAdvisor(VectorStore vectorStore, int topK, double similarityThreshold, String systemPromptTemplate) {
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
        this.systemPromptTemplate = systemPromptTemplate;
    }

    private static final String DEFAULT_SYSTEM_PROMPT = """
        다음은 검색된 관련 문서들입니다. 이 문서들을 참고하여 사용자의 질문에 답변해주세요.
        
        **중요 규칙:**
        - 문서에 있는 정보만 사용하여 답변하세요.
        - 문서에 없는 내용은 "제공된 문서에서 해당 정보를 찾을 수 없습니다"라고 답변하세요.
        - 답변 끝에 참고한 문서 출처를 명시하세요.
        
        [검색된 문서]
        {context}
        """;

    @Override
    public String getName() {
        return "RagAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 사용자 질문 추출
        String userQuery = extractUserQuery(chatClientRequest);

        if (userQuery == null || userQuery.isBlank()) {
            log.warn("사용자 질문을 찾을 수 없습니다.");
            return chatClientRequest;
        }

        // Vector Store에서 관련 문서 검색
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(userQuery)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build()
        );

        if (relevantDocs.isEmpty()) {
            log.info("관련 문서를 찾을 수 없습니다: {}", userQuery);
            return chatClientRequest;
        }

        log.info("검색된 문서: {} 개", relevantDocs.size());

        // 문서 컨텍스트 생성
        String context = buildContext(relevantDocs);

        // 시스템 메시지에 컨텍스트 추가
        String systemPrompt = systemPromptTemplate.replace("{context}", context);
        SystemMessage systemMessage = new SystemMessage(systemPrompt);

        // 기존 메시지 + RAG 시스템 메시지
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(chatClientRequest.prompt().getInstructions());

        Prompt enrichedPrompt = new Prompt(
            messages,
            chatClientRequest.prompt().getOptions()
        );

        // Context에 검색된 문서 저장
        return ChatClientRequest.builder()
            .prompt(enrichedPrompt)
            .context(chatClientRequest.context())
            .context("rag_documents", relevantDocs)
            .context("rag_query", userQuery)
            .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    /**
     * 사용자 질문 추출
     */
    private String extractUserQuery(ChatClientRequest request) {
        for (Message message : request.prompt().getInstructions()) {
            if (message instanceof UserMessage) {
                return message.getText();
            }
        }
        return null;
    }

    /**
     * 문서들을 컨텍스트로 변환
     */
    private String buildContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);

            context.append(String.format("[문서 %d]\n", i + 1));
            context.append("내용: ").append(doc.getText()).append("\n");

            // 메타데이터 추가
            if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                String filename = (String) doc.getMetadata().get("filename");
                if (filename != null) {
                    context.append("출처: ").append(filename).append("\n");
                }
            }

            context.append("\n---\n\n");
        }

        return context.toString();
    }
}