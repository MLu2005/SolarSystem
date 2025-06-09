package com.example.utilities.titanAtmosphere;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetWindGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * WindGridRealTitanTest is a test class that evaluates how a constant wind vector
 * is assigned across Titan's surface using a grid-based system.
 *
 * This test confirms:
 * - That wind is applied uniformly to all cells when using generateConstantWind().
 * - That global 3D positions are correctly mapped to grid coordinates.
 * - That wind outside of the initialized grid returns a default value (zero vector).
 *
 * How it works:
 * 1. Loads celestial bodies and finds Titan.
 * 2. Creates a PlanetSurfaceGrid with 10 km resolution.
 * 3. Initializes a PlanetWindGrid and applies a constant wind vector (10, 0, -10).
 * 4. Evaluates 4 positions around Titan:
 *    - Some within the grid range,
 *    - One intentionally far outside the range.
 * 5. Tests for each position:
 *    - The global position,
 *    - Mapped grid coordinate,
 *    - The resulting wind vector from the wind grid.
 */
public class WindGridRealTitanTest {

    private CelestialBody titan;
    private PlanetSurfaceGrid surfaceGrid;
    private PlanetWindGrid windGrid;
    private Vector3D constantWind;
    private Vector3D[] testPositions;
    
    @BeforeEach
    void setUp() {
        // Find Titan from the solar system bodies
        var bodies = SolarSystemFactory.loadFromTable();
        for (CelestialBody body : bodies) {
            if (body.getName().equalsIgnoreCase("Titan")) {
                titan = body;
                break;
            }
        }
        
        assertNotNull(titan, "Titan should be found in the solar system bodies");

        // Set up the grid and wind
        double cellSize = 10000.0; // 10 km
        surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        windGrid = new PlanetWindGrid(surfaceGrid);

        constantWind = new Vector3D(10, 0, -10);
        windGrid.generateConstantWind(constantWind);

        // Define test positions
        Vector3D titanPos = titan.getPosition();
        testPositions = new Vector3D[4];
        
        // Position offsets from Titan's center
        Vector3D[] offsets = new Vector3D[4];
        offsets[0] = new Vector3D(0, 0, 0);
        offsets[1] = new Vector3D(15000, 500, -25000);
        offsets[2] = new Vector3D(-70000, 0, 70000);
        offsets[3] = new Vector3D(9999999, 999, 9999999); // Far outside the grid
        
        // Calculate global positions
        for (int i = 0; i < offsets.length; i++) {
            testPositions[i] = new Vector3D(
                titanPos.getX() + offsets[i].getX(),
                titanPos.getY() + offsets[i].getY(),
                titanPos.getZ() + offsets[i].getZ()
            );
        }
    }

    @Test
    void testConstantWindInGrid() {
        // Test positions within the grid (first 3 positions)
        for (int i = 0; i < 3; i++) {
            Vector3D globalPos = testPositions[i];
            CoordinateKey key = surfaceGrid.toCoordinateKey(globalPos);
            Vector3D wind = windGrid.getWind(globalPos);
            
            // Assert that wind matches the constant wind we set
            assertEquals(constantWind, wind, 
                    "Wind at position " + globalPos + " (grid: " + key + ") should match constant wind");
        }
    }
    
    @Test
    void testPositionOutsideGrid() {
        // Test position far outside the grid (last position)
        Vector3D globalPos = testPositions[3];
        CoordinateKey key = surfaceGrid.toCoordinateKey(globalPos);
        Vector3D wind = windGrid.getWind(globalPos);
        
        // Assert that wind outside grid is zero vector (default)
        assertEquals(0, wind.magnitude(), 
                "Wind at position " + globalPos + " (grid: " + key + ") outside grid should be zero");
    }
    
    @Test
    void testCoordinateMapping() {
        // Test that different global positions map to different grid coordinates
        CoordinateKey key0 = surfaceGrid.toCoordinateKey(testPositions[0]);
        CoordinateKey key1 = surfaceGrid.toCoordinateKey(testPositions[1]);
        CoordinateKey key2 = surfaceGrid.toCoordinateKey(testPositions[2]);
        
        // Assert that positions map to different grid coordinates
        assertNotEquals(key0, key1, "Different positions should map to different grid coordinates");
        assertNotEquals(key0, key2, "Different positions should map to different grid coordinates");
        assertNotEquals(key1, key2, "Different positions should map to different grid coordinates");
    }
}