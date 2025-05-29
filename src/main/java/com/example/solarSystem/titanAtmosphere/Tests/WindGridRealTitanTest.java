package com.example.solarSystem.titanAtmosphere.Tests;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Vector3D;
import com.example.solarSystem.Physics.SolarSystemFactory;
import com.example.solarSystem.titanAtmosphere.CoordinateKey;
import com.example.solarSystem.titanAtmosphere.PlanetSurfaceGrid;
import com.example.solarSystem.titanAtmosphere.PlanetWindGrid;

import java.util.List;

public class WindGridRealTitanTest {

    public static void main(String[] args) {

        // 1. Załaduj układ planetarny
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

        // 3. Utwórz siatkę powierzchni i siatkę wiatru
        double cellSize = 10000.0; // 10 km
        PlanetSurfaceGrid surfaceGrid = new PlanetSurfaceGrid(titan, cellSize);
        PlanetWindGrid windGrid = new PlanetWindGrid(surfaceGrid);

        // 4. Wygeneruj stały wiatr (np. 15 m/s na północny wschód)
        Vector3D constantWind = new Vector3D(10, 0, -10);
        windGrid.generateConstantWind(constantWind);

        // 5. Przetestuj kilka pozycji względem Tytana
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
