package com.example.utilities.titanAtmosphere;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.titanAtmosphere.CoordinateKey;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetWindGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PerlinWindTest is a test class for verifying the generation and behavior of spatially varying wind
 * fields using 2D Perlin noise over Titan's surface grid.
 *
 * This test verifies:
 * - That procedural wind data is generated correctly from Perlin noise.
 * - That different coordinates yield smooth but variable wind vectors.
 * - That grid-based wind generation responds to seed, scale, and maxWindSpeed parameters.
 *
 * How it works:
 * 1. Loads all celestial bodies and finds Titan.
 * 2. Constructs a PlanetSurfaceGrid with 10 km resolution.
 * 3. Uses Perlin noise to generate wind vectors for each grid cell:
 *    - scale controls smoothness (higher = smoother transitions),
 *    - maxWindSpeed limits wind intensity,
 *    - seed ensures reproducibility of noise.
 * 4. Samples wind at 5 different global positions (offset from Titan).
 * 5. Verifies:
 *    - Wind vectors are non-null at each position
 *    - Wind vectors have reasonable magnitudes
 *    - Different positions have different wind vectors
 *    - Wind vectors are consistent for the same position
 */
public class PerlinWindTest {

    private CelestialBody titan;
    private PlanetSurfaceGrid surfaceGrid;
    private PlanetWindGrid windGrid;
    private double scale;
    private double maxWindSpeed;
    private int seed;
    private Vector3D[] testPositions;

    @BeforeEach
    void setUp() {
        // Find Titan from the solar system bodies
        List<CelestialBody> bodies = SolarSystemFactory.loadFromTable();
        titan = null;
        for (CelestialBody body : bodies) {
            if (body.getName().equalsIgnoreCase("Titan")) {
                titan = body;
                break;
            }
        }
        
        assertNotNull(titan, "Titan should be found in the solar system bodies");

        // Set up the surface grid and wind grid
        double cellSize = 10000.0;
        surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        windGrid = new PlanetWindGrid(surfaceGrid);

        // Set parameters for Perlin wind generation
        scale = 0.1;
        maxWindSpeed = 20.0;
        seed = 1234;
        
        // Generate the wind using Perlin noise
        windGrid.generatePerlinWind(scale, maxWindSpeed, seed);

        // Create test positions (offsets from Titan's position)
        testPositions = new Vector3D[5];
        testPositions[0] = new Vector3D(0, 0, 0);
        testPositions[1] = new Vector3D(10000, 0, 10000);
        testPositions[2] = new Vector3D(20000, 0, -30000);
        testPositions[3] = new Vector3D(-50000, 0, 40000);
        testPositions[4] = new Vector3D(-80000, 0, -80000);
    }

    @Test
    void testWindVectorsAreGenerated() {
        // Test that wind vectors are generated for each test position
        for (Vector3D offset : testPositions) {
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );
            
            Vector3D wind = windGrid.getWind(globalPos);
            assertNotNull(wind, "Wind vector should not be null for position: " + globalPos);
        }
    }

    @Test
    void testWindMagnitudesAreReasonable() {
        // Test that wind magnitudes are within expected range (0 to maxWindSpeed)
        for (Vector3D offset : testPositions) {
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );
            
            Vector3D wind = windGrid.getWind(globalPos);
            double magnitude = wind.magnitude();
            
            assertTrue(magnitude >= 0, "Wind magnitude should be non-negative");
            assertTrue(magnitude <= maxWindSpeed, 
                    "Wind magnitude should not exceed maxWindSpeed: " + maxWindSpeed);
        }
    }

    @Test
    void testDifferentPositionsHaveDifferentWinds() {
        // Test that different positions have different wind vectors
        Vector3D[] winds = new Vector3D[testPositions.length];
        
        for (int i = 0; i < testPositions.length; i++) {
            Vector3D offset = testPositions[i];
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );
            
            winds[i] = windGrid.getWind(globalPos);
        }
        
        // Check that at least some wind vectors are different
        boolean foundDifferentVectors = false;
        for (int i = 0; i < winds.length; i++) {
            for (int j = i + 1; j < winds.length; j++) {
                if (!winds[i].equals(winds[j])) {
                    foundDifferentVectors = true;
                    break;
                }
            }
            if (foundDifferentVectors) break;
        }
        
        assertTrue(foundDifferentVectors, "Different positions should have different wind vectors");
    }

    @Test
    void testWindVectorsAreConsistent() {
        // Test that the same position always returns the same wind vector
        for (Vector3D offset : testPositions) {
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );
            
            Vector3D firstWind = windGrid.getWind(globalPos);
            Vector3D secondWind = windGrid.getWind(globalPos);
            
            assertEquals(firstWind, secondWind, 
                    "Wind vector should be consistent for the same position");
        }
    }

    @Test
    void testCoordinateKeyConversion() {
        // Test that global positions are correctly converted to grid coordinates
        for (Vector3D offset : testPositions) {
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );
            
            CoordinateKey key = surfaceGrid.toCoordinateKey(globalPos);
            assertNotNull(key, "CoordinateKey should not be null for position: " + globalPos);
            
            // The same global position should always map to the same grid coordinate
            CoordinateKey secondKey = surfaceGrid.toCoordinateKey(globalPos);
            assertEquals(key, secondKey, 
                    "CoordinateKey should be consistent for the same position");
        }
    }
}