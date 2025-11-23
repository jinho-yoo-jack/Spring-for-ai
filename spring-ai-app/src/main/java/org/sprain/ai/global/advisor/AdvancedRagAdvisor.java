package org.sprain.ai.global.advisor;

import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.config.RagConfig;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class AdvancedRagAdvisor implements BaseAdvisor {

    private final VectorStore vectorStore;
    private final RagConfig config;

    public AdvancedRagAdvisor(VectorStore vectorStore) {
        this(vectorStore, RagConfig.builder().build());
    }

    public AdvancedRagAdvisor(VectorStore vectorStore, RagConfig config) {
        this.vectorStore = vectorStore;
        this.config = config;
    }

    @Override
    public String getName() {
        return "AdvancedRagAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userQuery = extractUserQuery(chatClientRequest);

        if (userQuery == null || userQuery.isBlank()) {
            return chatClientRequest;
        }

        // 검색 수행
        List<Document> relevantDocs = searchDocuments(userQuery);

        if (relevantDocs.isEmpty()) {
            log.info("관련 문서 없음: {}", userQuery);

            if (config.isRequireDocuments()) {
                // 문서가 필수인 경우 에러 메시지 추가
                SystemMessage noDocsMessage = new SystemMessage(
                    "죄송하지만 관련 문서를 찾을 수 없어 답변드릴 수 없습니다."
                );

                List<Message> messages = new ArrayList<>();
                messages.add(noDocsMessage);
                messages.addAll(chatClientRequest.prompt().getInstructions());

                return ChatClientRequest.builder()
                    .prompt(new Prompt(messages, chatClientRequest.prompt().getOptions()))
                    .context(chatClientRequest.context())
                    .build();
            }

            return chatClientRequest;
        }

        log.info("검색된 문서: {} 개", relevantDocs.size());

        // 컨텍스트 생성
        String context = buildEnhancedContext(relevantDocs);

        // 시스템 프롬프트 생성
        String systemPrompt = config.getSystemPromptTemplate()
            .replace("{context}", context)
            .replace("{query}", userQuery);

        SystemMessage systemMessage = new SystemMessage(systemPrompt);

        // 메시지 구성
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(chatClientRequest.prompt().getInstructions());

        Prompt enrichedPrompt = new Prompt(
            messages,
            chatClientRequest.prompt().getOptions()
        );

        // Context 저장
        return ChatClientRequest.builder()
            .prompt(enrichedPrompt)
            .context(chatClientRequest.context())
            .context("rag_documents", relevantDocs)
            .context("rag_query", userQuery)
            .context("rag_sources", extractSources(relevantDocs))
            .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        if (!config.isAppendSources()) {
            return chatClientResponse;
        }

        // Context에서 출처 가져오기
        @SuppressWarnings("unchecked")
        List<String> sources = (List<String>) chatClientResponse.context().get("rag_sources");

        if (sources != null && !sources.isEmpty()) {
            String sourcesText = "\n\n**참고 문서:**\n" +
                sources.stream()
                    .map(s -> "- " + s)
                    .collect(Collectors.joining("\n"));

            // chatResponse에서 content 가져오기
            String originalContent = "";
            if (chatClientResponse.chatResponse() != null) {
                originalContent = chatClientResponse.chatResponse()
                    .getResult()
                    .getOutput()
                    .getText();
            }

            String enhancedContent = originalContent + sourcesText;

            // 새로운 ChatResponse 생성
            ChatResponse newChatResponse = ChatResponse.builder()
                .from(chatClientResponse.chatResponse())
                .metadata("sources", sources)
                .build();

            return ChatClientResponse.builder()
                .chatResponse(newChatResponse)
                .context(chatClientResponse.context())
                .build();
        }

        return chatClientResponse;
    }

    /**
     * 문서 검색
     */
    private List<Document> searchDocuments(String query) {
        SearchRequest.Builder searchBuilder = SearchRequest.builder()
            .query(query)
            .topK(config.getTopK())
            .similarityThreshold(config.getSimilarityThreshold());

        // 필터 적용
        if (config.getFilterExpression() != null) {
            searchBuilder.filterExpression(config.getFilterExpression());
        }

        return vectorStore.similaritySearch(searchBuilder.build());
    }

    /**
     * 향상된 컨텍스트 생성
     */
    private String buildEnhancedContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            Map<String, Object> metadata = doc.getMetadata();

            context.append(String.format("### 문서 %d\n\n", i + 1));

            // 메타데이터
            if (metadata != null) {
                String filename = (String) metadata.get("filename");
                String documentId = (String) metadata.get("document_id");

                if (filename != null) {
                    context.append("**출처:** ").append(filename).append("\n");
                }
                if (documentId != null) {
                    context.append("**문서 ID:** ").append(documentId).append("\n");
                }
            }

            // 내용
            context.append("\n**내용:**\n");
            context.append(doc.getText());
            context.append("\n\n---\n\n");
        }

        return context.toString();
    }

    /**
     * 출처 목록 추출
     */
    private List<String> extractSources(List<Document> documents) {
        return documents.stream()
            .map(doc -> {
                Map<String, Object> metadata = doc.getMetadata();
                if (metadata != null) {
                    String filename = (String) metadata.get("filename");
                    if (filename != null) {
                        return filename;
                    }
                }
                return "알 수 없음";
            })
            .distinct()
            .collect(Collectors.toList());
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
}
