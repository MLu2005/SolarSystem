package com.example.utilities.titanAtmosphere.TerrainGenerator;

import com.example.utilities.Vector3D;
import com.example.utilities.titanAtmosphere.CoordinateKey;

import java.util.HashMap;
import java.util.Map;

/**
 * PlanetWindGrid stores atmospheric wind vectors at discrete locations
 * on a planet's surface. The grid is based on PlanetSurfaceGrid and maps
 * each grid cell (CoordinateKey) to a wind vector (Vector3D).
 *
 * This grid allows the simulation to calculate aerodynamic drag on spacecraft
 * as they move through Titan's atmosphere, relative to local wind conditions.
 */
public class PlanetWindGrid {

    /** Reference to the surface grid used for coordinate projection. */
    private final PlanetSurfaceGrid grid;

    /** Wind vector map: each CoordinateKey maps to a 3D wind vector. */
    private final Map<CoordinateKey, Vector3D> windMap;

    /**
     * Constructs a wind grid associated with a planetary surface grid.
     *
     * @param grid the PlanetSurfaceGrid used for coordinate mapping
     */
    public PlanetWindGrid(PlanetSurfaceGrid grid) {
        this.grid = grid;
        this.windMap = new HashMap<>();
    }

    /**
     * Assigns a constant wind vector to all grid cells in a predefined area.
     *
     * @param wind the wind vector to assign to all cells
     */
    public void generateConstantWind(Vector3D wind) {
        int range = 100; // Grid from -100 to +100 in rows/columns (201x201)

        int row = -range;
        while (row <= range) {
            int col = -range;
            while (col <= range) {
                CoordinateKey key = new CoordinateKey(row, col);
                windMap.put(key, wind);
                col++;
            }
            row++;
        }
    }

    /**
     * Generates spatially varying wind using Perlin noise. Two noise maps
     * (for X and Z directions) are combined to create realistic horizontal wind vectors.
     *
     * @param scale         controls the spatial frequency of the wind field
     * @param maxWindSpeed  maximum absolute speed of wind in any direction
     * @param seed          base seed for noise generation
     */
    public void generatePerlinWind(double scale, double maxWindSpeed, int seed) {
        int range = 100;
        SimplePerlinNoise noiseX = new SimplePerlinNoise(seed);
        SimplePerlinNoise noiseZ = new SimplePerlinNoise(seed + 999); // independent direction

        int row = -range;
        while (row <= range) {
            int col = -range;
            while (col <= range) {
                double nx = row * scale;
                double nz = col * scale;

                // Scale Perlin output from [-1, 1] to [-maxWindSpeed, +maxWindSpeed]
                double windX = noiseX.noise(nx, nz) * maxWindSpeed;
                double windZ = noiseZ.noise(nx, nz) * maxWindSpeed;

                Vector3D windVector = new Vector3D(windX, 0, windZ);
                CoordinateKey key = new CoordinateKey(row, col);
                windMap.put(key, windVector);
                col++;
            }
            row++;
        }
    }

    /**
     * Returns the wind vector at a given global position in space.
     * If no wind is defined at the corresponding grid location, a zero vector is returned.
     *
     * @param globalPosition the absolute position in 3D space
     * @return the wind vector at that position, or Vector3D(0, 0, 0) if undefined
     */
    public Vector3D getWind(Vector3D globalPosition) {
        CoordinateKey key = grid.toCoordinateKey(globalPosition);
        Vector3D wind = windMap.get(key);
        if (wind == null) {
            return new Vector3D(0, 0, 0);
        }
        return wind;
    }

    /**
     * Assigns a wind vector manually to a specific grid coordinate.
     *
     * @param key  the grid coordinate to assign wind to
     * @param wind the wind vector
     */
    public void setWind(CoordinateKey key, Vector3D wind) {
        windMap.put(key, wind);
    }
}
