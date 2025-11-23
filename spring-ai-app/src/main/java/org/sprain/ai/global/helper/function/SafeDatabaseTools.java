package org.sprain.ai.global.helper.function;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sprain.ai.global.helper.function.dto.query.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 안전한 데이터베이스 도구
 * SQL Injection 및 보안 검증 포함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafeDatabaseTools {

    private final JdbcTemplate jdbcTemplate;

    // 위험한 키워드 목록
    private static final Set<String> DANGEROUS_KEYWORDS = Set.of(
        "DROP", "DELETE", "UPDATE", "INSERT", "EXEC", "EXECUTE",
        "ALTER", "CREATE", "TRUNCATE", "GRANT", "REVOKE",
        "UNION", "SHUTDOWN", "DECLARE", "WAITFOR"
    );

    // 허용된 테이블 목록
    private static final Set<String> ALLOWED_TABLES = Set.of(
        "products", "orders", "customers", "categories",
        "order_items", "reviews", "inventory"
    );

    // 최대 쿼리 결과 수
    private static final int MAX_RESULT_SIZE = 1000;

    /**
     * 안전한 SQL 쿼리 실행
     */
    @Tool(description = "안전한 SQL 쿼리를 실행합니다 (SELECT만 허용, 보안 검증 포함)")
    public DatabaseQueryResponse safeExecuteQuery(DatabaseQueryRequest request) {
        log.info("=== 안전한 쿼리 실행 요청 ===");
        log.info("쿼리: {}", request.query());

        try {
            String query = request.query();

            // 1단계: 기본 유효성 검사
            validateQueryBasics(query);

            // 2단계: SQL Injection 방지
            if (containsDangerousKeywords(query)) {
                log.error("위험한 키워드 감지: {}", query);
                return new DatabaseQueryResponse(
                    false,
                    "위험한 쿼리가 감지되었습니다. 실행이 차단되었습니다.",
                    null,
                    0
                );
            }

            // 3단계: SELECT만 허용
            if (!query.trim().toUpperCase().startsWith("SELECT")) {
                log.error("SELECT가 아닌 쿼리 시도: {}", query);
                return new DatabaseQueryResponse(
                    false,
                    "보안상 SELECT 쿼리만 실행 가능합니다.",
                    null,
                    0
                );
            }

            // 4단계: 허용된 테이블만 접근
            if (!isAllowedTable(query)) {
                log.error("허용되지 않은 테이블 접근 시도: {}", query);
                return new DatabaseQueryResponse(
                    false,
                    "접근 권한이 없는 테이블입니다.",
                    null,
                    0
                );
            }

            // 5단계: 쿼리 크기 제한
            String limitedQuery = addLimitIfNeeded(query);

            // 6단계: 안전한 쿼리 실행
            log.info("쿼리 실행 승인: {}", limitedQuery);
            List<Map<String, Object>> results = jdbcTemplate.queryForList(limitedQuery);

            log.info("쿼리 실행 성공: {} 건의 결과", results.size());

            return new DatabaseQueryResponse(
                true,
                "쿼리가 성공적으로 실행되었습니다.",
                results,
                results.size()
            );

        } catch (SecurityException e) {
            log.error("보안 검증 실패", e);
            return new DatabaseQueryResponse(
                false,
                "보안 검증 실패: " + e.getMessage(),
                null,
                0
            );
        } catch (Exception e) {
            log.error("쿼리 실행 실패", e);
            return new DatabaseQueryResponse(
                false,
                "쿼리 실행 중 오류 발생: " + e.getMessage(),
                null,
                0
            );
        }
    }

    /**
     * 기본 유효성 검사
     */
    private void validateQueryBasics(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new SecurityException("쿼리가 비어있습니다");
        }

        if (query.length() > 5000) {
            throw new SecurityException("쿼리가 너무 깁니다 (최대 5000자)");
        }

        // 주석 제거 (SQL Injection 방지)
        if (query.contains("--") || query.contains("/*") || query.contains("*/")) {
            throw new SecurityException("쿼리에 주석을 포함할 수 없습니다");
        }

        // 세미콜론 체크 (다중 쿼리 실행 방지)
        if (query.contains(";") && !query.trim().endsWith(";")) {
            throw new SecurityException("다중 쿼리 실행은 허용되지 않습니다");
        }
    }

    /**
     * 위험한 키워드 포함 여부 확인
     */
    private boolean containsDangerousKeywords(String query) {
        String upperQuery = query.toUpperCase();

        for (String keyword : DANGEROUS_KEYWORDS) {
            // 단어 경계를 고려한 패턴 매칭
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
            if (pattern.matcher(upperQuery).find()) {
                log.warn("위험한 키워드 발견: {}", keyword);
                return true;
            }
        }

        return false;
    }

    /**
     * 허용된 테이블만 접근하는지 확인
     */
    private boolean isAllowedTable(String query) {
        String upperQuery = query.toUpperCase();

        // FROM 절에서 테이블 이름 추출
        Pattern fromPattern = Pattern.compile("FROM\\s+([\\w_]+)", Pattern.CASE_INSENSITIVE);
        var matcher = fromPattern.matcher(query);

        boolean allTablesAllowed = true;

        while (matcher.find()) {
            String tableName = matcher.group(1).toLowerCase();
            log.info("테이블 접근 시도: {}", tableName);

            if (!ALLOWED_TABLES.contains(tableName)) {
                log.warn("허용되지 않은 테이블: {}", tableName);
                allTablesAllowed = false;
                break;
            }
        }

        // JOIN 절의 테이블도 확인
        Pattern joinPattern = Pattern.compile("JOIN\\s+([\\w_]+)", Pattern.CASE_INSENSITIVE);
        matcher = joinPattern.matcher(query);

        while (matcher.find()) {
            String tableName = matcher.group(1).toLowerCase();
            log.info("JOIN 테이블 접근 시도: {}", tableName);

            if (!ALLOWED_TABLES.contains(tableName)) {
                log.warn("허용되지 않은 JOIN 테이블: {}", tableName);
                allTablesAllowed = false;
                break;
            }
        }

        return allTablesAllowed;
    }

    /**
     * LIMIT 절 추가 (결과 크기 제한)
     */
    private String addLimitIfNeeded(String query) {
        String upperQuery = query.toUpperCase();

        // 이미 LIMIT이 있으면 그대로 반환
        if (upperQuery.contains("LIMIT")) {
            return query;
        }

        // LIMIT 추가
        String trimmedQuery = query.trim();
        if (trimmedQuery.endsWith(";")) {
            trimmedQuery = trimmedQuery.substring(0, trimmedQuery.length() - 1);
        }

        return trimmedQuery + " LIMIT " + MAX_RESULT_SIZE;
    }

    /**
     * 안전한 파라미터화된 쿼리 실행
     */
    @Tool(description = "파라미터화된 안전한 쿼리를 실행합니다")
    public DatabaseQueryResponse safeParameterizedQuery(ParameterizedQueryRequest request) {
        log.info("=== 파라미터화된 쿼리 실행 ===");
        log.info("템플릿: {}", request.queryTemplate());
        log.info("파라미터: {}", request.parameters());

        try {
            // 파라미터 검증
            validateParameters(request.parameters());

            // 파라미터화된 쿼리 실행 (SQL Injection 안전)
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                request.queryTemplate(),
                request.parameters().toArray()
            );

            log.info("파라미터화된 쿼리 실행 성공: {} 건", results.size());

            return new DatabaseQueryResponse(
                true,
                "쿼리가 성공적으로 실행되었습니다.",
                results,
                results.size()
            );

        } catch (Exception e) {
            log.error("파라미터화된 쿼리 실행 실패", e);
            return new DatabaseQueryResponse(
                false,
                "쿼리 실행 실패: " + e.getMessage(),
                null,
                0
            );
        }
    }

    /**
     * 파라미터 검증
     */
    private void validateParameters(List<Object> parameters) {
        if (parameters == null) {
            return;
        }

        for (Object param : parameters) {
            if (param instanceof String) {
                String strParam = (String) param;

                // 위험한 문자 체크
                if (containsDangerousKeywords(strParam)) {
                    throw new SecurityException("파라미터에 위험한 내용이 포함되어 있습니다");
                }

                // 길이 제한
                if (strParam.length() > 1000) {
                    throw new SecurityException("파라미터가 너무 깁니다");
                }
            }
        }
    }

    /**
     * 미리 정의된 안전한 쿼리 실행
     */
    @Tool(description = "미리 정의된 안전한 쿼리를 실행합니다")
    public DatabaseQueryResponse executePredefinedQuery(PredefinedQueryRequest request) {
        log.info("=== 미리 정의된 쿼리 실행 ===");
        log.info("쿼리 ID: {}", request.queryId());

        // 미리 정의된 안전한 쿼리 목록
        Map<String, String> predefinedQueries = Map.of(
            "GET_PRODUCTS", "SELECT * FROM products WHERE category = ? LIMIT 100",
            "GET_ORDERS", "SELECT * FROM orders WHERE customer_id = ? AND created_at > ? LIMIT 100",
            "GET_CUSTOMERS", "SELECT id, name, email FROM customers WHERE status = ? LIMIT 100",
            "GET_RECENT_ORDERS", "SELECT * FROM orders ORDER BY created_at DESC LIMIT 50"
        );

        String queryTemplate = predefinedQueries.get(request.queryId());

        if (queryTemplate == null) {
            log.error("정의되지 않은 쿼리 ID: {}", request.queryId());
            return new DatabaseQueryResponse(
                false,
                "정의되지 않은 쿼리입니다.",
                null,
                0
            );
        }

        try {
            List<Map<String, Object>> results = request.parameters() != null && !request.parameters().isEmpty()
                ? jdbcTemplate.queryForList(queryTemplate, request.parameters().toArray())
                : jdbcTemplate.queryForList(queryTemplate);

            log.info("미리 정의된 쿼리 실행 성공: {} 건", results.size());

            return new DatabaseQueryResponse(
                true,
                "쿼리가 성공적으로 실행되었습니다.",
                results,
                results.size()
            );

        } catch (Exception e) {
            log.error("쿼리 실행 실패", e);
            return new DatabaseQueryResponse(
                false,
                "쿼리 실행 실패: " + e.getMessage(),
                null,
                0
            );
        }
    }

    /**
     * 쿼리 보안 검증만 수행 (실행 없이)
     */
    @Tool(description = "쿼리의 보안성을 검증합니다 (실행하지 않음)")
    public QueryValidationResponse validateQuery(DatabaseQueryRequest request) {
        log.info("=== 쿼리 보안 검증 ===");

        List<String> issues = new ArrayList<>();

        try {
            validateQueryBasics(request.query());
        } catch (SecurityException e) {
            issues.add("기본 검증 실패: " + e.getMessage());
        }

        if (containsDangerousKeywords(request.query())) {
            issues.add("위험한 키워드 포함");
        }

        if (!request.query().trim().toUpperCase().startsWith("SELECT")) {
            issues.add("SELECT가 아닌 쿼리");
        }

        if (!isAllowedTable(request.query())) {
            issues.add("허용되지 않은 테이블 접근");
        }

        boolean isValid = issues.isEmpty();

        return new QueryValidationResponse(
            isValid,
            isValid ? "쿼리가 안전합니다" : "보안 문제가 발견되었습니다",
            issues
        );
    }
}