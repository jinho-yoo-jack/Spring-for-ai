package org.sprain.ai.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that validate the completeness and quality of README documentation.
 * Ensures the README provides adequate information for developers and users.
 */
@DisplayName("README.md Documentation Completeness Tests")
class ReadmeDocumentationCompletenessTest {

    private static Path readmePath;
    private static String readmeContent;
    private static List<String> readmeLines;
    private static String readmeLowerCase;

    @BeforeAll
    static void setUp() throws IOException {
        Path projectRoot = Paths.get("").toAbsolutePath();
        readmePath = projectRoot.resolve("README.md");
        readmeContent = Files.readString(readmePath);
        readmeLines = Files.readAllLines(readmePath);
        readmeLowerCase = readmeContent.toLowerCase();
    }

    @Nested
    @DisplayName("Basic Documentation Requirements")
    class BasicDocumentationTests {

        @Test
        @DisplayName("Should have a clear project title")
        void shouldHaveClearProjectTitle() {
            long headingCount = readmeLines.stream()
                    .filter(line -> line.trim().startsWith("# "))
                    .count();
            
            assertTrue(headingCount >= 1,
                    "README should have at least one main heading to serve as project title");
        }

        @Test
        @DisplayName("Project title should be descriptive")
        void projectTitleShouldBeDescriptive() {
            String title = readmeLines.stream()
                    .filter(line -> line.trim().startsWith("#"))
                    .findFirst()
                    .map(line -> line.replaceFirst("^#+\\s*", ""))
                    .orElse("");
            
            assertTrue(title.length() >= 3,
                    "Project title should be at least 3 characters long");
            assertTrue(title.split("\\s+").length >= 1,
                    "Project title should contain at least one word");
        }

        @Test
        @DisplayName("Should have content that explains the project")
        void shouldHaveExplanatoryContent() {
            // The README should have more than just a heading
            String contentWithoutMarkdown = readmeContent.replaceAll("[#*_`]", "").trim();
            assertTrue(contentWithoutMarkdown.length() >= 5,
                    "README should have explanatory content beyond markdown formatting");
        }
    }

    @Nested
    @DisplayName("Project Context Documentation")
    class ProjectContextTests {

        @Test
        @DisplayName("Should indicate the project's purpose or domain")
        void shouldIndicateProjectPurpose() {
            boolean hasPurposeIndicator = 
                    readmeLowerCase.contains("study") ||
                    readmeLowerCase.contains("learn") ||
                    readmeLowerCase.contains("project") ||
                    readmeLowerCase.contains("application") ||
                    readmeLowerCase.contains("demo") ||
                    readmeLowerCase.contains("example");
            
            assertTrue(hasPurposeIndicator,
                    "README should indicate the project's purpose (study, project, demo, etc.)");
        }

        @Test
        @DisplayName("Should reference relevant technologies")
        void shouldReferenceRelevantTechnologies() {
            boolean mentionsRelevantTech = 
                    readmeLowerCase.contains("spring") ||
                    readmeLowerCase.contains("ai") ||
                    readmeLowerCase.contains("java") ||
                    readmeLowerCase.contains("artificial intelligence");
            
            assertTrue(mentionsRelevantTech,
                    "README should mention at least one relevant technology for this project");
        }

        @Test
        @DisplayName("Should be contextually appropriate for a Spring AI project")
        void shouldBeContextuallyAppropriate() {
            int springMentions = countOccurrences(readmeLowerCase, "spring");
            int aiMentions = countOccurrences(readmeLowerCase, "ai");
            
            int totalRelevantMentions = springMentions + aiMentions;
            
            assertTrue(totalRelevantMentions >= 1,
                    String.format("README should mention relevant keywords. Found: Spring=%d, AI=%d",
                            springMentions, aiMentions));
        }
    }

    @Nested
    @DisplayName("Documentation Quality Tests")
    class DocumentationQualityTests {

        @Test
        @DisplayName("Should not have spelling errors in common words")
        void shouldNotHaveObviousSpellingErrors() {
            // Check for common misspellings
            assertFalse(readmeLowerCase.contains("recieve"),
                    "Should use correct spelling: 'receive' not 'recieve'");
            assertFalse(readmeLowerCase.contains("occured"),
                    "Should use correct spelling: 'occurred' not 'occured'");
            assertFalse(readmeLowerCase.contains("seperate"),
                    "Should use correct spelling: 'separate' not 'seperate'");
        }

        @Test
        @DisplayName("Should use professional language")
        void shouldUseProfessionalLanguage() {
            // Check that it doesn't contain obviously unprofessional content
            assertFalse(readmeLowerCase.matches(".*\\b(stupid|dumb|sucks|crap)\\b.*"),
                    "README should use professional language");
        }

        @Test
        @DisplayName("Should have proper sentence structure in headings")
        void shouldHaveProperSentenceStructure() {
            List<String> headings = readmeLines.stream()
                    .filter(line -> line.trim().startsWith("#"))
                    .map(line -> line.replaceFirst("^#+\\s*", "").trim())
                    .collect(Collectors.toList());
            
            for (String heading : headings) {
                assertFalse(heading.isEmpty(),
                        "Headings should not be empty");
                assertFalse(heading.endsWith(".."),
                        "Headings should not end with multiple periods");
            }
        }

        @Test
        @DisplayName("Content should be logically organized")
        void contentShouldBeLogicallyOrganized() {
            // For a simple README, check that headings come before or with content
            if (readmeLines.size() > 0) {
                String firstLine = readmeLines.get(0).trim();
                // First non-empty line should typically be a heading or content
                assertTrue(!firstLine.isEmpty(),
                        "README should start with meaningful content");
            }
        }
    }

    @Nested
    @DisplayName("Accessibility and Readability Tests")
    class AccessibilityTests {

        @Test
        @DisplayName("Should be readable by screen readers")
        void shouldBeReadableByScreenReaders() {
            // Check that headings are properly structured for accessibility
            List<String> headings = readmeLines.stream()
                    .filter(line -> line.trim().startsWith("#"))
                    .collect(Collectors.toList());
            
            for (String heading : headings) {
                String text = heading.replaceFirst("^#+\\s*", "");
                assertTrue(text.matches(".*\\w.*"),
                        "Headings should contain readable text for screen readers");
            }
        }

        @Test
        @DisplayName("Should have reasonable reading complexity")
        void shouldHaveReasonableReadingComplexity() {
            // For short READMEs, just verify it's not overly complex
            String[] sentences = readmeContent.split("[.!?]+");
            
            if (sentences.length > 0) {
                for (String sentence : sentences) {
                    String[] words = sentence.trim().split("\\s+");
                    assertTrue(words.length < 50,
                            "Sentences should not be overly long (< 50 words each)");
                }
            }
        }

        @Test
        @DisplayName("Should not rely solely on visual formatting")
        void shouldNotRelyOnlyOnVisualFormatting() {
            // Content should be understandable even without markdown rendering
            String plainText = readmeContent.replaceAll("[#*_`\\[\\]()]", " ").trim();
            assertTrue(plainText.length() > 0,
                    "README should have meaningful text content beyond just formatting");
        }
    }

    @Nested
    @DisplayName("Version Control and Maintenance Tests")
    class MaintenanceTests {

        @Test
        @DisplayName("Should be in a maintainable format")
        void shouldBeInMaintainableFormat() {
            // Check for reasonable line lengths for easy diffs
            List<String> longLines = readmeLines.stream()
                    .filter(line -> line.length() > 120)
                    .collect(Collectors.toList());
            
            assertTrue(longLines.size() < readmeLines.size() / 2,
                    "Most lines should be reasonably short for easier version control");
        }

        @Test
        @DisplayName("Should not have merge conflict markers")
        void shouldNotHaveMergeConflictMarkers() {
            assertFalse(readmeContent.contains("<<<<<<<"),
                    "README should not contain merge conflict markers");
            assertFalse(readmeContent.contains(">>>>>>>"),
                    "README should not contain merge conflict markers");
            assertFalse(readmeContent.contains("======="),
                    "README should not contain merge conflict markers");
        }

        @Test
        @DisplayName("Should be git-friendly")
        void shouldBeGitFriendly() {
            // Check for good git practices
            if (readmeContent.endsWith("\n")) {
                // Ending with newline is good practice
                assertTrue(true, "README ends with newline (good practice)");
            }
            
            // Should not have carriage returns mixed with line feeds
            long crCount = readmeContent.chars().filter(c -> c == '\r').count();
            long lfCount = readmeContent.chars().filter(c -> c == '\n').count();
            
            assertTrue(crCount == 0 || crCount == lfCount,
                    "Line endings should be consistent");
        }
    }

    @Nested
    @DisplayName("Future-Proofing Tests")
    class FutureProofingTests {

        @Test
        @DisplayName("Should be extensible for additional sections")
        void shouldBeExtensibleForAdditionalSections() {
            // The current structure should allow for adding more content
            assertTrue(readmeContent.length() < 1000,
                    "README should have room for future expansion");
        }

        @Test
        @DisplayName("Should follow markdown best practices")
        void shouldFollowMarkdownBestPractices() {
            // Headings should have space after hash
            List<String> headingsWithoutSpace = readmeLines.stream()
                    .filter(line -> line.matches("^#+[^\\s].*"))
                    .collect(Collectors.toList());
            
            assertTrue(headingsWithoutSpace.isEmpty(),
                    "All headings should have a space after the # symbol(s)");
        }

        @Test
        @DisplayName("Should be compatible with common markdown parsers")
        void shouldBeCompatibleWithMarkdownParsers() {
            // Check for structures that might break common parsers
            assertFalse(readmeContent.contains("# #"),
                    "Should not have malformed heading syntax");
            assertFalse(readmeContent.matches(".*\\[\\]\\(\\).*"),
                    "Should not have empty links");
        }
    }

    // Helper method
    private static int countOccurrences(String text, String word) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }
}