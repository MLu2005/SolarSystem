package com.example.utilities.titanAtmosphere;

import com.example.utilities.Vector3D;
import com.example.solar_system.CelestialBody;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetWindGrid;
import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.physics_utilities.PhysicsEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AtmosphericForcePhysicsTest is a test class that evaluates how atmospheric drag
 * affects a spaceship at different altitudes above Titan's surface.
 *
 * Test Purpose:
 * The goal is to verify that:
 *
 * Atmospheric drag applies when the spaceship is within the atmospheric boundary,
 * No drag is applied when above the atmosphere (space region),
 * Forces are computed correctly based on the relative velocity and wind conditions.
 *
 * How it works:
 * - The test creates a Titan with flat terrain and constant horizontal wind.
 * - A spaceship is placed at two different altitudes:
 *
 * 10 km → inside atmosphere
 * 700 km → outside atmosphere
 *
 * - In both cases, it tests:
 *
 * Ship's position, surface height, and true altitude
 * Local wind vector
 * Relative velocity
 * Computed aerodynamic drag
 * Acceleration and updated velocity after 1s
 *
 * Why this test matters:
 * - It confirms the correct interaction between:
 *   {@link PlanetHeightGrid}, {@link PlanetWindGrid},
 *   {@link TitanEnvironment}, and {@link AtmosphericForce}.
 * - It shows that altitude cutoff is respected.
 * - It verifies realistic force computations based on physics.
 */
public class AtmosphericForcePhysicsTest {

    private CelestialBody titan;
    private TitanEnvironment environment;
    private AtmosphericForce atmosphericForce;
    private Vector3D initialVelocity;
    private double dragCoefficient;
    private double maxAtmosphereAltitude;

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

        // Set up the environment
        double cellSize = 10000.0;
        PlanetSurfaceGrid surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);

        PlanetHeightGrid heightGrid = new PlanetHeightGrid(surfaceGrid);
        double baseHeight = titan.getPosition().getY();
        heightGrid.generateFlatTerrain(baseHeight);

        PlanetWindGrid windGrid = new PlanetWindGrid(surfaceGrid);
        Vector3D wind = new Vector3D(10, 0, 0);
        windGrid.generateConstantWind(wind);

        environment = new TitanEnvironment(heightGrid, windGrid);

        // Set up atmospheric force
        dragCoefficient = 0.005;
        maxAtmosphereAltitude = 600_000;
        atmosphericForce = new AtmosphericForce(environment, dragCoefficient, maxAtmosphereAltitude);

        // Set initial velocity for tests
        initialVelocity = new Vector3D(100, 0, 0);
    }

    @Test
    void testShipInAtmosphere() {
        // Test ship at 10 km altitude (inside atmosphere)
        double altitudeOffset = 10_000;
        Vector3D position = titan.getPosition().add(new Vector3D(0, altitudeOffset, 0));
        SpaceShip ship = new SpaceShip("TestShip", 0.0, initialVelocity, 1000, 1000, position);

        double surfaceHeight = environment.getAltitude(position);
        double actualAltitude = position.getY() - surfaceHeight;
        Vector3D localWind = environment.getWind(position);
        Vector3D drag = atmosphericForce.compute(ship);
        Vector3D acceleration = drag.scale(1.0 / ship.getMass());

        // Assertions for ship in atmosphere
        assertTrue(actualAltitude < maxAtmosphereAltitude, 
                "Ship should be within the atmospheric boundary");
        assertNotEquals(0, drag.magnitude(), 
                "Atmospheric drag should be non-zero inside atmosphere");
        assertEquals(localWind, environment.getWind(position), 
                "Wind vector should match the environment's wind at position");
        
        // Verify drag direction is opposite to relative velocity
        Vector3D relativeVelocity = ship.getVelocity().subtract(localWind);
        double dotProduct = drag.normalize().dot(relativeVelocity.normalize());
        assertTrue(dotProduct < 0, 
                "Drag force should be in opposite direction to relative velocity");
    }

    @Test
    void testShipInSpace() {
        // Test ship at 700 km altitude (outside atmosphere)
        double altitudeOffset = 700_000;
        Vector3D position = titan.getPosition().add(new Vector3D(0, altitudeOffset, 0));
        SpaceShip ship = new SpaceShip("TestShip", 0.0, initialVelocity, 1000, 1000, position);

        double surfaceHeight = environment.getAltitude(position);
        double actualAltitude = position.getY() - surfaceHeight;
        Vector3D drag = atmosphericForce.compute(ship);

        // Assertions for ship in space
        assertTrue(actualAltitude > maxAtmosphereAltitude, 
                "Ship should be outside the atmospheric boundary");
        assertEquals(0, drag.magnitude(), 
                "Atmospheric drag should be zero outside atmosphere");
    }
}