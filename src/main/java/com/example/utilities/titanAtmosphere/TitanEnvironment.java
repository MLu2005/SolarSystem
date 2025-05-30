package com.example.utilities.titanAtmosphere;

import com.example.solarSystem.Vector3D;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetWindGrid;

/**
 * TitanEnvironment provides access to key environmental factors on or near Titan's surface.
 *
 * This class is a façade that wraps:
 *
 *   {@link PlanetHeightGrid} – for terrain elevation data
 *   {@link PlanetWindGrid} – for spatial wind vector data
 *
 *
 * Role:
 * TitanEnvironment abstracts environmental conditions for any physical simulation
 * involving Titan, such as:
 *
 * computing atmospheric drag,
 * evaluating landing altitude and safety
 * generating terrain-aware AI decisions.
 *
 * It divides AtmosphericForce/PhysicsEngine from knowing
 * how the environmental data is structured or generated. It can be extended to
 * include temperature, density, visibility, etc.
 */
public class TitanEnvironment {

    /** Grid of terrain elevations (height above Titan’s center) */
    private final PlanetHeightGrid heightGrid;

    /** Grid of local wind vectors at various surface points */
    private final PlanetWindGrid windGrid;

    /**
     * Constructs a TitanEnvironment from given height and wind data grids.
     *
     * @param heightGrid grid that provides local terrain elevation
     * @param windGrid grid that provides spatial wind vectors
     */
    public TitanEnvironment(PlanetHeightGrid heightGrid, PlanetWindGrid windGrid) {
        this.heightGrid = heightGrid;
        this.windGrid = windGrid;
    }

    /**
     * Returns the terrain height (in meters) at a given global 3D position.
     * Typically used to compute how high a spaceship is above the surface.
     *
     * @param globalPosition the global position (Vector3D) of an object
     * @return the surface height at that location
     */
    public double getAltitude(Vector3D globalPosition) {
        return heightGrid.getAltitude(globalPosition);
    }

    /**
     * Returns the atmospheric wind vector at a given global 3D position.
     * Typically used to compute the relative velocity for aerodynamic drag.
     *
     * @param globalPosition the global position (Vector3D) of an object
     * @return the wind vector at that location
     */
    public Vector3D getWind(Vector3D globalPosition) {
        return windGrid.getWind(globalPosition);
    }

    /**
     * Optional getter for direct access to the height grid.
     * Useful for debugging or extending behavior.
     *
     * @return the underlying PlanetHeightGrid
     */
    public PlanetHeightGrid getHeightGrid() {
        return heightGrid;
    }

    /**
     * Optional getter for direct access to the wind grid.
     * Useful for debugging or extending behavior.
     *
     * @return the underlying PlanetWindGrid
     */
    public PlanetWindGrid getWindGrid() {
        return windGrid;
    }
}
