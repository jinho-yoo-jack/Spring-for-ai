package org.sprain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.DocumentSource;
import org.sprain.ai.dto.RagResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(@Qualifier("claudeChatClient") ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * RAG 기반 질문 응답
     */
    public String ask(String question) {
        log.info("RAG 질문: {}", question);

        // 1. Vector Store에서 관련 문서 검색
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(5)  // 상위 5개 문서
                .build()
        );

        if (relevantDocs.isEmpty()) {
            log.warn("관련 문서를 찾을 수 없음");
            return "죄송하지만 관련 정보를 찾을 수 없습니다. 다른 질문을 해주세요.";
        }

        log.info("검색된 문서: {} 개", relevantDocs.size());

        // 검색된 문서의 메타데이터 로깅
        relevantDocs.forEach(doc ->
            log.debug("문서 발견 - ID: {}, 메타데이터: {}",
                doc.getId(), doc.getMetadata())
        );

        // 2. 검색된 문서들을 Context로 조합
        String context = relevantDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

        // 3. LLM에 컨텍스트와 함께 질문 전달
        String prompt = String.format("""
            다음 문서들을 참고하여 질문에 답변해주세요.
            문서에 없는 내용은 답변하지 마세요.
            답변은 한국어로 작성해주세요.
            
            [참고 문서]
            %s
            
            [질문]
            %s
            
            [답변]
            """, context, question);

        String answer = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        log.info("RAG 답변 생성 완료");
        return answer;
    }

    /**
     * 시스템 프롬프트를 사용한 RAG
     */
    public String askWithSystemPrompt(String question) {
        log.info("RAG (System Prompt) 질문: {}", question);

        // 1. 관련 문서 검색
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(5)
                .similarityThreshold(0.7)
                .build()
        );

        if (relevantDocs.isEmpty()) {
            return "죄송하지만 관련 정보를 찾을 수 없습니다.";
        }

        // 2. 컨텍스트 구성
        String context = relevantDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

        // 3. 시스템 프롬프트 + 유저 프롬프트 방식
        String systemPrompt = """
            당신은 문서 기반 질문 응답 시스템입니다.
            제공된 문서의 내용만을 기반으로 정확하게 답변하세요.
            문서에 없는 내용은 "해당 정보는 제공된 문서에 없습니다"라고 답변하세요.
            답변은 친절하고 명확하게 한국어로 작성하세요.
            """;

        String userPrompt = String.format("""
            [참고 문서]
            %s
            
            [질문]
            %s
            """, context, question);

        return chatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .call()
            .content();
    }

    /**
     * 스트리밍 방식의 RAG 답변
     */
    public void askStream(String question, java.util.function.Consumer<String> callback) {
        log.info("RAG 스트리밍 질문: {}", question);

        // 1. 관련 문서 검색
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(5)
                .build()
        );

        if (relevantDocs.isEmpty()) {
            callback.accept("죄송하지만 관련 정보를 찾을 수 없습니다.");
            return;
        }

        // 2. 컨텍스트 구성
        String context = relevantDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = String.format("""
            다음 문서들을 참고하여 질문에 답변해주세요.
            
            [참고 문서]
            %s
            
            [질문]
            %s
            """, context, question);

        // 3. 스트리밍 응답
        chatClient.prompt()
            .user(prompt)
            .stream()
            .content()
            .subscribe(
                callback::accept,
                error -> log.error("스트리밍 오류: {}", error.getMessage()),
                () -> log.info("스트리밍 완료")
            );
    }

    /**
     * 소스 정보를 포함한 RAG 답변
     */
    public RagResponse askWithSource(String question) {
        log.info("RAG (소스 포함) 질문: {}", question);

        // 1. 관련 문서 검색
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(5)
                .build()
        );

        if (relevantDocs.isEmpty()) {
            return new RagResponse(
                "죄송하지만 관련 정보를 찾을 수 없습니다.",
                List.of()
            );
        }

        // 2. 컨텍스트 구성
        String context = relevantDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = String.format("""
            다음 문서들을 참고하여 질문에 답변해주세요.
            
            [참고 문서]
            %s
            
            [질문]
            %s
            """, context, question);

        // 3. 답변 생성
        String answer = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        // 4. 소스 정보 추출
        List<DocumentSource> sources = relevantDocs.stream()
            .map(doc -> new DocumentSource(
                (String) doc.getMetadata().get("filename"),
                (String) doc.getMetadata().get("document_id"),
                doc.getText().substring(0, Math.min(200, doc.getText().length()))
            ))
            .toList();

        return new RagResponse(answer, sources);
    }

    /**
     * 관련 문서만 검색 (답변 생성 없이)
     */
    public List<Document> searchDocuments(String query, int topK) {
        log.info("문서 검색: {} (topK={})", query, topK);

        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build()
        );
    }

    /**
     * 유사도 점수와 함께 검색
     */
    public List<Document> searchWithScore(String query, int topK, double threshold) {
        log.info("유사도 검색: {} (topK={}, threshold={})", query, topK, threshold);

        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build()
        );
    }

    /**
     * 메타데이터 필터링과 함께 검색
     */
    public List<Document> searchWithFilter(String query, String documentId, int topK) {
        log.info("필터링 검색: {} (documentId={}, topK={})", query, documentId, topK);

        return vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression("document_id == '" + documentId + "'")
                .build()
        );
    }

    /**
     * 특정 문서에서만 질문 응답
     */
    public String askInDocument(String question, String documentId) {
        log.info("문서 내 RAG 질문: {} (documentId={})", question, documentId);

        // 1. 특정 문서에서만 검색
        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(3)
                .filterExpression("document_id == '" + documentId + "'")
                .build()
        );

        if (relevantDocs.isEmpty()) {
            return "해당 문서에서 관련 정보를 찾을 수 없습니다.";
        }

        // 2. 컨텍스트 구성 및 답변 생성
        String context = relevantDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = String.format("""
            다음 문서 내용을 참고하여 질문에 답변해주세요.
            
            [문서 내용]
            %s
            
            [질문]
            %s
            """, context, question);

        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }

    /**
     * 고급 RAG: 재랭킹(Re-ranking) 포함
     */
    public String askWithReranking(String question) {
        log.info("RAG (재랭킹) 질문: {}", question);

        // 1. 더 많은 문서 검색 (10개)
        List<Document> candidates = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(10)
                .similarityThreshold(0.5)
                .build()
        );

        if (candidates.isEmpty()) {
            return "관련 정보를 찾을 수 없습니다.";
        }

        // 2. 재랭킹: 키워드 매칭 + 문서 길이 기반
        List<Document> rerankedDocs = candidates.stream()
            .sorted((d1, d2) -> {
                int score1 = countKeywordMatches(d1.getText(), question);
                int score2 = countKeywordMatches(d2.getText(), question);
                return Integer.compare(score2, score1);
            })
            .limit(3)
            .toList();

        // 3. 컨텍스트 구성 및 답변 생성
        String context = rerankedDocs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = String.format("""
            다음 문서들을 참고하여 질문에 답변해주세요.
            
            [참고 문서]
            %s
            
            [질문]
            %s
            """, context, question);

        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }

    /**
     * 키워드 매칭 개수 계산 (재랭킹용)
     */
    private int countKeywordMatches(String content, String question) {
        String[] keywords = question.toLowerCase().split("\\s+");
        String lowerContent = content.toLowerCase();

        int count = 0;
        for (String keyword : keywords) {
            if (keyword.length() > 1 && lowerContent.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 검색 결과 요약 정보 반환
     */
    public String getSearchSummary(String query) {
        List<Document> docs = searchDocuments(query, 5);

        if (docs.isEmpty()) {
            return "검색 결과가 없습니다.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("검색 결과: %d개 문서 발견\n\n", docs.size()));

        for (int i = 0; i < docs.size(); i++) {
            Document doc = docs.get(i);
            summary.append(String.format("[문서 %d]\n", i + 1));
            summary.append(String.format("파일명: %s\n",
                doc.getMetadata().get("filename")));
            summary.append(String.format("미리보기: %s...\n\n",
                doc.getText().substring(0, Math.min(100, doc.getText().length()))));
        }

        return summary.toString();
    }
}