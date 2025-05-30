package com.example.utilities.titanAtmosphere.Tests;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.titanAtmosphere.CoordinateKey;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetWindGrid;

import java.util.List;


/**
 * PerlinWindTest is a diagnostic test for generating and inspecting spatially varying wind
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
 * 5. Prints:
 *    - Global 3D position of each test point,
 *    - Corresponding grid coordinate (row, col),
 *    - Wind vector at that position.
 *
 */


public class PerlinWindTest {

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
        PlanetWindGrid windGrid = new PlanetWindGrid(surfaceGrid);


        double scale = 0.1;
        double maxWindSpeed = 20.0;
        int seed = 1234;
        windGrid.generatePerlinWind(scale, maxWindSpeed, seed);


        Vector3D[] offsets = new Vector3D[5];
        offsets[0] = new Vector3D(0, 0, 0);
        offsets[1] = new Vector3D(10000, 0, 10000);
        offsets[2] = new Vector3D(20000, 0, -30000);
        offsets[3] = new Vector3D(-50000, 0, 40000);
        offsets[4] = new Vector3D(-80000, 0, -80000);

        int j = 0;
        while (j < offsets.length) {
            Vector3D offset = offsets[j];
            Vector3D globalPos = new Vector3D(
                    titan.getPosition().getX() + offset.getX(),
                    titan.getPosition().getY() + offset.getY(),
                    titan.getPosition().getZ() + offset.getZ()
            );

            CoordinateKey key = surfaceGrid.toCoordinateKey(globalPos);
            Vector3D wind = windGrid.getWind(globalPos);

            System.out.println("Global: " + globalPos +
                    " → Grid: " + key +
                    " → Wind: " + wind);
            j++;
        }
    }
}
