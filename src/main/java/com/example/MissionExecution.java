package com.example;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import java.io.File;
import java.io.IOException;
import java.util.List;

// * Reads ths JSON file. see line 212 below.
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Mission execution handles automated spacecraft maneuvering through distinct flight phases:
 * APPROACH, ORBIT_INSERTION, LANDING, and COMPLETE.
 *
 * This manager dynamically modifies the spacecraft’s velocity in real-time, guiding it
 * toward a stable orbit and safe landing. It supports both reactive steering logic and
 * precomputed burns sourced from an external JSON file (from a hill-climbing optimizer).
 *
 * Each phase applies different burn strategies based on current distance and velocity relative
 * to the target body.
 *
 * PHASES:
 * - APPROACH: Begins once the spacecraft is within the approach trigger distance. The craft attempts
 *   to match the target’s velocity vector using minor corrections. Once aligned, it steers toward a
 *   predicted position ahead of the target’s path.
 *
 * - ORBIT_INSERTION: Engages when the spacecraft enters orbital range. It computes the ideal
 *   orbital velocity using the gravitational parameter of the target and applies burns tangentially
 *   to circularize the orbit while maintaining altitude control. Retrograde and steering components
 *   are added to slow down and fine-tune the orbit.
 *
 * - LANDING: Once within a specified low-altitude threshold, the spacecraft enters direct descent mode.
 *   It applies constant downward burns to reduce both altitude and velocity. The descent vector is computed
 *   based on the spacecraft's current relative position to the target.
 *
 * - COMPLETE: Reached when the ship is below the final landing distance. All automated burns are stopped,
 *   locking the ship’s final trajectory.
 *
 * Features:
 * - Uses real-time physics via position and velocity deltas.
 * - Controls each burn’s magnitude to prevent excessive corrections.
 * - Logs transition events and key burn vectors to the console for visibility.
 *
 * The distances:
 * - 13,000 km: Begin steering and approach
 * - 5,000 km: Begin landing descent
 * - 1,500 km: Landing phase complete
 */

public class MissionExecution {

    public boolean isComplete() {
        return currentPhase == Phase.COMPLETE;
    }

    private enum Phase { APPROACH, ORBIT_INSERTION, LANDING, COMPLETE }

    private Phase currentPhase = Phase.APPROACH;

    private final String targetBodyName;
    private final double approachTriggerDistance;
    private final double orbitTriggerDistance;
    private final double landingStartDistance = 5000;
    private final double landingStopDistance = 1500;
    private final double targetOrbitMultiplier;

    private final Vector3D targetVelocityVector;

    private List<Burn> hillClimbBurns;  // from JSON, time-ordered burns
    private int nextBurnIndex = 0;

    public MissionExecution(
            String targetBodyName,
            double approachTriggerDistance,
            double orbitTriggerDistance,
            double targetOrbitMultiplier,
            Vector3D targetVelocityVector
    ) {
        this.targetBodyName = targetBodyName;
        this.approachTriggerDistance = approachTriggerDistance;
        this.orbitTriggerDistance = orbitTriggerDistance;
        this.targetOrbitMultiplier = targetOrbitMultiplier;
        this.targetVelocityVector = targetVelocityVector;

        loadHillClimbBurns();
    }

    /**
     * Tries to apply burns dynamically and from JSON to steer the ship toward orbit insertion.
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
            case APPROACH:
                if (distanceKm <= approachTriggerDistance) {
                    Vector3D velocityError = targetVelocityVector.subtract(vRel);
                    double errorMag = velocityError.magnitude();

                    if (errorMag > 0.5) {
                        Vector3D deltaV = limitDeltaV(velocityError, 1.0);
                        ship.setVelocity(ship.getVelocity().add(deltaV));
                        System.out.println("APPROACH: Adjusting velocity to match target.");
                        System.out.println("v = " + deltaV + " km/s");
                    } else {
                        Vector3D predictedPos = target.getPosition().add(target.getVelocity().scale(600));
                        Vector3D steering = predictedPos.subtract(ship.getPosition()).normalize().scale(0.5);
                        ship.setVelocity(ship.getVelocity().add(steering));

                        System.out.println("APPROACH: Steering toward predicted target position.");
                        System.out.println("v (steering) = " + steering + " km/s");

                        currentPhase = Phase.ORBIT_INSERTION;
                        System.out.println("Switching to ORBIT_INSERTION phase.");
                    }
                }
                break;

            case ORBIT_INSERTION:
                if (distanceKm <= landingStartDistance) {
                    System.out.printf("Within %.0f km. Switching to LANDING phase.%n", landingStartDistance);
                    currentPhase = Phase.LANDING;
                    return;
                }

                // * First apply next JSON burn if available
                if (hillClimbBurns != null && nextBurnIndex < hillClimbBurns.size()) {
                    Burn burn = hillClimbBurns.get(nextBurnIndex);
                    nextBurnIndex++;
                    ship.setVelocity(ship.getVelocity().add(burn.deltaV));
                    System.out.println("Applying JSON burn v: " + burn.deltaV + " km/s");
                }

                if (distanceKm < orbitTriggerDistance) {
                    // * Calculate ideal orbital velocity for circular orbit
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

                    System.out.println("ORBIT_INSERTION: Orbit shaping burn.");
                    System.out.println("v = " + limitedDeltaV + " km/s");

                } else {
                    Vector3D retro = vRel.normalize().scale(-0.5 * vMag);
                    Vector3D towardTarget = rVec.normalize().scale(-6.5);
                    Vector3D deltaV = retro.add(towardTarget);
                    Vector3D limitedDeltaV = limitDeltaV(deltaV, 2.5);

                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));
                    System.out.println("ORBIT_INSERTION: Retro + steering burn.");
                    System.out.println("v = " + limitedDeltaV + " km/s");
                }
                break;

            case LANDING:
                if (distanceKm <= landingStopDistance) {
                    System.out.println("Reached landing stop distance. COMPLETE!");
                    currentPhase = Phase.COMPLETE;
                } else {
                    Vector3D descentVector = rVec.normalize().scale(-0.3);
                    Vector3D limitedDeltaV = limitDeltaV(descentVector, 1.0);
                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));

                    System.out.printf("LANDING: Descending... Distance = %.2f km%n", distanceKm);
                    System.out.println("v = " + limitedDeltaV + " km/s");
                }
                break;

            case COMPLETE:
                System.out.println("COMPLETE: Final trajectory locked.");
                break;
        }
    }

    private Vector3D limitDeltaV(Vector3D deltaV, double maxMagnitude) {
        double mag = deltaV.magnitude();
        return mag > maxMagnitude ? deltaV.normalize().scale(maxMagnitude) : deltaV;
    }

    private CelestialBody findBody(List<CelestialBody> bodies, String name) {
        return bodies.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private void loadHillClimbBurns() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            HillClimbResult result = mapper.readValue(
                    new File("src/main/java/com/example/utilities/HillClimb/hillclimb_results.json"),
                    HillClimbResult.class
            );
            hillClimbBurns = result.burns;
            scaleDownHillClimbBurns();
            assert hillClimbBurns != null;
            System.out.println("Loaded " + hillClimbBurns.size() + " hillclimb burns.");
        } catch (IOException e) {
            System.err.println("Failed to load hillclimb burns: " + e.getMessage());
            hillClimbBurns = List.of(); // fallback empty list
        }
    }

    private void scaleDownHillClimbBurns() {
        final double scaleFactor = 0.001; // Convert from m/s to km/s
        for (Burn burn : hillClimbBurns) {
            burn.deltaV = burn.deltaV.scale(scaleFactor);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HillClimbResult {
        public List<Burn> burns;
    }

    public static class Burn {
        public double time;
        public Vector3D deltaV;

        public Burn() {}

        public Burn(double time, Vector3D deltaV) {
            this.time = time;
            this.deltaV = deltaV;
        }

        @Override
        public String toString() {
            return "Burn{time=" + time + ", deltaV=" + deltaV + '}';
        }
    }
}
