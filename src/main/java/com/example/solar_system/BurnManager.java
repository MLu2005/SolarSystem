package com.example.solar_system;

import com.example.utilities.Vector3D;

import java.util.List;

public class BurnManager {

    private enum Phase { APPROACH, ORBIT_INSERTION, COMPLETE }

    private Phase currentPhase = Phase.APPROACH;
    private final double targetOrbitMultiplier;
    private final String targetBodyName;
    private final double approachTriggerDistance;
    private final double orbitTriggerDistance;

    public BurnManager(double targetOrbitMultiplier,
                       String targetBodyName,
                       double approachTriggerDistance,
                       double orbitTriggerDistance) {
        this.targetOrbitMultiplier = targetOrbitMultiplier;
        this.targetBodyName = targetBodyName;
        this.approachTriggerDistance = approachTriggerDistance;
        this.orbitTriggerDistance = orbitTriggerDistance;
    }

    public void tryApplyBurn(List<CelestialBody> bodies, String spaceshipName) {
        CelestialBody ship = findBody(bodies, spaceshipName);
        CelestialBody target = findBody(bodies, targetBodyName);
        if (ship == null || target == null) return;

        Vector3D rVec = ship.getPosition().subtract(target.getPosition());
        Vector3D vRel = ship.getVelocity().subtract(target.getVelocity());
        double distance = rVec.magnitude();
        double vMag = vRel.magnitude();

        System.out.printf("🛰️ Distance to %s: %.2f km | Phase: %s%n", targetBodyName, distance, currentPhase);
        System.out.printf("Velocity magnitude (relative): %.6f km/s%n", vMag);

        switch (currentPhase) {
            case APPROACH:
                if (distance <= approachTriggerDistance) {
                    if (vMag > 1.0) {
                        // Gradual velocity kill
                        Vector3D retroBurn = vRel.normalize().scale(-0.5 * vMag);
                        Vector3D limitedRetro = limitDeltaV(retroBurn, 1.0);
                        ship.setVelocity(ship.getVelocity().add(limitedRetro));
                        System.out.println("🛬 APPROACH: Gradual deceleration.");
                        System.out.println("Δv (retrograde) = " + limitedRetro + " km/s");
                    } else {
                        // Minimal relative motion, apply small steering toward predicted Titan location
                        Vector3D titanFuturePos = target.getPosition().add(target.getVelocity().scale(600)); // 600s = 10 mins
                        Vector3D towardTitan = ship.getPosition().subtract(titanFuturePos).normalize().scale(-1);
                        Vector3D steering = towardTitan.scale(0.5);
                        ship.setVelocity(ship.getVelocity().add(steering));
                        System.out.println("🧭 APPROACH: Steering burn (0.5 km/s) toward Titan’s predicted position.");
                        System.out.println("Δv (steering) = " + steering + " km/s");

                        currentPhase = Phase.ORBIT_INSERTION;
                        System.out.println("🔄 Switching to ORBIT_INSERTION phase.");
                    }
                }
                break;

            case ORBIT_INSERTION:
                if (distance < orbitTriggerDistance) {
                    double mu = target.getGravitationalParameter();
                    double desiredSpeed = Math.sqrt(mu / distance) * targetOrbitMultiplier;

                    Vector3D angularMomentum = rVec.cross(vRel);
                    Vector3D burnDirection = angularMomentum.cross(rVec).normalize();

                    if (burnDirection.magnitude() == 0) {
                        System.out.println("⚠️ ORBIT_INSERTION: Cannot compute orbit insertion direction.");
                        return;
                    }

                    Vector3D desiredVelocity = burnDirection.scale(desiredSpeed);
                    Vector3D deltaV = desiredVelocity.subtract(vRel);
                    Vector3D limitedDeltaV = limitDeltaV(deltaV, 1.0);

                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));
                    System.out.printf("🚀 FINAL ORBITAL BURN around %s%n", targetBodyName);
                    System.out.println("Δv = " + limitedDeltaV + " km/s");

                    currentPhase = Phase.COMPLETE;
                } else {
                    // Still approaching, fine-tune with partial retro-burn + minor steering
                    Vector3D retrograde = vRel.normalize().scale(-0.5 * vMag);
                    Vector3D towardTitan = rVec.normalize().scale(-1);
                    Vector3D steering = towardTitan.scale(0.2);

                    Vector3D deltaV = retrograde.add(steering);
                    Vector3D limitedDeltaV = limitDeltaV(deltaV, 1.0);
                    ship.setVelocity(ship.getVelocity().add(limitedDeltaV));

                    System.out.println("🔄 ORBIT_INSERTION: Half-speed retro + 0.2 km/s steering.");
                    System.out.println("Δv = " + limitedDeltaV + " km/s");
                }
                break;

            case COMPLETE:
                // No action required
                System.out.println("✅ Orbit insertion complete. Holding trajectory.");
                break;
        }
    }

    private Vector3D limitDeltaV(Vector3D deltaV, double maxMagnitude) {
        double mag = deltaV.magnitude();
        if (mag > maxMagnitude) {
            return deltaV.normalize().scale(maxMagnitude);
        }
        return deltaV;
    }

    private CelestialBody findBody(List<CelestialBody> bodies, String name) {
        return bodies.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

