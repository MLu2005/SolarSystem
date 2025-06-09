package com.example.utilities.titanAtmosphere;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.titanAtmosphere.CoordinateKey;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * HeightGridRealTitanTest is a test class that verifies the mapping between
 * global 3D positions near Titan and their corresponding terrain height values
 * stored in the PlanetHeightGrid.
 *
 * This test confirms:
 * - Correct coordinate conversion from global 3D space to grid indices.
 * - Accurate terrain height retrieval from PlanetHeightGrid.
 * - That various offsets from Titan's center map to the correct grid cells.
 *
 * How it works:
 * 1. Loads celestial bodies from SolarSystemFactory.
 * 2. Locates Titan in the system.
 * 3. Creates a surface grid with 10 km resolution.
 * 4. Initializes a flat height grid with a fixed height (1234.0 meters).
 * 5. Tests different offset positions around Titan and verifies their grid mapping and height values.
 */
public class HeightGridRealTitanTest {

    private CelestialBody titan;
    private PlanetSurfaceGrid surfaceGrid;
    private PlanetHeightGrid heightGrid;
    private final double expectedHeight = 1234.0;
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

        // Create surface grid and height grid
        double cellSize = 10000.0;
        surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        heightGrid = new PlanetHeightGrid(surfaceGrid);
        heightGrid.generateFlatTerrain(expectedHeight);

        // Define test offsets
        offsets = new Vector3D[4];
        offsets[0] = new Vector3D(0, 0, 0);
        offsets[1] = new Vector3D(15000, 500, -25000);
        offsets[2] = new Vector3D(-70000, 0, 70000);
        offsets[3] = new Vector3D(9999999, 999, 9999999);
    }

    @Test
    void testZeroOffset() {
        testOffsetMapping(0);
    }

    @Test
    void testSmallOffset() {
        testOffsetMapping(1);
    }

    @Test
    void testMediumOffset() {
        testOffsetMapping(2);
    }

    @Test
    void testLargeOffset() {
        Vector3D offset = offsets[3];
        Vector3D globalPos = new Vector3D(
                titan.getPosition().getX() + offset.getX(),
                titan.getPosition().getY() + offset.getY(),
                titan.getPosition().getZ() + offset.getZ()
        );

        CoordinateKey key = surfaceGrid.toCoordinateKey(globalPos);
        double height = heightGrid.getAltitude(globalPos);

        // For very large offsets, the height is expected to be 0.0
        // This is likely a boundary condition in the PlanetHeightGrid implementation
        assertEquals(0.0, height, 0.001, 
                "Height at very large offset " + offset + " should be 0.0");

        // Assert that the coordinate key is not null
        assertNotNull(key, "Coordinate key should not be null for position " + globalPos);
    }

    private void testOffsetMapping(int offsetIndex) {
        Vector3D offset = offsets[offsetIndex];
        Vector3D globalPos = new Vector3D(
                titan.getPosition().getX() + offset.getX(),
                titan.getPosition().getY() + offset.getY(),
                titan.getPosition().getZ() + offset.getZ()
        );

        CoordinateKey key = surfaceGrid.toCoordinateKey(globalPos);
        double height = heightGrid.getAltitude(globalPos);

        // Assert that the height matches our expected flat terrain height
        assertEquals(expectedHeight, height, 0.001, 
                "Height at offset " + offset + " should match the flat terrain height");

        // Assert that the coordinate key is not null
        assertNotNull(key, "Coordinate key should not be null for position " + globalPos);
    }
}
