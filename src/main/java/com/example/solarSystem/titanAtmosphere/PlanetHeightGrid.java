package com.example.solarSystem.titanAtmosphere;


import com.example.solarSystem.Vector3D;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores height data (terrain elevation) for a planet using a discrete surface grid.
 * Converts global spaceship positions to local surface keys and returns terrain altitude.
 */
public class PlanetHeightGrid {

    private final PlanetSurfaceGrid grid;
    private final Map<CoordinateKey, Double> heightMap;

    /**
     * Creates a new height grid associated with a planet's surface grid.
     *
     * @param grid the PlanetSurfaceGrid used to map 3D positions to grid coordinates
     */
    public PlanetHeightGrid(PlanetSurfaceGrid grid) {
        this.grid = grid;
        this.heightMap = new HashMap<>();
    }

    /**
     * Fills the entire grid with a flat terrain of a fixed height.
     * Useful for testing or initializing a base terrain.
     *
     * @param height the fixed height value (e.g., 0.0 or 500.0 meters)
     */
    public void generateFlatTerrain(double height) {
        int range = 100; // Creates a 201x201 grid (from -100 to +100)

        for (int row = -range; row <= range; row++) {
            for (int col = -range; col <= range; col++) {
                CoordinateKey key = new CoordinateKey(row, col);
                heightMap.put(key, height);
            }
        }
    }

    /**
     * Returns the altitude (terrain height) at a given global position.
     *
     * @param globalPosition the position of the object in 3D space
     * @return the height at that location, or 0.0 if not initialized
     */
    public double getAltitude(Vector3D globalPosition) {
        CoordinateKey key = grid.toCoordinateKey(globalPosition);
        return heightMap.getOrDefault(key, 0.0);
    }

    /**
     * Sets the height value for a specific coordinate.
     *
     * @param key   the grid coordinate
     * @param value the terrain height to assign
     */
    public void setHeight(CoordinateKey key, double value) {
        heightMap.put(key, value);
    }
}
