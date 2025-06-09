package com.example.solar_system;

import com.example.utilities.Vector3D;
import java.util.List;

/*
 Highly elaborate description of the class below ->>
 */
/*
 * BurnManager handles automatic engine burns for the spacecraft across different flight phases:
 * APPROACH, ORBIT_INSERTION, LANDING, and COMPLETE.
 *
 * --- How does a burn work here? ---
 * The system watches the distance and velocity between the spacecraft and its target body (Titan).
 * Based on the current flight phase, it calculates a change in velocity (delta-V) and applies that to
 * the spacecraft, nudging it toward an ideal approach, orbit, or descent trajectory.
 *
 * --- How it affects the ship ---
 * - In the APPROACH phase, the ship adjusts its velocity to match an ideal approach speed and direction.
 * - In the ORBIT_INSERTION phase, it shapes the ship's orbit around the target and slowly reduces altitude.
 * - In the LANDING phase, the ship descends directly, applying constant burns to reduce speed and height.
 * - Once within the final landing threshold, the COMPLETE phase locks the ship’s final trajectory and stops all burns.
 *
 * Each burn is limited in to simulate more realistic control and to prevent overcorrection.
 *
 * 13,000km start steering and approach titan - 5,000km start landing - 1,500 km start landerVisualizer.
 */


/**
 * Manages the spaceship's automated burn phases during approach, orbit insertion, and landing.
 */
public class BurnManager {

    // * Represents the phases through which the ark goes through.
    private enum Phase { APPROACH, ORBIT_INSERTION, LANDING, COMPLETE }

    private Phase currentPhase = Phase.APPROACH;

    private final double targetOrbitMultiplier;
    private final String targetBodyName;
    private final double approachTriggerDistance;
    private final double orbitTriggerDistance;
    private final Vector3D targetVelocityVector;

    private final double landingStartDistance = 5000;
    private final double landingStopDistance = 1500;

    /**
     * Constructs a BurnManager to control a spacecraft's velocity relative to a target body.
     *
     * @param targetOrbitMultiplier    Multiplier for desired orbital speed.
     * @param targetBodyName           Name of the target celestial body.
     * @param approachTriggerDistance  Distance to begin velocity matching (km).
     * @param orbitTriggerDistance     Distance to start orbital maneuvers (km).
     * @param targetVelocityVector     Ideal velocity vector to match during approach.
     */
    public BurnManager(double targetOrbitMultiplier,
                       String targetBodyName,
                       double approachTriggerDistance,
                       double orbitTriggerDistance,
                       Vector3D targetVelocityVector) {
        this.targetOrbitMultiplier = targetOrbitMultiplier;
        this.targetBodyName = targetBodyName;
        this.approachTriggerDistance = approachTriggerDistance;
        this.orbitTriggerDistance = orbitTriggerDistance;
        this.targetVelocityVector = targetVelocityVector;
    }

    /**
     * Attempts to apply a burn to the spacecraft based on its distance to the target body.
     *
     * @param bodies        List of all celestial bodies in the simulation.
     * @param spaceshipName The name of the spacecraft to control.
     * @param distanceKm    The current distance to the target body in kilometers.
     */
    public void tryApplyBurn(List<CelestialBody> bodies, String spaceshipName, double distanceKm) {
        CelestialBody ship = findBody(bodies, spaceshipName);
        CelestialBody target = findBody(bodies, targetBodyName);
        if (ship == null || target == null) return;

        Vector3D rVec = ship.getPosition().subtract(target.getPosition());
        Vector3D vRel = ship.getVelocity().subtract(target.getVelocity());
        double vMag = vRel.magnitude();

        System.out.printf("Distance to %s: %.2f km | Phase: %s%n", targetBodyName, distanceKm, currentPhase);

        switch (currentPhase) {

            // * We try to match the speed of the probe to that of titan if its higher we apply burning to reduce it. and so on.
            case APPROACH:
                if (distanceKm <= approachTriggerDistance) {
                    // * Calculates the error between current and target relative velocity
                    Vector3D velocityError = targetVelocityVector.subtract(vRel);
                    double errorMag = velocityError.magnitude();

                    if (errorMag > 0.5) {
                        Vector3D deltaV = limitDeltaV(velocityError, 1.0);
                        ship.setVelocity(ship.getVelocity().add(deltaV));
                        System.out.println("APPROACH: Adjusting to match ideal velocity.");
                        System.out.println("Δv = " + deltaV + " km/s");
                    } else {
                        Vector3D predictedPos = target.getPosition().add(target.getVelocity().scale(600));
                        Vector3D steering = predictedPos.subtract(ship.getPosition()).normalize().scale(0.5);
                        ship.setVelocity(ship.getVelocity().add(steering));

                        System.out.println("APPROACH: Steering burn (0.5 km/s) toward predicted position.");
                        System.out.println("Δv (steering) = " + steering + " km/s");

                        currentPhase = Phase.ORBIT_INSERTION;
                        System.out.println("Switching to ORBIT_INSERTION phase.");
                    }
                }
                break;

            /*
             * This phase is responsible for inserting the spacecraft into a stable orbit
             * around the target body once it has approached within a trigger distance.
             *
             * The burn strategy adapts based on proximity:
             * - If within landingStartDistance, the phase transitions to LANDING.
             * - If within orbitTriggerDistance, the system attempts to circularize the orbit
             *   by calculating the ideal orbital speed and applying a progress burn along the orbital plane.
             * - If still further away, a retrograde burn is combined with steering toward the target
             *   to slow down and guide the spacecraft closer.
             *
             * The orbital insertion relies on:
             * - Gravitational parameter of the target for orbital speed calculation
             * - Cross products to compute angular momentum and burn direction
             * - Controlled delta-V applications using limitDeltaV to shape the orbit safely
             *
             * Logging is included to show when burns are applied and when phase transitions occur.
             */
            case ORBIT_INSERTION:
                if (distanceKm <= landingStartDistance) {
                    // * If close enough, transition to landing phase
                    System.out.printf("Within %.0f km. Switching to LANDING phase.%n", landingStartDistance);
                    currentPhase = Phase.LANDING;
                    return;
                }

                if (distanceKm < orbitTriggerDistance) {
                    double mu = target.getGravitationalParameter();
                    double desiredSpeed = Math.sqrt(mu / (distanceKm * 1000)) * targetOrbitMultiplier;

                    Vector3D angularMomentum = rVec.cross(vRel);
                    Vector3D burnDirection = angularMomentum.cross(rVec).normalize();

                    if (burnDirection.magnitude() == 0) {
                        System.out.println("ORBIT_INSERTION: Cannot compute orbit direction.");
                        return;
                    }

                    Vector3D desiredVelocity = burnDirection.scale(desiredSpeed);
                    Vector3D deltaV = desiredVelocity.subtract(vRel);

                    Vector3D inward = rVec.normalize().scale(-0.10);
                    deltaV = deltaV.add(inward);

                    Vector3D limitedDeltaV = limitDeltaV(deltaV, 1.0);
                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));

                    System.out.println("ORBIT INSERTION: Shaping orbit and reducing altitude.");
                    System.out.println("Δv = " + limitedDeltaV + " km/s");
                } else {
                    Vector3D retro = vRel.normalize().scale(-0.5 * vMag);
                    Vector3D towardTarget = rVec.normalize().scale(-6.5);
                    Vector3D deltaV = retro.add(towardTarget);
                    Vector3D limitedDeltaV = limitDeltaV(deltaV, 2.5);

                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));
                    System.out.println("ORBIT_INSERTION: Half retro + minor steering toward target.");
                    System.out.println("Δv = " + limitedDeltaV + " km/s");
                }
                break;

            case LANDING:
                if (distanceKm <= landingStopDistance) {
                    System.out.println("Reached 1500 km. Landing complete. COMPLETE!");
                    currentPhase = Phase.COMPLETE;
                } else {
                    Vector3D descentVector = rVec.normalize().scale(-0.3);
                    Vector3D limitedDeltaV = limitDeltaV(descentVector, 1.0);
                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));

                    System.out.printf("⬇LANDING: Descending... Distance = %.2f km%n", distanceKm);
                    System.out.println("Δv = " + limitedDeltaV + " km/s");
                }
                break;

            case COMPLETE:
                System.out.println("COMPLETE! Holding final trajectory.");
                break;
        }
    }

    /**
     * Limits the magnitude of a velocity change vector to avoid overcorrection.
     *
     * @param deltaV       The raw delta-V vector.
     * @param maxMagnitude The maximum allowed magnitude.
     * @return A scaled version of the vector if it exceeds the limit, otherwise unchanged.
     */
    private Vector3D limitDeltaV(Vector3D deltaV, double maxMagnitude) {
        double mag = deltaV.magnitude();
        return mag > maxMagnitude ? deltaV.normalize().scale(maxMagnitude) : deltaV;
    }

    /**
     * Checks if the burn sequence has finished.
     *
     * @return true if the spacecraft is in the COMPLETE phase.
     */
    public boolean isComplete() {
        return currentPhase == Phase.COMPLETE;
    }

    /**
     * Finds a celestial body in the list by name.
     *
     * @param bodies The list of celestial bodies.
     * @param name   The name to match.
     * @return The matching body or null if not found.
     */
    private CelestialBody findBody(List<CelestialBody> bodies, String name) {
        return bodies.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
