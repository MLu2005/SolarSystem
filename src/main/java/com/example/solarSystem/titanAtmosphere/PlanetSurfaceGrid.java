package com.example.solarSystem.titanAtmosphere;


import com.example.solarSystem.Vector3D;
import com.example.solarSystem.CelestialBody;

/**
 * Class responsible for converting a global 3D position (Vector3D)
 * into a discrete grid coordinate (CoordinateKey) fixed to the surface
 * of a planet. This allows mapping environmental data (like height or wind)
 * based on the spaceship's position relative to the planet.
 */
public class PlanetSurfaceGrid {

    private final CelestialBody planet;
    private final double cellSize;

    /**
     * Constructs a grid converter for a specific planet and cell size.
     *
     * @param planet   the planet to which the grid is fixed
     * @param cellSize the size of one grid cell (e.g., 10000 meters for 10 km)
     */
    public PlanetSurfaceGrid(CelestialBody planet, double cellSize) {
        this.planet = planet;
        this.cellSize = cellSize;
    }

    /**
     * Converts a global position (for example a spaceship) into a grid coordinate
     * fixed to the surface of the planet.
     *
     * @param globalPosition position in global 3D space
     * @return corresponding grid cell coordinate
     */
    public CoordinateKey toCoordinateKey(Vector3D globalPosition) {
        // Step 1: Get planet's current global position
        Vector3D planetCenter = planet.getPosition();

        // Step 2: Convert to local position relative to the planet
        Vector3D local = globalPosition.subtract(planetCenter);

        // Step 3: Use x and z (ignoring y/altitude) to compute grid indices
        int col = (int) Math.floor(local.getX() / cellSize);
        int row = (int) Math.floor(local.getZ() / cellSize);

        return new CoordinateKey(row, col);
    }

    /**
     * (Optional) Converts a global position into local coordinates relative to the planet.
     * Can be used for debugging or analysis.
     *
     * @param globalPosition position in global 3D space
     * @return position in the local reference frame of the planet
     */
    public Vector3D toLocalPosition(Vector3D globalPosition) {
        return globalPosition.subtract(planet.getPosition());
    }
}

