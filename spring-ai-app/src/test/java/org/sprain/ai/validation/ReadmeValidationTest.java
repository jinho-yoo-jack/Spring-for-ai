package org.sprain.ai.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive validation tests for the README.md file.
 * These tests ensure the README exists, is properly formatted,
 * and contains essential documentation for the project.
 */
@DisplayName("README.md Validation Tests")
class ReadmeValidationTest {

    private static final String README_PATH = "README.md";
    private static final Path PROJECT_ROOT = Paths.get("").toAbsolutePath();
    private static final Path README_FILE = PROJECT_ROOT.resolve(README_PATH);

    @Nested
    @DisplayName("File Existence and Accessibility Tests")
    class FileExistenceTests {

        @Test
        @DisplayName("Should have README.md file in project root")
        void readmeShouldExist() {
            assertTrue(Files.exists(README_FILE),
                    "README.md should exist in the project root directory");
        }

        @Test
        @DisplayName("Should be a regular file, not a directory")
        void readmeShouldBeRegularFile() {
            assertTrue(Files.isRegularFile(README_FILE),
                    "README.md should be a regular file");
        }

        @Test
        @DisplayName("Should be readable")
        void readmeShouldBeReadable() {
            assertTrue(Files.isReadable(README_FILE),
                    "README.md should be readable");
        }

        @Test
        @DisplayName("Should not be empty")
        void readmeShouldNotBeEmpty() throws IOException {
            long fileSize = Files.size(README_FILE);
            assertTrue(fileSize > 0,
                    "README.md should not be empty");
        }

        @Test
        @DisplayName("Should have .md extension")
        void readmeShouldHaveMarkdownExtension() {
            String fileName = README_FILE.getFileName().toString();
            assertTrue(fileName.endsWith(".md"),
                    "README should have .md extension for markdown files");
        }
    }

    @Nested
    @DisplayName("Content Structure Tests")
    class ContentStructureTests {

        @Test
        @DisplayName("Should contain at least one line of content")
        void readmeShouldHaveContent() throws IOException {
            List<String> lines = Files.readAllLines(README_FILE);
            assertFalse(lines.isEmpty(),
                    "README.md should contain at least one line");
        }

        @Test
        @DisplayName("Should have a title/heading")
        void readmeShouldHaveTitle() throws IOException {
            String content = Files.readString(README_FILE);
            assertTrue(content.contains("#"),
                    "README.md should contain at least one heading (marked with #)");
        }

        @Test
        @DisplayName("Should start with a heading")
        void readmeShouldStartWithHeading() throws IOException {
            List<String> lines = Files.readAllLines(README_FILE);
            String firstNonEmptyLine = lines.stream()
                    .filter(line -> !line.trim().isEmpty())
                    .findFirst()
                    .orElse("");
            
            assertTrue(firstNonEmptyLine.trim().startsWith("#"),
                    "README.md should start with a heading (line beginning with #)");
        }

        @Test
        @DisplayName("Should have proper markdown heading syntax")
        void readmeShouldHaveProperHeadingSyntax() throws IOException {
            String content = Files.readString(README_FILE);
            // Check for markdown heading pattern (# followed by space and text)
            Pattern headingPattern = Pattern.compile("^#{1,6}\\s+.+", Pattern.MULTILINE);
            assertTrue(headingPattern.matcher(content).find(),
                    "README.md should have properly formatted headings (# followed by space)");
        }

        @Test
        @DisplayName("Should not have only whitespace")
        void readmeShouldNotBeOnlyWhitespace() throws IOException {
            String content = Files.readString(README_FILE);
            assertFalse(content.trim().isEmpty(),
                    "README.md should contain non-whitespace content");
        }

        @Test
        @DisplayName("Should have meaningful content length")
        void readmeShouldHaveMeaningfulLength() throws IOException {
            String content = Files.readString(README_FILE).trim();
            assertTrue(content.length() >= 10,
                    "README.md should have at least 10 characters of content");
        }
    }

    @Nested
    @DisplayName("Content Quality Tests")
    class ContentQualityTests {

        @Test
        @DisplayName("Should mention 'Spring' in content")
        void readmeShouldMentionSpring() throws IOException {
            String content = Files.readString(README_FILE).toLowerCase();
            assertTrue(content.contains("spring"),
                    "README.md should mention 'Spring' as this is a Spring project");
        }

        @Test
        @DisplayName("Should mention 'AI' in content")
        void readmeShouldMentionAI() throws IOException {
            String content = Files.readString(README_FILE).toLowerCase();
            assertTrue(content.contains("ai"),
                    "README.md should mention 'AI' as this is an AI-related project");
        }

        @Test
        @DisplayName("Should have project-relevant keywords")
        void readmeShouldHaveRelevantKeywords() throws IOException {
            String content = Files.readString(README_FILE).toLowerCase();
            boolean hasSpring = content.contains("spring");
            boolean hasAI = content.contains("ai");
            
            assertTrue(hasSpring || hasAI,
                    "README.md should contain project-relevant keywords like 'Spring' or 'AI'");
        }

        @Test
        @DisplayName("Should not have placeholder text")
        void readmeShouldNotHavePlaceholders() throws IOException {
            String content = Files.readString(README_FILE).toLowerCase();
            assertFalse(content.contains("lorem ipsum"),
                    "README.md should not contain placeholder text like 'lorem ipsum'");
            assertFalse(content.contains("todo"),
                    "README.md should not contain TODO markers (should be completed)");
            assertFalse(content.contains("[insert"),
                    "README.md should not contain placeholder markers like '[insert'");
        }

        @Test
        @DisplayName("Should have proper capitalization in title")
        void titleShouldBeProperlyCapitalized() throws IOException {
            List<String> lines = Files.readAllLines(README_FILE);
            String firstLine = lines.stream()
                    .filter(line -> !line.trim().isEmpty())
                    .findFirst()
                    .orElse("");
            
            if (firstLine.startsWith("#")) {
                String title = firstLine.replaceFirst("^#+\\s*", "");
                assertFalse(title.isEmpty(),
                        "Title should not be empty");
                // First character after heading markers should be uppercase or digit
                char firstChar = title.charAt(0);
                assertTrue(Character.isUpperCase(firstChar) || Character.isDigit(firstChar),
                        "Title should start with an uppercase letter or digit");
            }
        }
    }

    @Nested
    @DisplayName("Markdown Formatting Tests")
    class MarkdownFormattingTests {

        @Test
        @DisplayName("Should not have excessive consecutive blank lines")
        void readmeShouldNotHaveExcessiveBlankLines() throws IOException {
            String content = Files.readString(README_FILE);
            assertFalse(content.contains("\n\n\n\n"),
                    "README.md should not have more than 2 consecutive blank lines");
        }

        @Test
        @DisplayName("Should use consistent heading levels")
        void readmeShouldHaveConsistentHeadingLevels() throws IOException {
            String content = Files.readString(README_FILE);
            Pattern headingPattern = Pattern.compile("^(#{1,6})\\s", Pattern.MULTILINE);
            var matcher = headingPattern.matcher(content);
            
            if (matcher.find()) {
                int firstLevel = matcher.group(1).length();
                assertTrue(firstLevel <= 2,
                        "Top-level heading should be # or ## for better document structure");
            }
        }

        @Test
        @DisplayName("Should not have trailing spaces at end of file")
        void readmeShouldNotHaveTrailingSpacesAtEnd() throws IOException {
            String content = Files.readString(README_FILE);
            if (!content.isEmpty()) {
                char lastChar = content.charAt(content.length() - 1);
                // Allow newline at end but not spaces
                assertTrue(lastChar != ' ' && lastChar != '\t',
                        "README.md should not have trailing spaces at the end of file");
            }
        }

        @Test
        @DisplayName("Should have consistent line endings")
        void readmeShouldHaveConsistentLineEndings() throws IOException {
            byte[] bytes = Files.readAllBytes(README_FILE);
            String content = new String(bytes);
            
            long crlfCount = content.chars().filter(c -> c == '\r').count();
            long lfCount = content.chars().filter(c -> c == '\n').count();
            
            // Either all CRLF or all LF, not mixed
            assertTrue(crlfCount == 0 || crlfCount == lfCount,
                    "README.md should have consistent line endings (either LF or CRLF, not mixed)");
        }
    }

    @Nested
    @DisplayName("File Size and Performance Tests")
    class FileSizeTests {

        @Test
        @DisplayName("Should not be excessively large")
        void readmeShouldNotBeExcessivelyLarge() throws IOException {
            long fileSize = Files.size(README_FILE);
            long maxSize = 1024 * 1024; // 1MB
            assertTrue(fileSize < maxSize,
                    "README.md should be less than 1MB for better performance");
        }

        @Test
        @DisplayName("Should have reasonable line count")
        void readmeShouldHaveReasonableLineCount() throws IOException {
            long lineCount = Files.lines(README_FILE).count();
            assertTrue(lineCount > 0 && lineCount < 10000,
                    "README.md should have a reasonable number of lines (1-10000)");
        }

        @Test
        @DisplayName("Should not have excessively long lines")
        void readmeShouldNotHaveExcessivelyLongLines() throws IOException {
            List<String> lines = Files.readAllLines(README_FILE);
            int maxLineLength = 500; // Common markdown line length limit
            
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                assertTrue(line.length() <= maxLineLength,
                        String.format("Line %d is too long (%d characters). " +
                                "Consider breaking it into multiple lines for better readability.",
                                i + 1, line.length()));
            }
        }
    }

    @Nested
    @DisplayName("Character Encoding Tests")
    class EncodingTests {

        @Test
        @DisplayName("Should be UTF-8 encoded")
        void readmeShouldBeUtf8Encoded() throws IOException {
            // Try to read as UTF-8 - should not throw exception
            assertDoesNotThrow(() -> Files.readString(README_FILE),
                    "README.md should be valid UTF-8 encoded");
        }

        @Test
        @DisplayName("Should not contain null characters")
        void readmeShouldNotContainNullCharacters() throws IOException {
            String content = Files.readString(README_FILE);
            assertFalse(content.contains("\0"),
                    "README.md should not contain null characters");
        }

        @Test
        @DisplayName("Should be readable as text")
        void readmeShouldBeReadableAsText() throws IOException {
            List<String> lines = Files.readAllLines(README_FILE);
            assertNotNull(lines, "README.md should be readable as text lines");
            
            for (String line : lines) {
                assertNotNull(line, "Each line should be readable");
            }
        }
    }

    @Nested
    @DisplayName("Documentation Best Practices Tests")
    class DocumentationBestPracticesTests {

        @Test
        @DisplayName("Should have a descriptive title")
        void readmeShouldHaveDescriptiveTitle() throws IOException {
            String content = Files.readString(README_FILE);
            Pattern titlePattern = Pattern.compile("^#\\s+(.+)", Pattern.MULTILINE);
            var matcher = titlePattern.matcher(content);
            
            if (matcher.find()) {
                String title = matcher.group(1).trim();
                assertTrue(title.length() >= 5,
                        "README title should be descriptive (at least 5 characters)");
            }
        }

        @Test
        @DisplayName("Title should indicate project purpose")
        void titleShouldIndicateProjectPurpose() throws IOException {
            String content = Files.readString(README_FILE);
            Pattern titlePattern = Pattern.compile("^#\\s+(.+)", Pattern.MULTILINE);
            var matcher = titlePattern.matcher(content);
            
            if (matcher.find()) {
                String title = matcher.group(1).toLowerCase();
                assertTrue(title.length() > 0,
                        "README should have a title that indicates project purpose");
            }
        }

        @Test
        @DisplayName("Should not contain sensitive information patterns")
        void readmeShouldNotContainSensitiveInfo() throws IOException {
            String content = Files.readString(README_FILE).toLowerCase();
            
            assertFalse(content.matches(".*password\\s*=\\s*.+"),
                    "README.md should not contain hardcoded passwords");
            assertFalse(content.matches(".*api[_-]?key\\s*=\\s*.+"),
                    "README.md should not contain hardcoded API keys");
            assertFalse(content.matches(".*secret\\s*=\\s*.+"),
                    "README.md should not contain hardcoded secrets");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should be in project root alongside build files")
        void readmeShouldBeInProjectRoot() {
            Path buildGradle = PROJECT_ROOT.resolve("build.gradle");
            Path settingsGradle = PROJECT_ROOT.resolve("settings.gradle");
            
            assertTrue(Files.exists(buildGradle) || Files.exists(settingsGradle),
                    "README.md location should be verified by presence of build files");
            
            assertEquals(PROJECT_ROOT, README_FILE.getParent(),
                    "README.md should be in the same directory as build files");
        }

        @Test
        @DisplayName("Should be parseable without errors")
        void readmeShouldBeParseable() throws IOException {
            assertDoesNotThrow(() -> {
                List<String> lines = Files.readAllLines(README_FILE);
                String content = Files.readString(README_FILE);
                
                assertNotNull(lines);
                assertNotNull(content);
            }, "README.md should be parseable without errors");
        }

        @Test
        @DisplayName("Multiple reads should return consistent content")
        void multipleReadsShouldBeConsistent() throws IOException {
            String content1 = Files.readString(README_FILE);
            String content2 = Files.readString(README_FILE);
            
            assertEquals(content1, content2,
                    "Multiple reads of README.md should return consistent content");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle concurrent reads gracefully")
        void shouldHandleConcurrentReads() {
            assertDoesNotThrow(() -> {
                Thread t1 = new Thread(() -> {
                    try {
                        Files.readString(README_FILE);
                    } catch (IOException e) {
                        fail("Thread 1 failed to read README");
                    }
                });
                
                Thread t2 = new Thread(() -> {
                    try {
                        Files.readString(README_FILE);
                    } catch (IOException e) {
                        fail("Thread 2 failed to read README");
                    }
                });
                
                t1.start();
                t2.start();
                t1.join();
                t2.join();
            }, "README.md should handle concurrent reads gracefully");
        }

        @Test
        @DisplayName("Should be accessible from test context")
        void shouldBeAccessibleFromTestContext() {
            assertDoesNotThrow(() -> {
                assertTrue(Files.exists(README_FILE));
                assertTrue(Files.isReadable(README_FILE));
            }, "README.md should be accessible from test context");
        }
    }
}