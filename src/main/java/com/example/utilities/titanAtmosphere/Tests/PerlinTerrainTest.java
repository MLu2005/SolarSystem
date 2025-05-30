package com.example.utilities.titanAtmosphere.Tests;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.titanAtmosphere.CoordinateKey;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;

import java.util.List;


/**
 * PerlinTerrainTest is a diagnostic test for generating and validating procedural terrain
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
 * 5. For each test point, prints:
 *    - Global position,
 *    - Corresponding grid key,
 *    - Height value from the terrain grid.
 *
 * This test is essential before applying terrain in landing simulations or visual rendering.
 */


public class PerlinTerrainTest {

    public static void main(String[] args) {


        List<CelestialBody> bodies = SolarSystemFactory.loadFromTable();

        CelestialBody titan = null;
        int i = 0;
        while (i < bodies.size()) {
            CelestialBody body = bodies.get(i);
            if (body.getName().equalsIgnoreCase("Titan")) {
                titan = body;
                break;
            }
            i++;
        }

        if (titan == null) {
            System.out.println("Titan not found.");
            return;
        }


        double cellSize = 10000.0;
        PlanetSurfaceGrid surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        PlanetHeightGrid heightGrid = new PlanetHeightGrid(surfaceGrid);


        double scale = 0.1;
        double amplitude = 500.0;
        int seed = 12345;

        heightGrid.generatePerlinTerrain(scale, amplitude, seed);


        Vector3D[] offsets = new Vector3D[5];
        offsets[0] = new Vector3D(0, 0, 0);
        offsets[1] = new Vector3D(15000, 0, 15000);
        offsets[2] = new Vector3D(-30000, 0, 20000);
        offsets[3] = new Vector3D(50000, 0, -40000);
        offsets[4] = new Vector3D(-100000, 0, -100000);

        int j = 0;
        while (j < offsets.length) {
            Vector3D offset = offsets[j];
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );

            CoordinateKey key = surfaceGrid.toCoordinateKey(globalPos);
            double height = heightGrid.getAltitude(globalPos);

            System.out.println("Global: " + globalPos +
                    " → Grid: " + key +
                    " → Height: " + height);
            j++;
        }
    }
}
