package com.example.solarSystem.titanAtmosphere.Tests;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Vector3D;
import com.example.solarSystem.Physics.SolarSystemFactory;
import com.example.solarSystem.titanAtmosphere.CoordinateKey;
import com.example.solarSystem.titanAtmosphere.PlanetHeightGrid;
import com.example.solarSystem.titanAtmosphere.PlanetSurfaceGrid;

import java.util.List;

public class HeightGridRealTitanTest {

    public static void main(String[] args) {

        // 1. Załaduj listę ciał z fabryki
        List<CelestialBody> bodies = SolarSystemFactory.loadFromTable();

        // 2. Znajdź Tytana
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

        // 3. Wyświetl pozycję Tytana
        System.out.println("Titan position: " + titan.getPosition());

        // 4. Utwórz siatkę powierzchni i wysokości
        double cellSize = 10000.0;
        PlanetSurfaceGrid surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        PlanetHeightGrid heightGrid = new PlanetHeightGrid(surfaceGrid);
        heightGrid.generateFlatTerrain(1234.0);

        // 5. Przetestuj kilka pozycji wokół Tytana
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
