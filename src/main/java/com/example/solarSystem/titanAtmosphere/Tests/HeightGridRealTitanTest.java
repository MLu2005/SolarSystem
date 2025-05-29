package com.example.solarSystem.titanAtmosphere.Tests;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Vector3D;
import com.example.solarSystem.Physics.SolarSystemFactory;
import com.example.solarSystem.titanAtmosphere.CoordinateKey;
import com.example.solarSystem.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.solarSystem.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;

import java.util.List;

/**
 * HeightGridRealTitanTest is a diagnostic test used to verify the mapping between
 * global 3D positions near Titan and their corresponding terrain height values
 * stored in the PlanetHeightGrid.
 *
 * This test confirms:
 * - Correct coordinate conversion from global 3D space to grid indices.
 * - Accurate terrain height retrieval from PlanetHeightGrid.
 * - That various offsets from Titan’s center map to the correct grid cells.
 *
 * How it works:
 * 1. Loads celestial bodies from SolarSystemFactory.
 * 2. Locates Titan in the system.
 * 3. Creates a surface grid with 10 km resolution.
 * 4. Initializes a flat height grid with a fixed height (1234.0 meters).
 * 5. Computes the grid key and height for 4 different offset positions around Titan.
 *
 * Each test prints:
 * - The global test position,
 * - The grid cell it maps to,
 * - The height value stored at that location.
 *
 */


public class HeightGridRealTitanTest {

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


        System.out.println("Titan position: " + titan.getPosition());


        double cellSize = 10000.0;
        PlanetSurfaceGrid surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        PlanetHeightGrid heightGrid = new PlanetHeightGrid(surfaceGrid);
        heightGrid.generateFlatTerrain(1234.0);


        Vector3D[] offsets = new Vector3D[4];
        offsets[0] = new Vector3D(0, 0, 0);
        offsets[1] = new Vector3D(15000, 500, -25000);
        offsets[2] = new Vector3D(-70000, 0, 70000);
        offsets[3] = new Vector3D(9999999, 999, 9999999);

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
