package org.sprain.ai.global.advisor;

import lombok.RequiredArgsConstructor;
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
import org.sprain.ai.entity.DocumentEntity;
import org.sprain.ai.repository.DocumentRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class HybridRagAdvisor implements BaseAdvisor {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final int topK;

    public HybridRagAdvisor(VectorStore vectorStore, DocumentRepository documentRepository) {
        this(vectorStore, documentRepository, 5);
    }

    @Override
    public String getName() {
        return "HybridRagAdvisor";
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

        // 1. Vector 검색
        List<Document> vectorDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(userQuery)
                .topK(topK)
                .build()
        );

        // 2. 키워드 검색
        List<DocumentEntity> keywordDocs = documentRepository.findByContentContaining(userQuery);

        // 3. 결과 병합
        Set<String> vectorDocIds = vectorDocs.stream()
            .map(doc -> (String) doc.getMetadata().get("document_id"))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<Document> hybridDocs = new ArrayList<>(vectorDocs);

        // 키워드 검색에서 추가 문서 추가
        for (DocumentEntity keywordDoc : keywordDocs) {
            if (!vectorDocIds.contains(keywordDoc.getId()) && hybridDocs.size() < topK * 2) {
                Document doc = new Document(
                    keywordDoc.getContent(),
                    Map.of(
                        "document_id", keywordDoc.getId(),
                        "filename", keywordDoc.getFilename(),
                        "source", "keyword_search"
                    )
                );
                hybridDocs.add(doc);
            }
        }

        if (hybridDocs.isEmpty()) {
            log.info("하이브리드 검색 결과 없음: {}", userQuery);
            return chatClientRequest;
        }

        log.info("하이브리드 검색 결과: Vector({}) + Keyword({})",
            vectorDocs.size(),
            hybridDocs.size() - vectorDocs.size()
        );

        // 컨텍스트 생성
        String context = buildContext(hybridDocs);
        String systemPrompt = String.format("""
            다음은 Vector 검색과 키워드 검색을 통해 찾은 관련 문서들입니다.
            이 문서들을 참고하여 답변해주세요.
            
            [검색된 문서]
            %s
            """, context);

        SystemMessage systemMessage = new SystemMessage(systemPrompt);

        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(chatClientRequest.prompt().getInstructions());

        return ChatClientRequest.builder()
            .prompt(new Prompt(messages, chatClientRequest.prompt().getOptions()))
            .context(chatClientRequest.context())
            .context("rag_documents", hybridDocs)
            .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    private String extractUserQuery(ChatClientRequest request) {
        for (Message message : request.prompt().getInstructions()) {
            if (message instanceof UserMessage) {
                return message.getText();
            }
        }
        return null;
    }

    private String buildContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append(String.format("[문서 %d]\n%s\n\n---\n\n", i + 1, doc.getText()));
        }
        return context.toString();
    }
}