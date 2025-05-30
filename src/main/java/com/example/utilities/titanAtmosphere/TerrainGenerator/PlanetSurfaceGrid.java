package com.example.utilities.titanAtmosphere.TerrainGenerator;

import com.example.utilities.Vector3D;
import com.example.solar_system.CelestialBody;
import com.example.utilities.titanAtmosphere.CoordinateKey;

/**
 * PlanetSurfaceGrid is responsible for converting a 3D global position
 * (typically the location of a spacecraft) into a discrete 2D grid coordinate
 * (CoordinateKey) fixed to a planet's surface.
 *
 * The grid lies in the XZ-plane, centered at the planet's global position.
 * The Y-axis (altitude) is ignored when determining surface location.
 *
 * This grid serves as the basis for mapping terrain height and wind fields
 * relative to the moving celestial body.
 */
public class PlanetSurfaceGrid {

    /** The planet to which this surface grid is attached. */
    private final CelestialBody planet;

    /** Size of each grid cell in meters (10000 for 10 km cells). */
    private final double cellSize;

    /**
     * Constructs a surface grid anchored to a given celestial body.
     *
     * @param planet   the planet serving as the center reference
     * @param cellSize the size of a single grid cell in meters
     */
    public PlanetSurfaceGrid(CelestialBody planet, double cellSize) {
        this.planet = planet;
        this.cellSize = cellSize;
    }

    /**
     * Converts a 3D global position to a CoordinateKey (row, col)
     * that represents a grid cell on the planet's surface.
     *
     * This method uses only the X and Z components of the vector
     * and calculates the offset from the planet's center.
     *
     * @param globalPosition position in the global reference frame
     * @return corresponding CoordinateKey on the surface grid
     */
    public CoordinateKey toCoordinateKey(Vector3D globalPosition) {
        //Get planet's current global position
        Vector3D planetCenter = planet.getPosition();

        //Convert to local position relative to the planet
        Vector3D local = globalPosition.subtract(planetCenter);

        //Use x and z (ignore y/altitude) to compute grid indices
        int col = (int) Math.floor(local.getX() / cellSize);
        int row = (int) Math.floor(local.getZ() / cellSize);

        return new CoordinateKey(row, col);
    }

    /**
     * Converts a global position into local coordinates relative to the planet's center.
     *
     * @param globalPosition global 3D position in space
     * @return position relative to the planet's center
     */
    public Vector3D toLocalPosition(Vector3D globalPosition) {
        return globalPosition.subtract(planet.getPosition());
    }
}
