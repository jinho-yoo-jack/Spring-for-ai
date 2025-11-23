package org.sprain.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.dto.RagResponse;
import org.sprain.ai.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    /**
     * 기본 RAG 질문 응답
     * POST /api/rag/ask
     */
    @PostMapping("/ask")
    public ResponseEntity<AnswerResponse> ask(@RequestBody QuestionRequest request) {
        try {
            log.info("RAG 질문 요청: {}", request.question());

            String answer = ragService.ask(request.question());

            return ResponseEntity.ok(new AnswerResponse(
                answer,
                "success"
            ));
        } catch (Exception e) {
            log.error("RAG 질문 처리 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AnswerResponse(
                    "질문 처리 중 오류가 발생했습니다: " + e.getMessage(),
                    "error"
                ));
        }
    }

    /**
     * 시스템 프롬프트를 사용한 RAG
     * POST /api/rag/ask-with-system-prompt
     */
    @PostMapping("/ask-with-system-prompt")
    public ResponseEntity<AnswerResponse> askWithSystemPrompt(@RequestBody QuestionRequest request) {
        try {
            log.info("RAG (System Prompt) 질문 요청: {}", request.question());

            String answer = ragService.askWithSystemPrompt(request.question());

            return ResponseEntity.ok(new AnswerResponse(answer, "success"));
        } catch (Exception e) {
            log.error("RAG 질문 처리 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AnswerResponse(
                    "질문 처리 중 오류가 발생했습니다: " + e.getMessage(),
                    "error"
                ));
        }
    }

    /**
     * 스트리밍 RAG 응답
     * GET /api/rag/ask-stream?question=...
     */
    @GetMapping(value = "/ask-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@RequestParam String question) {
        log.info("RAG 스트리밍 질문 요청: {}", question);

        return Flux.create(sink -> {
            ragService.askStream(question, chunk -> {
                sink.next(chunk);
            });
            sink.complete();
        });
    }

    /**
     * 소스 정보를 포함한 RAG 응답
     * POST /api/rag/ask-with-source
     */
    @PostMapping("/ask-with-source")
    public ResponseEntity<RagResponse> askWithSource(@RequestBody QuestionRequest request) {
        try {
            log.info("RAG (소스 포함) 질문 요청: {}", request.question());

            RagResponse response = ragService.askWithSource(request.question());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("RAG 질문 처리 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RagResponse(
                    "질문 처리 중 오류가 발생했습니다: " + e.getMessage(),
                    List.of()
                ));
        }
    }

    /**
     * 특정 문서에서만 질문 응답
     * POST /api/rag/ask-in-document/{documentId}
     */
    @PostMapping("/ask-in-document/{documentId}")
    public ResponseEntity<AnswerResponse> askInDocument(
        @PathVariable String documentId,
        @RequestBody QuestionRequest request) {
        try {
            log.info("문서 내 RAG 질문 요청: documentId={}, question={}",
                documentId, request.question());

            String answer = ragService.askInDocument(request.question(), documentId);

            return ResponseEntity.ok(new AnswerResponse(answer, "success"));
        } catch (Exception e) {
            log.error("문서 내 RAG 질문 처리 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AnswerResponse(
                    "질문 처리 중 오류가 발생했습니다: " + e.getMessage(),
                    "error"
                ));
        }
    }

    /**
     * 재랭킹을 사용한 RAG
     * POST /api/rag/ask-with-reranking
     */
    @PostMapping("/ask-with-reranking")
    public ResponseEntity<AnswerResponse> askWithReranking(@RequestBody QuestionRequest request) {
        try {
            log.info("RAG (재랭킹) 질문 요청: {}", request.question());

            String answer = ragService.askWithReranking(request.question());

            return ResponseEntity.ok(new AnswerResponse(answer, "success"));
        } catch (Exception e) {
            log.error("RAG 재랭킹 질문 처리 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AnswerResponse(
                    "질문 처리 중 오류가 발생했습니다: " + e.getMessage(),
                    "error"
                ));
        }
    }

    /**
     * 문서 검색 (답변 생성 없이)
     * GET /api/rag/search?query=...&topK=5
     */
    @GetMapping("/search")
    public ResponseEntity<SearchDocumentsResponse> searchDocuments(
        @RequestParam String query,
        @RequestParam(defaultValue = "5") int topK) {
        try {
            log.info("문서 검색 요청: query={}, topK={}", query, topK);

            List<Document> documents = ragService.searchDocuments(query, topK);

            List<DocumentInfo> documentInfos = documents.stream()
                .map(doc -> new DocumentInfo(
                    doc.getId(),
                    doc.getText(),
                    doc.getMetadata()
                ))
                .toList();

            return ResponseEntity.ok(new SearchDocumentsResponse(
                query,
                documents.size(),
                documentInfos
            ));
        } catch (Exception e) {
            log.error("문서 검색 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SearchDocumentsResponse(query, 0, List.of()));
        }
    }

    /**
     * 유사도 점수와 함께 검색
     * GET /api/rag/search-with-score?query=...&topK=5&threshold=0.7
     */
    @GetMapping("/search-with-score")
    public ResponseEntity<SearchDocumentsResponse> searchWithScore(
        @RequestParam String query,
        @RequestParam(defaultValue = "5") int topK,
        @RequestParam(defaultValue = "0.7") double threshold) {
        try {
            log.info("유사도 검색 요청: query={}, topK={}, threshold={}",
                query, topK, threshold);

            List<Document> documents = ragService.searchWithScore(query, topK, threshold);

            List<DocumentInfo> documentInfos = documents.stream()
                .map(doc -> new DocumentInfo(
                    doc.getId(),
                    doc.getText(),
                    doc.getMetadata()
                ))
                .toList();

            return ResponseEntity.ok(new SearchDocumentsResponse(
                query,
                documents.size(),
                documentInfos
            ));
        } catch (Exception e) {
            log.error("유사도 검색 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SearchDocumentsResponse(query, 0, List.of()));
        }
    }

    /**
     * 메타데이터 필터링 검색
     * GET /api/rag/search-with-filter?query=...&documentId=...&topK=5
     */
    @GetMapping("/search-with-filter")
    public ResponseEntity<SearchDocumentsResponse> searchWithFilter(
        @RequestParam String query,
        @RequestParam String documentId,
        @RequestParam(defaultValue = "5") int topK) {
        try {
            log.info("필터링 검색 요청: query={}, documentId={}, topK={}",
                query, documentId, topK);

            List<Document> documents = ragService.searchWithFilter(query, documentId, topK);

            List<DocumentInfo> documentInfos = documents.stream()
                .map(doc -> new DocumentInfo(
                    doc.getId(),
                    doc.getText(),
                    doc.getMetadata()
                ))
                .toList();

            return ResponseEntity.ok(new SearchDocumentsResponse(
                query,
                documents.size(),
                documentInfos
            ));
        } catch (Exception e) {
            log.error("필터링 검색 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SearchDocumentsResponse(query, 0, List.of()));
        }
    }

    /**
     * 검색 결과 요약
     * GET /api/rag/search-summary?query=...
     */
    @GetMapping("/search-summary")
    public ResponseEntity<SearchSummaryResponse> getSearchSummary(@RequestParam String query) {
        try {
            log.info("검색 요약 요청: {}", query);

            String summary = ragService.getSearchSummary(query);

            return ResponseEntity.ok(new SearchSummaryResponse(query, summary));
        } catch (Exception e) {
            log.error("검색 요약 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SearchSummaryResponse(query, "검색 요약 중 오류가 발생했습니다."));
        }
    }

    // ===== DTO 클래스들 =====

    /**
     * 질문 요청
     */
    public record QuestionRequest(String question) {}

    /**
     * 답변 응답
     */
    public record AnswerResponse(String answer, String status) {}

    /**
     * 문서 검색 응답
     */
    public record SearchDocumentsResponse(
        String query,
        int resultCount,
        List<DocumentInfo> documents
    ) {}

    /**
     * 문서 정보
     */
    public record DocumentInfo(
        String id,
        String content,
        Map<String, Object> metadata
    ) {}

    /**
     * 검색 요약 응답
     */
    public record SearchSummaryResponse(String query, String summary) {}
}