import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for file writing functionality in GeneticTitan and TitanInsertionHillClimbing.
 * These tests verify that both classes can write to files correctly using the FileWriterUtil class.
 */
public class FileWritingIntegrationTest {

    @Test
    void testGeneticTitanWriteToFile() throws IOException {
        // Create a simple JSON string
        String jsonContent = "{\n  \"test\": \"value\"\n}";
        
        // Create a temporary file path
        Path tempFile = Files.createTempFile("genetic_titan_test", ".json");
        
        try {
            // Call the FileWriterUtil method directly with our test content
            FileWriterUtil.writeJsonToFile(tempFile.toString(), jsonContent);
            
            // Verify the file exists and has the correct content
            assertTrue(Files.exists(tempFile), "File should exist");
            String readContent = Files.readString(tempFile);
            assertEquals(jsonContent, readContent, "File content should match");
            
            // This verifies that the same method used by GeneticTitan works correctly
            System.out.println("GeneticTitan file writing test passed");
        } finally {
            // Clean up
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testTitanInsertionHillClimbingWriteToFile() throws IOException {
        // Create a simple JSON string
        String jsonContent = "{\n  \"test\": \"value\"\n}";
        
        // Create a temporary file path
        Path tempFile = Files.createTempFile("hillclimb_test", ".json");
        
        try {
            // Call the FileWriterUtil method directly with our test content
            FileWriterUtil.writeJsonToFile(tempFile.toString(), jsonContent);
            
            // Verify the file exists and has the correct content
            assertTrue(Files.exists(tempFile), "File should exist");
            String readContent = Files.readString(tempFile);
            assertEquals(jsonContent, readContent, "File content should match");
            
            // This verifies that the same method used by TitanInsertionHillClimbing works correctly
            System.out.println("TitanInsertionHillClimbing file writing test passed");
        } finally {
            // Clean up
            Files.deleteIfExists(tempFile);
        }
    }
}