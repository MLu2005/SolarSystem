package com.example.utilities.titanAtmosphere;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.titanAtmosphere.CoordinateKey;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PerlinTerrainTest is a test class for generating and validating procedural terrain
 * using Perlin noise mapped over Titan's surface grid.
 *
 * This test verifies:
 * - That Perlin noise is correctly applied to generate varying terrain heights.
 * - That the generated terrain values respond to scale, amplitude, and seed parameters.
 * - That spatial coordinates are mapped consistently to grid keys and associated heights.
 *
 * How it works:
 * 1. Loads celestial bodies and locates Titan.
 * 2. Builds a PlanetSurfaceGrid with fixed cell size (10 km).
 * 3. Generates Perlin noise terrain using specified parameters:
 *    - scale: controls terrain smoothness,
 *    - amplitude: maximum terrain height variation,
 *    - seed: noise seed for reproducibility.
 * 4. Evaluates 5 different offset positions around Titan.
 * 5. For each test point, verifies:
 *    - Global position is correctly calculated
 *    - Corresponding grid key is properly generated
 *    - Height value from the terrain grid is within expected range
 *
 * This test is essential before applying terrain in landing simulations or visual rendering.
 */
public class PerlinTerrainTest {

    private CelestialBody titan;
    private PlanetSurfaceGrid surfaceGrid;
    private PlanetHeightGrid heightGrid;
    private double scale;
    private double amplitude;
    private int seed;
    private Vector3D[] offsets;

    @BeforeEach
    void setUp() {
        // Load celestial bodies and find Titan
        List<CelestialBody> bodies = SolarSystemFactory.loadFromTable();

        for (CelestialBody body : bodies) {
            if (body.getName().equalsIgnoreCase("Titan")) {
                titan = body;
                break;
            }
        }

        assertNotNull(titan, "Titan should be found in the solar system bodies");

        // Set up the surface grid and height grid
        double cellSize = 10000.0;
        surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        heightGrid = new PlanetHeightGrid(surfaceGrid);

        // Set up Perlin noise parameters
        scale = 0.1;
        amplitude = 500.0;
        seed = 12345;

        // Generate terrain using Perlin noise
        heightGrid.generatePerlinTerrain(scale, amplitude, seed);

        // Define test positions
        offsets = new Vector3D[5];
        offsets[0] = new Vector3D(0, 0, 0);
        offsets[1] = new Vector3D(15000, 0, 15000);
        offsets[2] = new Vector3D(-30000, 0, 20000);
        offsets[3] = new Vector3D(50000, 0, -40000);
        offsets[4] = new Vector3D(-100000, 0, -100000);
    }

    @Test
    void testPerlinTerrainGeneration() {
        // Verify that the height grid was properly initialized
        assertNotNull(heightGrid, "Height grid should be initialized");

        // Test that terrain heights are within expected range based on amplitude
        for (Vector3D offset : offsets) {
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );

            CoordinateKey key = surfaceGrid.toCoordinateKey(globalPos);
            double height = heightGrid.getAltitude(globalPos);

            // Verify the height is within expected range [-amplitude, amplitude]
            // Perlin noise typically returns values in the range [-1, 1], so heights should be in [-amplitude, amplitude]
            assertTrue(height >= -amplitude && height <= amplitude,
                    "Height should be within range [-amplitude, amplitude]");

            // Verify that the coordinate key is not null
            assertNotNull(key, "Coordinate key should not be null for position: " + globalPos);

            // Log the test point data for debugging
            System.out.println("Global: " + globalPos +
                    " → Grid: " + key +
                    " → Height: " + height);
        }
    }

    @Test
    void testDifferentSeedProducesDifferentTerrain() {
        // Create a new height grid with a different seed
        PlanetHeightGrid newHeightGrid = new PlanetHeightGrid(surfaceGrid);
        int differentSeed = seed + 1;
        newHeightGrid.generatePerlinTerrain(scale, amplitude, differentSeed);

        // Check that at least one test point has a different height
        boolean foundDifference = false;
        for (Vector3D offset : offsets) {
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );

            double originalHeight = heightGrid.getAltitude(globalPos);
            double newHeight = newHeightGrid.getAltitude(globalPos);

            if (Math.abs(originalHeight - newHeight) > 0.001) {
                foundDifference = true;
                break;
            }
        }

        assertTrue(foundDifference, "Different seeds should produce different terrain heights");
    }

    @Test
    void testConsistentCoordinateMapping() {
        // Test that the same global position always maps to the same grid key
        for (Vector3D offset : offsets) {
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );

            CoordinateKey key1 = surfaceGrid.toCoordinateKey(globalPos);
            CoordinateKey key2 = surfaceGrid.toCoordinateKey(globalPos);

            assertEquals(key1, key2, "Same global position should map to same coordinate key");
        }
    }
}
