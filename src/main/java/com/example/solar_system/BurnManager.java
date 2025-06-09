package com.example.solar_system;

import com.example.utilities.Vector3D;
import java.util.List;

public class BurnManager {

    private enum Phase { APPROACH, ORBIT_INSERTION, LANDING, COMPLETE }

    private Phase currentPhase = Phase.APPROACH;

    private final double targetOrbitMultiplier;
    private final String targetBodyName;
    private final double approachTriggerDistance;
    private final double orbitTriggerDistance;
    private final Vector3D targetVelocityVector;

    private final double landingStartDistance = 5000; // km
    private final double landingStopDistance = 1500;  // km

    private double previousAngle = Double.NaN;
    private int orbitCount = 0;

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

    public void tryApplyBurn(List<CelestialBody> bodies, String spaceshipName, double distanceKm) {
        CelestialBody ship = findBody(bodies, spaceshipName);
        CelestialBody target = findBody(bodies, targetBodyName);
        if (ship == null || target == null) return;

        Vector3D rVec = ship.getPosition().subtract(target.getPosition());
        Vector3D vRel = ship.getVelocity().subtract(target.getVelocity());
        double vMag = vRel.magnitude();

        System.out.printf("🛰️ Distance to %s: %.2f km | Phase: %s%n", targetBodyName, distanceKm, currentPhase);
        System.out.printf("Velocity magnitude (relative): %.6f km/s%n", vMag);

        switch (currentPhase) {
            case APPROACH:
                if (distanceKm <= approachTriggerDistance) {
                    Vector3D velocityError = targetVelocityVector.subtract(vRel);
                    double errorMag = velocityError.magnitude();

                    if (errorMag > 0.5) {
                        Vector3D deltaV = limitDeltaV(velocityError, 1.0);
                        ship.setVelocity(ship.getVelocity().add(deltaV));
                        System.out.println("🛬 APPROACH: Adjusting to match ideal velocity.");
                        System.out.println("Δv = " + deltaV + " km/s");
                    } else {
                        Vector3D predictedPos = target.getPosition().add(target.getVelocity().scale(600));
                        Vector3D steering = predictedPos.subtract(ship.getPosition()).normalize().scale(0.5);
                        ship.setVelocity(ship.getVelocity().add(steering));

                        System.out.println("🧭 APPROACH: Steering burn (0.5 km/s) toward predicted position.");
                        System.out.println("Δv (steering) = " + steering + " km/s");

                        currentPhase = Phase.ORBIT_INSERTION;
                        System.out.println("🔄 Switching to ORBIT_INSERTION phase.");
                    }
                }
                break;

            case ORBIT_INSERTION:
                if (distanceKm <= landingStartDistance) {
                    System.out.printf("🛬 Within %.0f km. Switching to LANDING phase.%n", landingStartDistance);
                    currentPhase = Phase.LANDING;
                    return;
                }

                if (distanceKm < orbitTriggerDistance) {
                    double mu = target.getGravitationalParameter();
                    double desiredSpeed = Math.sqrt(mu / (distanceKm * 1000)) * targetOrbitMultiplier;

                    Vector3D angularMomentum = rVec.cross(vRel);
                    Vector3D burnDirection = angularMomentum.cross(rVec).normalize();

                    if (burnDirection.magnitude() == 0) {
                        System.out.println("⚠️ ORBIT_INSERTION: Cannot compute orbit direction.");
                        return;
                    }

                    Vector3D desiredVelocity = burnDirection.scale(desiredSpeed);
                    Vector3D deltaV = desiredVelocity.subtract(vRel);

                    Vector3D inward = rVec.normalize().scale(-0.10); // ⚠️ 3× descent rate (was -0.05)
                    deltaV = deltaV.add(inward);

                    Vector3D limitedDeltaV = limitDeltaV(deltaV, 1.0);
                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));

                    System.out.println("🚀 ORBIT INSERTION: Shaping orbit and reducing altitude (3× rate).");
                    System.out.println("Δv = " + limitedDeltaV + " km/s");
                } else {
                    Vector3D retro = vRel.normalize().scale(-0.5 * vMag);
                    Vector3D towardTarget = rVec.normalize().scale(-6.5);
                    Vector3D deltaV = retro.add(towardTarget);
                    Vector3D limitedDeltaV = limitDeltaV(deltaV, 2.5);

                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));
                    System.out.println("🔄 ORBIT_INSERTION: Half retro + minor steering toward target.");
                    System.out.println("Δv = " + limitedDeltaV + " km/s");
                }
                break;

            case LANDING:
                if (distanceKm <= landingStopDistance) {
                    System.out.println("✅ Reached 1500 km. Landing complete. ✅ COMPLETE!");
                    currentPhase = Phase.COMPLETE;
                } else {
                    Vector3D descentVector = rVec.normalize().scale(-0.3);
                    Vector3D limitedDeltaV = limitDeltaV(descentVector, 1.0);
                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));

                    System.out.printf("⬇️ LANDING: Descending... Distance = %.2f km%n", distanceKm);
                    System.out.println("Δv = " + limitedDeltaV + " km/s");
                }
                break;

            case COMPLETE:
                System.out.println("✅ COMPLETE! Holding final trajectory.");
                break;
        }
    }

    private Vector3D limitDeltaV(Vector3D deltaV, double maxMagnitude) {
        double mag = deltaV.magnitude();
        return mag > maxMagnitude ? deltaV.normalize().scale(maxMagnitude) : deltaV;
    }

    public boolean isComplete() {
        return currentPhase == Phase.COMPLETE;
    }

    private CelestialBody findBody(List<CelestialBody> bodies, String name) {
        return bodies.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
