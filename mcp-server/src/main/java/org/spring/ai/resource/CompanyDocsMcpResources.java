package org.spring.ai.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 회사 문서 MCP Resources
 * Spring AI 1.1.0-M2 + MCP Java SDK v0.13.1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyDocsMcpResources {

    @Value("${company.docs.base-path}")
    private String docsBasePath;

    private final ObjectMapper objectMapper;

    /**
     * ✅ 회사 정책 문서
     * <p>
     * URI: company://policy
     */
    @McpResource(
        uri = "company://policy",
        name = "Company Policy",
        description = "회사의 모든 정책 문서 (인사, 복지, 보안 등)",
        mimeType = "text/markdown"
    )
    public String getCompanyPolicy() {
        try {
            log.info("📄 회사 정책 문서 로드");
            Path policyPath = Paths.get(docsBasePath, "policy.md");
            return Files.readString(policyPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("❌ 정책 문서 로드 실패", e);
            return "# 오류\n정책 문서를 불러올 수 없습니다.";
        }
    }

    /**
     * ✅ FAQ 리소스
     * <p>
     * URI: company://faq
     */
    @McpResource(
        uri = "company://faq",
        name = "Frequently Asked Questions",
        description = "자주 묻는 질문과 답변 모음",
        mimeType = "text/markdown"
    )
    public String getFAQ() {
        try {
            log.info("❓ FAQ 문서 로드");
            Path faqPath = Paths.get(docsBasePath, "faq.md");
            return Files.readString(faqPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("❌ FAQ 로드 실패", e);
            return "# 오류\nFAQ를 불러올 수 없습니다.";
        }
    }

    /**
     * ✅ 직원 핸드북
     * <p>
     * URI: company://handbook
     */
    @McpResource(
        uri = "company://handbook",
        name = "Employee Handbook",
        description = "직원 핸드북 - 업무 가이드, 행동 강령, 복리후생 안내",
        mimeType = "text/markdown"
    )
    public String getEmployeeHandbook() {
        try {
            log.info("📚 직원 핸드북 로드");
            Path handbookPath = Paths.get(docsBasePath, "handbook.md");
            return Files.readString(handbookPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("❌ 핸드북 로드 실패", e);
            return "# 오류\n핸드북을 불러올 수 없습니다.";
        }
    }

    /**
     * ✅ 데이터베이스 기반 리소스
     * <p>
     * URI: database://employees/summary
     */
    @McpResource(
        uri = "database://employees/summary",
        name = "Employee Summary",
        description = "전체 직원 통계 및 요약 정보",
        mimeType = "application/json"
    )
    public String getEmployeeSummary() {
        try {
            log.info("👥 직원 요약 정보 생성");

            Map<String, Object> summary = Map.of(
                "totalEmployees", 150,
                "departments", Map.of(
                    "개발팀", 60,
                    "디자인팀", 20,
                    "마케팅팀", 30,
                    "인사팀", 15,
                    "기타", 25
                ),
                "averageTenure", "3.5년",
                "employmentTypes", Map.of(
                    "정규직", 130,
                    "계약직", 15,
                    "인턴", 5
                ),
                "lastUpdated", LocalDateTime.now().toString()
            );

            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(summary);
        } catch (Exception e) {
            log.error("❌ 직원 요약 생성 실패", e);
            return "{}";
        }
    }

    /**
     * ✅ 캐시 가능한 설정 리소스
     * <p>
     * URI: config://application
     */
    @McpResource(
        uri = "config://application",
        name = "Application Configuration",
        description = "애플리케이션 설정 정보",
        mimeType = "application/json"
    )
    public String getAppConfiguration() {
        log.info("⚙️ 애플리케이션 설정 로드");

        Map<String, Object> config = Map.of(
            "environment", "production",
            "version", "2.1.0",
            "buildDate", "2025-01-15",
            "features", Map.of(
                "chat", true,
                "analytics", true,
                "notifications", false,
                "darkMode", true
            ),
            "limits", Map.of(
                "maxUploadSize", "10MB",
                "maxConcurrentUsers", 1000,
                "apiRateLimit", "100req/min"
            )
        );

        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(config);
        } catch (Exception e) {
            log.error("❌ 설정 변환 실패", e);
            return "{}";
        }
    }

    /**
     * ✅ 실시간 API 기반 리소스
     * <p>
     * URI: api://github/repos
     */
    @McpResource(
        uri = "api://github/repos",
        name = "GitHub Repositories",
        description = "회사의 GitHub 저장소 목록",
        mimeType = "application/json"
    )
    public String getGitHubRepos() {
        log.info("🐙 GitHub 저장소 목록 조회");

        try {
            // 실제로는 GitHub API 호출
            // RestTemplate restTemplate = new RestTemplate();
            // String response = restTemplate.getForObject(
            //     "https://api.github.com/orgs/your-company/repos",
            //     String.class
            // );

            // 예시 데이터
            var repos = Map.of(
                "repositories", java.util.List.of(
                    Map.of(
                        "name", "spring-ai-demo",
                        "description", "Spring AI 데모 프로젝트",
                        "stars", 125,
                        "language", "Java"
                    ),
                    Map.of(
                        "name", "chat-application",
                        "description", "실시간 채팅 애플리케이션",
                        "stars", 89,
                        "language", "TypeScript"
                    )
                ),
                "totalCount", 2,
                "lastUpdated", LocalDateTime.now().toString()
            );

            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(repos);
        } catch (Exception e) {
            log.error("❌ GitHub API 호출 실패", e);
            return "{\"error\": \"GitHub 정보를 불러올 수 없습니다\"}";
        }
    }
}