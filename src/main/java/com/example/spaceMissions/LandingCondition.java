package com.example.spaceMissions;

import com.example.utilities.titanAtmosphere.TitanEnvironment;
import com.example.utilities.Vector3D;

/**
 * LandingCondition determines whether a spaceship has safely landed on Titan.
 *
 * A landing is considered successful if:
 *
 * The spaceship is very close to the surface (within a small altitude tolerance),
 * Its speed is low enough not to damage it or bounce back (below maxLandingSpeed).
 *
 * Altitude is calculated as the vertical (Y-axis) distance between the ship's position
 * and the terrain surface at that point (from TitanEnvironment). The speed is based on
 * the full 3D velocity vector magnitude.
 *
 * Typical values:
 *
 * altitudeTolerance: ~1 meter (acceptable offset from surface)
 * maxLandingSpeed: ~1–5 m/s (depending on landing gear and realism)
 *
 */
public class LandingCondition {

    /** The planetary environment (height and wind) used for evaluating landing location. */
    private final TitanEnvironment environment;

    /** Maximum speed (in m/s) allowed for a safe landing. */
    private final double maxLandingSpeed;

    /** Maximum altitude (in meters) considered as surface contact. */
    private final double altitudeTolerance;

    /**
     * Constructs a LandingCondition with given parameters.
     *
     * @param environment        the TitanEnvironment with terrain height data
     * @param maxLandingSpeed    maximum allowed speed at contact (e.g., 5 m/s)
     * @param altitudeTolerance  tolerance above terrain to accept as landed (e.g., 1 m)
     */
    public LandingCondition(TitanEnvironment environment, double maxLandingSpeed, double altitudeTolerance) {
        this.environment = environment;
        this.maxLandingSpeed = maxLandingSpeed;
        this.altitudeTolerance = altitudeTolerance;
    }

    /**
     * Checks whether the spaceship is in a safe landing state:
     * - it is within altitude tolerance from the surface,
     * - and its velocity is under the maximum allowed speed.
     *
     * @param ship the ship whose position and velocity are being evaluated
     * @return true if both criteria are met, otherwise false
     */
    public boolean isSafeLanding(SpaceShip ship) {
        Vector3D position = ship.getPosition();
        Vector3D velocity = ship.getVelocity();

        double surfaceHeight = environment.getAltitude(position);
        double altitudeAboveGround = position.getY() - surfaceHeight;
        double speed = velocity.magnitude();

        return altitudeAboveGround <= altitudeTolerance && speed <= maxLandingSpeed;
    }
}
