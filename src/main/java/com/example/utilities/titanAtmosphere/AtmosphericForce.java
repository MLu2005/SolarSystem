package com.example.utilities.titanAtmosphere;

import com.example.utilities.Vector3D;
import com.example.utilities.Ship.SpaceShip;

/**
 * AtmosphericForce models aerodynamic drag acting on a spacecraft
 * moving through Titan's atmosphere. The force is calculated
 * based on the ship's velocity relative to the local wind field
 * and is only applied if the ship is below a certain atmospheric altitude.
 *
 * Physics:The drag force is computed using:
 *
 *     Fdrag = -k * |v_rel|^2 * normalize(v_rel)
 *
 * where:
 * - k is the dragCoefficient (how streamlined our ship is)
 * - v_rel is the relative velocity to the wind
 *
 * Atmospheric drag is ignored if the ship is above maxAtmosphereAltitude.
 */
public class AtmosphericForce {

    /** Environment containing wind and terrain data. */
    private final TitanEnvironment environment;

    /** Aerodynamic drag coefficient (k). */
    private final double dragCoefficient;

    /** Maximum altitude at which atmosphere applies. Above this, drag = 0. */
    private final double maxAtmosphereAltitude;

    /**
     * Constructs an AtmosphericForce model using the given environment and parameters.
     *
     * @param environment            Titan environment with terrain/wind data
     * @param dragCoefficient        aerodynamic drag constant (0.001)
     * @param maxAtmosphereAltitude  max height where drag is active (70000 meters)
     */
    public AtmosphericForce(TitanEnvironment environment, double dragCoefficient, double maxAtmosphereAltitude) {
        this.environment = environment;
        this.dragCoefficient = dragCoefficient;
        this.maxAtmosphereAltitude = maxAtmosphereAltitude;
    }

    /**
     * Computes the aerodynamic drag force acting on the spaceship at its current position.
     * This depends on local wind, relative velocity, and height above terrain.
     *
     * @param ship the spaceship whose motion is being simulated
     * @return the drag force vector; zero if above atmospheric limit or at rest relative to wind
     */
    public Vector3D compute(SpaceShip ship) {
        Vector3D velocity = ship.getVelocity();
        Vector3D position = ship.getPosition();

        // 1. Calculate current height above terrain
        double surfaceHeight = environment.getAltitude(position);
        double altitude = position.getY() - surfaceHeight;

        // 2. Ignore drag if above atmospheric boundary
        if (altitude > maxAtmosphereAltitude) {
            return Vector3D.zero();
        }

        // 3. Get wind vector at current location
        Vector3D wind = environment.getWind(position);

        // 4. Compute velocity relative to wind
        Vector3D relativeVelocity = velocity.subtract(wind);
        double speed = relativeVelocity.magnitude();

        // 5. If not moving, no drag
        if (speed == 0) {
            return Vector3D.zero();
        }

        // 6. Apply drag formula: F = -k * v^2 * direction
        Vector3D direction = relativeVelocity.normalize();
        double forceMagnitude = -dragCoefficient * speed * speed;

        return direction.scale(forceMagnitude);
    }
}
