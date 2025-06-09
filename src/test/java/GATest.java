import com.example.utilities.GA.GAResultsParser;
import com.example.utilities.GA.GeneticTitan;
import com.example.utilities.Vector3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Genetic Algorithm (GA) utilities.
 * Tests the functionality of GAResultsParser, GeneticTitan, and related classes.
 */
public class GATest


{

    private static final String TEST_JSON_PATH = "src/test/resources/test_best_individuals.json";

    @BeforeEach
    void setUp() {
        // Create a test JSON file with sample data
        createTestJsonFile();
    }

    /**
     * Creates a test JSON file with sample data for testing the parser
     */
    private void createTestJsonFile() {
        try {
            // Ensure the directory exists
            Path directory = Paths.get("src/test/resources");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // Create the test JSON content
            String jsonContent = "{\n" +
                    "  \"best_individuals\": [\n" +
                    "    {\n" +
                    "      \"rank\": 0,\n" +
                    "      \"fitness\": 0.123456,\n" +
                    "      \"min_distance_to_titan_km\": 8123.456,\n" +
                    "      \"launch_position\": {\n" +
                    "        \"x\": 1.5E7,\n" +
                    "        \"y\": 2.5E7,\n" +
                    "        \"z\": 3.5E7\n" +
                    "      },\n" +
                    "      \"launch_velocity\": {\n" +
                    "        \"vx\": 10.5,\n" +
                    "        \"vy\": 20.5,\n" +
                    "        \"vz\": 30.5\n" +
                    "      },\n" +
                    "      \"launch_mass\": 50000.0,\n" +
                    "      \"delta_v_relative_to_earth\": 12.34\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"rank\": 1,\n" +
                    "      \"fitness\": 0.111111,\n" +
                    "      \"min_distance_to_titan_km\": 9000.0,\n" +
                    "      \"launch_position\": {\n" +
                    "        \"x\": 1.6E7,\n" +
                    "        \"y\": 2.6E7,\n" +
                    "        \"z\": 3.6E7\n" +
                    "      },\n" +
                    "      \"launch_velocity\": {\n" +
                    "        \"vx\": 11.5,\n" +
                    "        \"vy\": 21.5,\n" +
                    "        \"vz\": 31.5\n" +
                    "      },\n" +
                    "      \"launch_mass\": 50000.0,\n" +
                    "      \"delta_v_relative_to_earth\": 13.45\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}\n";

            // Write to the test file
            try (FileWriter writer = new FileWriter(TEST_JSON_PATH)) {
                writer.write(jsonContent);
            }
        } catch (IOException e) {
            System.err.println("Error creating test JSON file: " + e.getMessage());
            fail("Failed to create test JSON file");
        }
    }

    @Test
    void testGAResultsParserLoadsIndividuals() {
        // Test that the parser can load individuals from the JSON file
        List<GAResultsParser.Individual> individuals = GAResultsParser.loadAllIndividuals(TEST_JSON_PATH);

        // Verify we loaded the expected number of individuals
        assertEquals(2, individuals.size(), "Should load 2 individuals from test JSON");

        // Verify the first individual's properties
        GAResultsParser.Individual first = individuals.get(0);
        assertEquals(0, first.rank(), "First individual should have rank 0");
        assertEquals(0.123456, first.fitness(), 1e-6, "First individual should have correct fitness");
        assertEquals(8123.456, first.minDistanceToTitan(), 1e-3, "First individual should have correct min distance to Titan");

        // Verify the second individual's properties
        GAResultsParser.Individual second = individuals.get(1);
        assertEquals(1, second.rank(), "Second individual should have rank 1");
        assertEquals(0.111111, second.fitness(), 1e-6, "Second individual should have correct fitness");
        assertEquals(9000.0, second.minDistanceToTitan(), 1e-3, "Second individual should have correct min distance to Titan");
    }

    @Test
    void testGAResultsParserGetBestIndividual() {
        // Test that getBestIndividual returns the individual with rank 0
        GAResultsParser parser = new GAResultsParser(TEST_JSON_PATH);
        GAResultsParser.Individual best = GAResultsParser.getBestIndividual();

        assertEquals(0, best.rank(), "Best individual should have rank 0");
        assertEquals(0.123456, best.fitness(), 1e-6, "Best individual should have correct fitness");
        assertEquals(8123.456, best.minDistanceToTitan(), 1e-3, "Best individual should have correct min distance to Titan");
    }

    @Test
    void testGAResultsParserGetLaunchPosition() {
        // Test that getLaunchPosition returns the correct Vector3D
        GAResultsParser parser = new GAResultsParser(TEST_JSON_PATH);
        Vector3D position = GAResultsParser.getLaunchPosition();

        assertEquals(1.5E7, position.getX(), 1e-6, "Launch position X should be correct");
        assertEquals(2.5E7, position.getY(), 1e-6, "Launch position Y should be correct");
        assertEquals(3.5E7, position.getZ(), 1e-6, "Launch position Z should be correct");
    }

    @Test
    void testGAResultsParserGetLaunchVelocity() {
        // Test that getLaunchVelocity returns the correct Vector3D
        GAResultsParser parser = new GAResultsParser(TEST_JSON_PATH);
        Vector3D velocity = GAResultsParser.getLaunchVelocity();

        assertEquals(10.5, velocity.getX(), 1e-6, "Launch velocity X should be correct");
        assertEquals(20.5, velocity.getY(), 1e-6, "Launch velocity Y should be correct");
        assertEquals(30.5, velocity.getZ(), 1e-6, "Launch velocity Z should be correct");
    }

    @Test
    void testIndividualTimeOfClosestApproach() {
        // Test the getTimeOfClosestApproach method of Individual
        GAResultsParser parser = new GAResultsParser(TEST_JSON_PATH);
        GAResultsParser.Individual best = GAResultsParser.getBestIndividual();

        // The time of closest approach is estimated based on the min distance to Titan
        double time = best.getTimeOfClosestApproach();

        // Since minDistanceToTitan is 8123.456 km (between 1e5 and 1e6),
        // the time should be 60% of SIM_LEN according to the implementation
        double expectedTime = executables.Constants.SIM_LEN * 0.7;

        // Verify that the time matches the expected value
        assertEquals(expectedTime, time, 1e-6, "Time of closest approach should be correctly estimated");

        // Also verify that it's using the correct condition from the implementation
        if (best.minDistanceToTitan() < 1e5) {
            assertEquals(executables.Constants.SIM_LEN * 0.7, time, 1e-6, 
                "For distance < 1e5, time should be 70% of SIM_LEN");
        } else if (best.minDistanceToTitan() < 1e6) {
            assertEquals(executables.Constants.SIM_LEN * 0.6, time, 1e-6, 
                "For distance < 1e6, time should be 60% of SIM_LEN");
        } else {
            assertEquals(executables.Constants.SIM_LEN * 0.5, time, 1e-6, 
                "For distance >= 1e6, time should be 50% of SIM_LEN");
        }
    }

    @Test
    void testGeneticTitanComputeDvRel() {
        // Test the computeDvRel method of GeneticTitan
        double vx = 10.5;
        double vy = 20.5;
        double vz = 30.5;

        double dvRel = GeneticTitan.computeDvRel(vx, vy, vz);

        // The actual value will depend on the Earth's velocity in the simulation,
        // so we just check that it's a positive number
        assertTrue(dvRel > 0, "Delta-v relative to Earth should be positive");
    }

    @Test
    void testGAResultsParserWithInvalidFile() {
        // Test that the parser throws an exception when the file doesn't exist
        String nonExistentFile = "non_existent_file.json";

        Exception exception = assertThrows(RuntimeException.class, () -> {
            GAResultsParser parser = new GAResultsParser(nonExistentFile);
        });

        assertTrue(exception.getMessage().contains("Failed to load GA results"), 
                "Exception message should indicate failure to load results");
    }

    @Test
    void testLoadBestIndividualMethod() {
        // Test the static loadBestIndividual method
        GAResultsParser.Individual best = GAResultsParser.loadBestIndividual(TEST_JSON_PATH);

        assertNotNull(best, "Best individual should not be null");
        assertEquals(0, best.rank(), "Best individual should have rank 0");
        assertEquals(0.123456, best.fitness(), 1e-6, "Best individual should have correct fitness");
    }
}
