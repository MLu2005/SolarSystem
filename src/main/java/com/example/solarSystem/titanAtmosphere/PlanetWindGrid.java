package com.example.solarSystem.titanAtmosphere;

import com.example.solarSystem.Vector3D;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores atmospheric wind data mapped to a discrete surface grid of the planet.
 * Wind vectors can be used to calculate aerodynamic forces acting on a spaceship.
 */
public class PlanetWindGrid {

    private final PlanetSurfaceGrid grid;
    private final Map<CoordinateKey, Vector3D> windMap;

    /**
     * Creates a wind grid based on a surface grid of the planet.
     *
     * @param grid the surface grid used for coordinate conversion
     */
    public PlanetWindGrid(PlanetSurfaceGrid grid) {
        this.grid = grid;
        this.windMap = new HashMap<>();
    }

    /**
     * Fills the entire grid with a constant wind vector.
     * Useful for initial testing or uniform conditions.
     *
     * @param wind the wind vector to assign to every grid cell
     */
    public void generateConstantWind(Vector3D wind) {
        int range = 100; // grid covers from -100 to +100 in both directions

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
     * Returns the wind vector at a given global 3D position.
     * Converts the position to a grid coordinate and looks up the wind vector.
     *
     * @param globalPosition the global 3D position in space
     * @return the wind vector at that position, or zero vector if outside the grid
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
     * Manually sets a wind vector at a specific grid coordinate.
     *
     * @param key  the grid coordinate
     * @param wind the wind vector to set
     */
    public void setWind(CoordinateKey key, Vector3D wind) {
        windMap.put(key, wind);
    }
}
