package org.spring.ai.prompt;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

/**
 * Prompt Templates - Spring AI 1.1.0-M2
 * 파라미터가 없는 단순 프롬프트
 */
@Slf4j
@Component
public class SimplePromptTemplates {

    /**
     * ✅ Java 코드 리뷰 프롬프트
     */
    @McpPrompt(
        name = "java_code_review",
        description = "Java 코드를 리뷰하고 개선 사항을 제안합니다"
    )
    public String javaCodeReviewPrompt() {
        return """
            당신은 시니어 Java 개발자입니다.
            제공된 Java 코드를 리뷰하고 개선 사항을 제안해주세요.
            
            다음 형식으로 리뷰해주세요:
            
            ## 전체 평가
            - 점수: [1-10점]
            - 총평: [한 줄 요약]
            
            ## 주요 발견사항
            1. [발견사항 1]
            2. [발견사항 2]
            3. [발견사항 3]
            
            ## 구체적인 개선 제안
            ### 성능
            - [제안]
            
            ### 보안
            - [제안]
            
            ### 가독성
            - [제안]
            
            ## 코드 스멜
            [발견된 코드 스멜]
            
            ## 리팩토링 제안
            [구체적인 리팩토링 방법]
            """;
    }

    /**
     * ✅ Python 코드 리뷰 프롬프트
     */
    @McpPrompt(
        name = "python_code_review",
        description = "Python 코드를 리뷰하고 PEP 8 준수 여부를 확인합니다"
    )
    public String pythonCodeReviewPrompt() {
        return """
            당신은 시니어 Python 개발자입니다.
            제공된 Python 코드를 리뷰해주세요.
            
            ## 리뷰 체크리스트
            - PEP 8 스타일 가이드 준수
            - Type hints 사용
            - Docstring 작성
            - 예외 처리
            - 테스트 가능성
            
            ## 리뷰 결과
            [여기에 리뷰 내용을 작성하세요]
            """;
    }

    /**
     * ✅ 짧은 요약 프롬프트
     */
    @McpPrompt(
        name = "short_summary",
        description = "문서를 3문장으로 요약합니다"
    )
    public String shortSummaryPrompt() {
        return """
            제공된 문서를 정확히 3문장으로 요약해주세요.
            
            요구사항:
            - 핵심 내용만 포함
            - 명확하고 간결한 문장
            - 각 문장은 독립적으로 이해 가능
            
            요약:
            1. [첫 번째 문장]
            2. [두 번째 문장]
            3. [세 번째 문장]
            """;
    }

    /**
     * ✅ 상세 요약 프롬프트
     */
    @McpPrompt(
        name = "detailed_summary",
        description = "문서를 상세하게 요약합니다"
    )
    public String detailedSummaryPrompt() {
        return """
            제공된 문서를 여러 단락으로 상세하게 요약해주세요.
            
            ## 요약 구조
            
            ### 개요
            [문서의 전반적인 내용]
            
            ### 주요 내용
            [핵심 포인트를 단락으로 설명]
            
            ### 세부사항
            [중요한 세부 내용]
            
            ### 결론
            [요약 및 시사점]
            """;
    }

    /**
     * ✅ 한영 번역 프롬프트
     */
    @McpPrompt(
        name = "translate_ko_to_en",
        description = "한국어를 영어로 번역합니다"
    )
    public String translateKoToEnPrompt() {
        return """
            제공된 한국어 텍스트를 자연스러운 영어로 번역해주세요.
            
            번역 가이드라인:
            - 격식있는 비즈니스 문체 사용
            - 문맥에 맞는 자연스러운 표현
            - 전문 용어는 정확하게 번역
            
            원문:
            [한국어 텍스트]
            
            번역:
            [영어 번역]
            """;
    }

    /**
     * ✅ 영한 번역 프롬프트
     */
    @McpPrompt(
        name = "translate_en_to_ko",
        description = "영어를 한국어로 번역합니다"
    )
    public String translateEnToKoPrompt() {
        return """
            제공된 영어 텍스트를 자연스러운 한국어로 번역해주세요.
            
            번역 가이드라인:
            - 존댓말 사용
            - 한국어 어순에 맞게 자연스럽게
            - 전문 용어는 괄호 안에 영문 병기
            
            원문:
            [영어 텍스트]
            
            번역:
            [한국어 번역]
            """;
    }

    /**
     * ✅ SQL 쿼리 생성 프롬프트
     */
    @McpPrompt(
        name = "generate_sql",
        description = "자연어 요청을 SQL 쿼리로 변환합니다"
    )
    public String generateSqlPrompt() {
        return """
            자연어 요청을 MySQL SQL 쿼리로 변환해주세요.
            
            ## 규칙
            - SELECT만 허용
            - 파라미터화된 쿼리 사용
            - LIMIT 절 포함
            - 인덱스를 고려한 효율적인 쿼리
            
            ## 응답 형식
            
            ### SQL 쿼리
```sql
            [쿼리 작성]
```
            
            ### 설명
            [쿼리가 수행하는 작업]
            
            ### 주의사항
            [성능 관련 고려사항]
            """;
    }

    /**
     * ✅ 이메일 작성 - 회의 요청
     */
    @McpPrompt(
        name = "email_meeting_request",
        description = "회의 요청 이메일을 작성합니다"
    )
    public String emailMeetingRequestPrompt() {
        return """
            회의 요청 이메일을 작성해주세요.
            
            ## 이메일 형식
            
            **제목:**
            [명확한 회의 목적]
            
            **본문:**
            
            안녕하세요,
            
            [회의 필요성 및 목적]
            
            [회의 안건]
            - 안건 1
            - 안건 2
            - 안건 3
            
            [제안 일시]
            - 옵션 1: [날짜/시간]
            - 옵션 2: [날짜/시간]
            - 옵션 3: [날짜/시간]
            
            [예상 소요 시간]
            
            [참석자]
            
            [준비사항]
            
            감사합니다.
            """;
    }

    /**
     * ✅ 이메일 작성 - 감사 인사
     */
    @McpPrompt(
        name = "email_thank_you",
        description = "감사 인사 이메일을 작성합니다"
    )
    public String emailThankYouPrompt() {
        return """
            감사 인사 이메일을 작성해주세요.
            
            **제목:**
            [감사의 내용을 담은 제목]
            
            **본문:**
            
            안녕하세요,
            
            [감사 인사]
            
            [구체적으로 감사한 내용]
            
            [그로 인한 긍정적 영향]
            
            [향후 협력 희망]
            
            감사합니다.
            """;
    }

    /**
     * ✅ 데이터 트렌드 분석
     */
    @McpPrompt(
        name = "analyze_trend",
        description = "데이터의 트렌드를 분석합니다"
    )
    public String analyzeTrendPrompt() {
        return """
            제공된 데이터의 트렌드를 분석해주세요.
            
            ## 분석 항목
            
            ### 1. 전반적 트렌드
            [증가/감소/정체 등]
            
            ### 2. 주요 변화 시점
            [급격한 변화가 있었던 시점과 원인]
            
            ### 3. 패턴
            [반복되는 패턴이나 주기성]
            
            ### 4. 이상치
            [예상 범위를 벗어난 데이터]
            
            ### 5. 예측
            [향후 트렌드 예측]
            
            ### 6. 권장사항
            [비즈니스 액션 아이템]
            """;
    }

    /**
     * ✅ 버그 리포트 작성
     */
    @McpPrompt(
        name = "bug_report",
        description = "상세한 버그 리포트를 작성합니다"
    )
    public String bugReportPrompt() {
        return """
            제공된 정보를 바탕으로 버그 리포트를 작성해주세요.
            
            ## 버그 리포트
            
            ### 제목
            [버그를 한 문장으로 설명]
            
            ### 우선순위
            [ ] Critical
            [ ] High
            [ ] Medium
            [ ] Low
            
            ### 상세 설명
            [버그에 대한 자세한 설명]
            
            ### 재현 단계
            1. [단계 1]
            2. [단계 2]
            3. [단계 3]
            
            ### 예상 동작
            [정상 동작]
            
            ### 실제 동작
            [비정상 동작]
            
            ### 환경
            - OS:
            - Browser:
            - Version:
            
            ### 스크린샷/로그
            [첨부 자료]
            
            ### 해결 제안
            [가능한 해결 방법]
            """;
    }
}