import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FileWriterUtil class.
 */
public class FileWriterUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteToFile() throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("test.txt");
        String content = "Test content";

        // Act
        FileWriterUtil.writeToFile(tempFile.toString(), content);

        // Assert
        assertTrue(Files.exists(tempFile), "File should exist");
        String readContent = Files.readString(tempFile);
        assertEquals(content, readContent, "File content should match");
    }

    @Test
    void testWriteJsonToFile() throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("test.json");
        String jsonContent = "{\"key\": \"value\"}";

        // Act
        FileWriterUtil.writeJsonToFile(tempFile.toString(), jsonContent);

        // Assert
        assertTrue(Files.exists(tempFile), "JSON file should exist");
        String readContent = Files.readString(tempFile);
        assertEquals(jsonContent, readContent, "JSON content should match");
    }
}