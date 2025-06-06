import com.example.solar_system.CelestialBody;
import com.example.utilities.DataLoader;
import com.example.utilities.Vector3D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the DataLoader class.
 * Tests loading celestial body data from CSV files.
 */
public class DataLoaderTest {

    /**
     * Tests loading valid celestial body data from a CSV file.
     */
    @Test
    void testLoadBodiesFromCSV() {
        String filePath = "src\\test\\resources\\test_celestial_bodies.csv";
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV(filePath);

        // Check that we loaded the expected number of bodies
        assertEquals(3, bodies.size(), "Should load 3 celestial bodies");

        // Check the first body (Sun)
        CelestialBody sun = bodies.get(0);
        assertEquals("Sun", sun.getName(), "First body should be the Sun");
        assertEquals(1.989e30, sun.getMass(), 1e20, "Sun mass should match");
        assertEquals(0, sun.getPosition().getX(), 1e-9, "Sun X position should be 0");
        assertEquals(0, sun.getVelocity().getY(), 1e-9, "Sun Y velocity should be 0");

        // Check the second body (Earth)
        CelestialBody earth = bodies.get(1);
        assertEquals("Earth", earth.getName(), "Second body should be Earth");
        assertEquals(149.6e6, earth.getPosition().getX(), 1e3, "Earth X position should match");
        assertEquals(29.78, earth.getVelocity().getY(), 1e-9, "Earth Y velocity should match");
    }

    /**
     * Tests handling of malformed data in the CSV file.
     */
    @Test
    void testMalformedCSV() {
        String filePath = "src\\test\\resources\\malformed_celestial_bodies.csv";
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV(filePath);

        // Should only load the valid entry (Sun)
        assertEquals(1, bodies.size(), "Should only load the valid celestial body");
        assertEquals("Sun", bodies.get(0).getName(), "Only valid body should be the Sun");
    }

    /**
     * Tests handling of a non-existent file.
     */
    @Test
    void testFileNotFound() {
        String filePath = "non_existent_file.csv";
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV(filePath);

        // Should return an empty list
        assertTrue(bodies.isEmpty(), "Should return an empty list for non-existent file");
    }

    /**
     * Tests handling of empty lines in the CSV file.
     */
    @Test
    void testEmptyLines(@TempDir Path tempDir) throws IOException {
        // Create a temporary file with empty lines
        Path csvFile = tempDir.resolve("empty_lines.csv");
        Files.writeString(csvFile, 
            "name;x;y;z;vx;vy;vz;mass\n" +
            "\n" +  // Empty line
            "Sun;0;0;0;0;0;0;1.989e30\n" +
            "\n" +  // Empty line
            "Earth;149.6e6;0;0;0;29.78;0;5.972e24\n"
        );

        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV(csvFile.toString());

        // Should skip empty lines and load 2 bodies
        assertEquals(2, bodies.size(), "Should load 2 celestial bodies, skipping empty lines");
        assertEquals("Sun", bodies.get(0).getName(), "First body should be the Sun");
        assertEquals("Earth", bodies.get(1).getName(), "Second body should be Earth");
    }
}
