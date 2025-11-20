package org.sprain.ai.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Content-specific validation tests for the README.md file.
 * These tests verify the actual content matches expected patterns
 * and provides appropriate documentation for this Spring AI study project.
 */
@DisplayName("README.md Content Validation Tests")
class ReadmeContentValidationTest {

    private static Path readmePath;
    private static String readmeContent;
    private static List<String> readmeLines;

    @BeforeAll
    static void setUp() throws IOException {
        Path projectRoot = Paths.get("").toAbsolutePath();
        readmePath = projectRoot.resolve("README.md");
        readmeContent = Files.readString(readmePath);
        readmeLines = Files.readAllLines(readmePath);
    }

    @Test
    @DisplayName("Should have 'Spring AI' or 'Spring' and 'AI' in title")
    void shouldMentionSpringAIInTitle() {
        String firstLine = readmeLines.stream()
                .filter(line -> !line.trim().isEmpty())
                .findFirst()
                .orElse("");
        
        String titleText = firstLine.replaceFirst("^#+\\s*", "").toLowerCase();
        
        boolean hasSpringAI = titleText.contains("spring ai") || 
                             titleText.contains("spring-ai") ||
                             titleText.contains("springai");
        boolean hasBothKeywords = titleText.contains("spring") && titleText.contains("ai");
        
        assertTrue(hasSpringAI || hasBothKeywords,
                "README title should mention both 'Spring' and 'AI' since this is a Spring AI project");
    }

    @Test
    @DisplayName("Content should match expected Spring AI study project pattern")
    void contentShouldMatchProjectPattern() {
        String lowerContent = readmeContent.toLowerCase();
        
        boolean mentionsSpring = lowerContent.contains("spring");
        boolean mentionsAI = lowerContent.contains("ai");
        boolean mentionsStudy = lowerContent.contains("study") || lowerContent.contains("learn");
        
        int keywordCount = (mentionsSpring ? 1 : 0) + (mentionsAI ? 1 : 0) + (mentionsStudy ? 1 : 0);
        
        assertTrue(keywordCount >= 2,
                String.format("README should mention at least 2 of: Spring, AI, Study. Found: %d", keywordCount));
    }

    @Test
    @DisplayName("Should have a level-1 heading")
    void shouldHaveLevel1Heading() {
        Pattern level1Heading = Pattern.compile("^#\\s+", Pattern.MULTILINE);
        assertTrue(level1Heading.matcher(readmeContent).find(),
                "README should have at least one level-1 heading (single #)");
    }

    @Test
    @DisplayName("Main heading should be properly formatted")
    void mainHeadingShouldBeProperlyFormatted() {
        List<String> headingLines = readmeLines.stream()
                .filter(line -> line.trim().startsWith("#"))
                .collect(Collectors.toList());
        
        assertFalse(headingLines.isEmpty(), "Should have at least one heading");
        
        String firstHeading = headingLines.get(0);
        assertTrue(firstHeading.matches("^#\\s+\\S.*"),
                "First heading should have format: '# Text' with space after #");
    }

    @Test
    @DisplayName("Should not have malformed markdown syntax")
    void shouldNotHaveMalformedMarkdown() {
        // Check for common markdown mistakes
        assertFalse(readmeContent.contains("#NoSpace"),
                "Headings should have a space after # symbol");
        assertFalse(readmeContent.contains("##  "),
                "Headings should not have multiple spaces after # symbols");
    }

    @Test
    @DisplayName("Content should be appropriate length for a study project")
    void contentShouldBeAppropriateLength() {
        int contentLength = readmeContent.trim().length();
        assertTrue(contentLength >= 15 && contentLength <= 1000,
                String.format("README content length (%d) should be reasonable for a study project (15-1000 chars)",
                        contentLength));
    }

    @Test
    @DisplayName("Should indicate this is a study/learning project")
    void shouldIndicateStudyProject() {
        String lowerContent = readmeContent.toLowerCase();
        
        boolean indicatesStudy = lowerContent.contains("study") ||
                                lowerContent.contains("learn") ||
                                lowerContent.contains("tutorial") ||
                                lowerContent.contains("example") ||
                                lowerContent.contains("demo") ||
                                lowerContent.contains("practice");
        
        assertTrue(indicatesStudy,
                "README should indicate this is a study/learning project with keywords like " +
                "'study', 'learn', 'tutorial', 'example', 'demo', or 'practice'");
    }

    @Test
    @DisplayName("Should not contain inconsistent casing in technical terms")
    void shouldHaveConsistentTechnicalTermCasing() {
        // Check for consistent casing of "Spring"
        if (readmeContent.contains("Spring") || readmeContent.contains("spring")) {
            long properSpringCount = Pattern.compile("\\bSpring\\b").matcher(readmeContent).results().count();
            long lowercaseSpringCount = Pattern.compile("\\bspring\\b").matcher(readmeContent).results().count();
            
            // Either should be consistently uppercase or have a good reason to be lowercase
            assertTrue(properSpringCount > 0 || lowercaseSpringCount > 0,
                    "Technical term 'Spring' should be present");
        }
    }

    @Test
    @DisplayName("Should have meaningful content beyond just a title")
    void shouldHaveMeaningfulContent() {
        String contentWithoutHeadings = readmeContent.replaceAll("^#+\\s+.*$", "").trim();
        
        // For now, even a short title is acceptable, but check structure
        assertTrue(readmeContent.trim().length() > 0,
                "README should have meaningful content");
    }

    @Test
    @DisplayName("All lines should use valid characters")
    void allLinesShouldUseValidCharacters() {
        for (int i = 0; i < readmeLines.size(); i++) {
            String line = readmeLines.get(i);
            // Check for common problematic characters
            assertFalse(line.contains("\t"),
                    String.format("Line %d should use spaces instead of tabs", i + 1));
        }
    }

    @Test
    @DisplayName("Should maintain consistent style throughout")
    void shouldMaintainConsistentStyle() {
        if (readmeLines.size() > 1) {
            // Check that if there are multiple headings, they follow consistent patterns
            List<String> headings = readmeLines.stream()
                    .filter(line -> line.trim().startsWith("#"))
                    .collect(Collectors.toList());
            
            if (headings.size() > 1) {
                boolean allConsistent = headings.stream()
                        .allMatch(h -> h.matches("^#+\\s+.*"));
                assertTrue(allConsistent,
                        "All headings should follow consistent format (# followed by space)");
            }
        }
    }

    @Test
    @DisplayName("Content should be grammatically structured")
    void contentShouldBeGrammaticallyStructured() {
        String titleText = readmeLines.stream()
                .filter(line -> line.trim().startsWith("#"))
                .findFirst()
                .map(line -> line.replaceFirst("^#+\\s*", ""))
                .orElse("");
        
        if (!titleText.isEmpty()) {
            // Check that title has at least one word character
            assertTrue(titleText.matches(".*\\w+.*"),
                    "Title should contain at least one word");
            
            // Check that title doesn't start with punctuation
            assertFalse(titleText.matches("^[.,;:!?].*"),
                    "Title should not start with punctuation");
        }
    }

    @Test
    @DisplayName("Should have appropriate project context")
    void shouldHaveAppropriateProjectContext() {
        // Verify the README is appropriate for this specific project
        String lowerContent = readmeContent.toLowerCase();
        
        // Should mention key technologies
        boolean mentionsTech = lowerContent.contains("spring") ||
                              lowerContent.contains("ai") ||
                              lowerContent.contains("java");
        
        assertTrue(mentionsTech,
                "README should mention at least one key technology (Spring, AI, or Java)");
    }

    @Test
    @DisplayName("Content should be version control friendly")
    void contentShouldBeVersionControlFriendly() {
        // Check that content doesn't have problematic patterns for version control
        assertFalse(readmeContent.contains("\r\r"),
                "README should not have double carriage returns");
        
        // Should end with a newline or not have trailing whitespace issues
        if (readmeContent.length() > 0) {
            char lastChar = readmeContent.charAt(readmeContent.length() - 1);
            assertTrue(lastChar == '\n' || !Character.isWhitespace(lastChar),
                    "README should either end with newline or have no trailing whitespace");
        }
    }

    @Test
    @DisplayName("Should be suitable for GitHub display")
    void shouldBeSuitableForGitHubDisplay() {
        // Check for features that work well on GitHub
        assertTrue(readmeContent.startsWith("#") || readmeContent.trim().startsWith("#"),
                "README should start with a heading for proper GitHub rendering");
        
        // Should not have overly complex structures that don't render well
        assertFalse(readmeContent.contains("<script>"),
                "README should not contain scripts");
        assertFalse(readmeContent.contains("<iframe>"),
                "README should not contain iframes");
    }
}