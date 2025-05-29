package com.example.solarSystem.titanAtmosphere.Tests;

import com.example.solarSystem.Vector3D;
import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.titanAtmosphere.*;
import com.example.solarSystem.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.solarSystem.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import com.example.solarSystem.titanAtmosphere.TerrainGenerator.PlanetWindGrid;
import com.example.spaceMissions.SpaceShip;
import com.example.solarSystem.Physics.SolarSystemFactory;
import com.example.solarSystem.Physics.PhysicsEngine;

import java.util.List;




/**
 * AtmosphericForcePhysicsTest is a simulation test that evaluates how atmospheric drag
 * affects a spaceship at different altitudes above Titan’s surface.
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
 * - In both cases, it prints:
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

    public static void main(String[] args) {


        List<CelestialBody> bodies = SolarSystemFactory.loadFromTable();
        CelestialBody titan = null;
        for (CelestialBody body : bodies) {
            if (body.getName().equalsIgnoreCase("Titan")) {
                titan = body;
                break;
            }
        }

        if (titan == null) {
            System.out.println("Titan not found.");
            return;
        }


        double cellSize = 10000.0;
        PlanetSurfaceGrid surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);

        PlanetHeightGrid heightGrid = new PlanetHeightGrid(surfaceGrid);
        double baseHeight = titan.getPosition().getY();
        heightGrid.generateFlatTerrain(baseHeight);


        PlanetWindGrid windGrid = new PlanetWindGrid(surfaceGrid);
        Vector3D wind = new Vector3D(10, 0, 0);
        windGrid.generateConstantWind(wind);

        TitanEnvironment environment = new TitanEnvironment(heightGrid, windGrid);

        // 3. AtmosphericForce setup
        double dragCoefficient = 0.005;
        double maxAtmosphereAltitude = 600_000;
        AtmosphericForce atmosphericForce = new AtmosphericForce(environment, dragCoefficient, maxAtmosphereAltitude);

        PhysicsEngine engine = new PhysicsEngine();
        Vector3D initialVelocity = new Vector3D(100, 0, 0);


        runTest("SHIP IN ATMOSPHERE (10 km)", titan, 10_000, initialVelocity, environment, atmosphericForce);
        runTest("SHIP IN SPACE (700 km)", titan, 700_000, initialVelocity, environment, atmosphericForce);
    }

    private static void runTest(String label, CelestialBody titan, double altitudeOffset, Vector3D velocity,
                                TitanEnvironment environment, AtmosphericForce atmosphericForce) {

        System.out.println("\n---- " + label + " ----");

        Vector3D position = titan.getPosition().add(new Vector3D(0, altitudeOffset, 0));
        SpaceShip ship = new SpaceShip("TestShip", 0.0, velocity, 1000, 1000, position);

        double surfaceHeight = environment.getAltitude(position);
        double actualAltitude = position.getY() - surfaceHeight;
        Vector3D localWind = environment.getWind(position);
        Vector3D drag = atmosphericForce.compute(ship);
        Vector3D acceleration = drag.scale(1.0 / ship.getMass());
        Vector3D newVelocity = ship.getVelocity().add(acceleration);

        System.out.println("Position: " + position);
        System.out.println("Surface height at location: " + surfaceHeight);
        System.out.println("Altitude above surface: " + actualAltitude + " m");
        System.out.println("Wind at position: " + localWind);
        System.out.println("Relative velocity: " + ship.getVelocity().subtract(localWind));
        System.out.println("Atmospheric drag force: " + drag);
        System.out.println("Acceleration: " + acceleration);
        System.out.println("New velocity after 1s: " + newVelocity);
    }
}
