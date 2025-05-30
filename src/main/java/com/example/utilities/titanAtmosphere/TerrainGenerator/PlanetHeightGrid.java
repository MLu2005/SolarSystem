package com.example.utilities.titanAtmosphere.TerrainGenerator;

import com.example.utilities.Vector3D;
import com.example.utilities.titanAtmosphere.CoordinateKey;

import java.util.HashMap;
import java.util.Map;

/**
 * PlanetHeightGrid stores elevation (terrain height) values across a grid
 * representing a planet's surface. It maps 3D global positions to discrete
 * CoordinateKeys using a PlanetSurfaceGrid, and retrieves or generates terrain
 * heights at those grid cells.
 *
 * The grid works in a 2D XZ-plane (ignoring Y), centered around the planet's core.
 * This system allows simplified simulation of landing and atmospheric interactions
 * without spherical mapping.
 */
public class PlanetHeightGrid {

    /** Surface grid used to convert 3D positions to grid coordinates. */
    private final PlanetSurfaceGrid grid;

    /** Map storing elevation values by grid key (row, col). */
    private final Map<CoordinateKey, Double> heightMap;

    /**
     * Constructs a height grid using a reference surface grid.
     *
     * @param grid the PlanetSurfaceGrid used to map 3D positions to grid coordinates
     */
    public PlanetHeightGrid(PlanetSurfaceGrid grid) {
        this.grid = grid;
        this.heightMap = new HashMap<>();
    }

    /**
     * Fills the entire grid with a flat terrain of a fixed height.
     *
     * @param height the fixed terrain height to assign to all grid cells
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
     * Generates varied terrain using Perlin noise, producing natural hills and valleys.
     *
     * @param scale     spatial frequency of the terrain pattern (0.05)
     * @param amplitude maximum height variation (1000.0 for 1km peaks)
     * @param seed      random seed for noise generation
     */
    public void generatePerlinTerrain(double scale, double amplitude, int seed) {
        int range = 100;
        SimplePerlinNoise noise = new SimplePerlinNoise(seed);

        int row = -range;
        while (row <= range) {
            int col = -range;
            while (col <= range) {
                double nx = row * scale;
                double ny = col * scale;

                double height = noise.noise(nx, ny) * amplitude;

                CoordinateKey key = new CoordinateKey(row, col);
                heightMap.put(key, height);
                col++;
            }
            row++;
        }
    }

    /**
     * Returns the terrain height at a given global 3D position.
     * If the position lies outside initialized terrain, defaults to 0.0.
     *
     * @param globalPosition the absolute position in 3D space
     * @return the terrain height at the corresponding grid location
     */
    public double getAltitude(Vector3D globalPosition) {
        CoordinateKey key = grid.toCoordinateKey(globalPosition);
        return heightMap.getOrDefault(key, 0.0);
    }

    /**
     * Sets a custom height value at a given grid coordinate.
     *
     * @param key   the grid coordinate key
     * @param value the height value to assign
     */
    public void setHeight(CoordinateKey key, double value) {
        heightMap.put(key, value);
    }
}
