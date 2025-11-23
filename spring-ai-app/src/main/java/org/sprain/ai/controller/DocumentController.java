package org.sprain.ai.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.entity.DocumentEntity;
import org.sprain.ai.service.DocumentService;
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 파일 업로드 및 벡터화
     * POST /api/documents/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
        @RequestPart("file") MultipartFile file) throws IOException {
        log.info("문서 업로드 요청: {}", file.getOriginalFilename());

        DocumentEntity document = documentService.uploadDocument(file);
        return ResponseEntity.ok(new DocumentUploadResponse(
            document.getId().toString(),
            document.getFilename(),
            document.getChunkCount(),
            "문서가 성공적으로 업로드되었습니다."
        ));
    }

    /**
     * 텍스트로 직접 문서 추가
     * POST /api/documents/text
     */
    @PostMapping("/text")
    public ResponseEntity<DocumentUploadResponse> addTextDocument(
        @RequestBody TextDocumentRequest request) {
        try {
            log.info("텍스트 문서 추가 요청: {}", request.filename());

            DocumentEntity document = documentService.addTextDocument(
                request.filename(),
                request.content()
            );

            return ResponseEntity.ok(new DocumentUploadResponse(
                document.getId().toString(),
                document.getFilename(),
                document.getChunkCount(),
                "텍스트 문서가 성공적으로 추가되었습니다."
            ));
        } catch (Exception e) {
            log.error("텍스트 문서 추가 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DocumentUploadResponse(null, null, 0, "문서 추가 실패: " + e.getMessage()));
        }
    }

    /**
     * 모든 문서 조회
     * GET /api/documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentSummary>> getAllDocuments() {
        log.info("전체 문서 목록 조회 요청");

        List<DocumentEntity> documents = documentService.getAllDocuments();

        List<DocumentSummary> summaries = documents.stream()
            .map(doc -> new DocumentSummary(
                doc.getId().toString(),
                doc.getFilename(),
                doc.getContentType(),
                doc.getChunkCount(),
                doc.getUploadedAt(),
                doc.getContent().length()
            ))
            .toList();

        return ResponseEntity.ok(summaries);
    }

    /**
     * 특정 문서 조회
     * GET /api/documents/{documentId}
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentEntity> getDocument(@PathVariable String documentId) {
        log.info("문서 조회 요청: {}", documentId);

        return documentService.getAllDocuments().stream()
            .filter(doc -> doc.getId().equals(documentId))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 문서 삭제
     * DELETE /api/documents/{documentId}
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<DeleteResponse> deleteDocument(@PathVariable String documentId) {
        try {
            log.info("문서 삭제 요청: {}", documentId);

            documentService.deleteDocument(documentId);

            return ResponseEntity.ok(new DeleteResponse(
                documentId,
                "문서가 성공적으로 삭제되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.error("문서 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DeleteResponse(documentId, e.getMessage()));
        } catch (Exception e) {
            log.error("문서 삭제 중 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DeleteResponse(documentId, "문서 삭제 실패: " + e.getMessage()));
        }
    }

    /**
     * 특정 문서 내에서 검색
     * GET /api/documents/{documentId}/search
     */
    @GetMapping("/{documentId}/search")
    public ResponseEntity<SearchResponse> searchInDocument(
        @PathVariable String documentId,
        @RequestParam String query,
        @RequestParam(defaultValue = "5") int topK) {
        try {
            log.info("문서 내 검색 요청: documentId={}, query={}, topK={}", documentId, query, topK);

            List<Document> results = documentService.searchInDocument(documentId, query, topK);

            List<SearchResult> searchResults = results.stream()
                .map(doc -> new SearchResult(
                    doc.getId(),
                    doc.getText(),
                    doc.getMetadata()
                ))
                .toList();

            return ResponseEntity.ok(new SearchResponse(
                documentId,
                query,
                searchResults.size(),
                searchResults
            ));
        } catch (Exception e) {
            log.error("검색 중 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SearchResponse(documentId, query, 0, List.of()));
        }
    }

    // ===== DTO 클래스들 =====

    /**
     * 파일 업로드 응답
     */
    public record DocumentUploadResponse(
        String documentId,
        String filename,
        int chunkCount,
        String message
    ) {
    }

    /**
     * 텍스트 문서 추가 요청
     */
    public record TextDocumentRequest(
        String filename,
        String content
    ) {
    }

    /**
     * 문서 요약 정보
     */
    public record DocumentSummary(
        String id,
        String filename,
        String contentType,
        int chunkCount,
        LocalDateTime createdAt,
        int contentLength
    ) {
    }

    /**
     * 삭제 응답
     */
    public record DeleteResponse(
        String documentId,
        String message
    ) {
    }

    /**
     * 검색 응답
     */
    public record SearchResponse(
        String documentId,
        String query,
        int resultCount,
        List<SearchResult> results
    ) {
    }

    /**
     * 검색 결과
     */
    public record SearchResult(
        String id,
        String content,
        java.util.Map<String, Object> metadata
    ) {
    }
}