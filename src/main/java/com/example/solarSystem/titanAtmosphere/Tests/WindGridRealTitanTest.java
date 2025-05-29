package com.example.solarSystem.titanAtmosphere.Tests;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Vector3D;
import com.example.solarSystem.Physics.SolarSystemFactory;
import com.example.solarSystem.titanAtmosphere.CoordinateKey;
import com.example.solarSystem.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import com.example.solarSystem.titanAtmosphere.TerrainGenerator.PlanetWindGrid;

import java.util.List;

/**
 * WindGridRealTitanTest is a diagnostic test that evaluates how a constant wind vector
 * is assigned across Titan’s surface using a grid-based system.
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
 * 5. Prints for each position:
 *    - The global position,
 *    - Mapped grid coordinate,
 *    - The resulting wind vector from the wind grid.
 *
 */



public class WindGridRealTitanTest {

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


        double cellSize = 10000.0; // 10 km
        PlanetSurfaceGrid surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        PlanetWindGrid windGrid = new PlanetWindGrid(surfaceGrid);

        Vector3D constantWind = new Vector3D(10, 0, -10);
        windGrid.generateConstantWind(constantWind);


        Vector3D[] offsets = new Vector3D[4];
        offsets[0] = new Vector3D(0, 0, 0);
        offsets[1] = new Vector3D(15000, 500, -25000);
        offsets[2] = new Vector3D(-70000, 0, 70000);
        offsets[3] = new Vector3D(9999999, 999, 9999999); // poza siatką

        int j = 0;
        while (j < offsets.length) {
            Vector3D offset = offsets[j];
            Vector3D titanPos = titan.getPosition();
            Vector3D globalPos = new Vector3D(
                    titanPos.getX() + offset.getX(),
                    titanPos.getY() + offset.getY(),
                    titanPos.getZ() + offset.getZ()
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
