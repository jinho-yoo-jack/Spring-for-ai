package org.spring.ai.resource;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 프로젝트 문서 Resources
 */
@Slf4j
@Component
public class ProjectDocsMcpResources {

    @Value("${company.docs.base-path}")
    private String docsBasePath;

    /**
     * ✅ Project Alpha README
     */
    @McpResource(
        uri = "project://alpha/readme",
        name = "Project Alpha README",
        description = "Alpha 프로젝트의 README 문서",
        mimeType = "text/markdown"
    )
    public String getProjectAlphaReadme() {
        return loadProjectReadme("alpha");
    }

    /**
     * ✅ Project Beta README
     */
    @McpResource(
        uri = "project://beta/readme",
        name = "Project Beta README",
        description = "Beta 프로젝트의 README 문서",
        mimeType = "text/markdown"
    )
    public String getProjectBetaReadme() {
        return loadProjectReadme("beta");
    }

    /**
     * ✅ Project Gamma README
     */
    @McpResource(
        uri = "project://gamma/readme",
        name = "Project Gamma README",
        description = "Gamma 프로젝트의 README 문서",
        mimeType = "text/markdown"
    )
    public String getProjectGammaReadme() {
        return loadProjectReadme("gamma");
    }

    /**
     * ✅ Project Alpha API Docs
     */
    @McpResource(
        uri = "project://alpha/api-docs",
        name = "Project Alpha API Documentation",
        description = "Alpha 프로젝트 API 문서",
        mimeType = "text/markdown"
    )
    public String getProjectAlphaApiDocs() {
        return loadProjectDoc("alpha", "API.md");
    }

    /**
     * 공통 로직 - 프로젝트 README 로드
     */
    private String loadProjectReadme(String projectId) {
        return loadProjectDoc(projectId, "README.md");
    }

    /**
     * 공통 로직 - 프로젝트 문서 로드
     */
    private String loadProjectDoc(String projectId, String docName) {
        try {
            log.info("📁 프로젝트 문서 로드 - 프로젝트: {}, 문서: {}", projectId, docName);

            Path docPath = Paths.get(docsBasePath, "projects", projectId, docName);

            if (!Files.exists(docPath)) {
                return String.format(
                    "# 오류\n프로젝트 '%s'의 '%s' 문서를 찾을 수 없습니다.",
                    projectId,
                    docName
                );
            }

            return Files.readString(docPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("❌ 프로젝트 문서 로드 실패 - 프로젝트: {}, 문서: {}",
                projectId, docName, e);
            return String.format("# 오류\n문서를 불러올 수 없습니다: %s", e.getMessage());
        }
    }
}