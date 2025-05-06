package com.example.solarSystem.Physics;

import com.example.solarSystem.Vector3D;
import com.example.solarSystem.CelestialBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PhysicsEngine handles the gravitational interactions between celestial bodies
 * and updates their positions and velocities using a basic integration step.
 */
public class PhysicsEngine {

    private static final double G = 6.6743E-20; // Gravitational constant in km^3 kg^-1 s^-2
    private final List<CelestialBody> bodies = new ArrayList<>();

    public void addBody(CelestialBody body) {
        bodies.add(body);
    }

    public List<CelestialBody> getBodies() {
        return bodies;
    }

    /**
     * Computes the gravitational force exerted on 'a' by 'b'.
     */
    private Vector3D computeGravitationalForce(CelestialBody a, CelestialBody b) {
        Vector3D r = b.getPosition().subtract(a.getPosition());
        double distance = r.magnitude() + 1e-10; // avoid division by zero
        double forceMagnitude = G * a.getMass() * b.getMass() / (distance * distance);
        return r.normalize().scale(forceMagnitude);
    }

    /**
     * Advances the simulation by time step dt using a simple Verlet-like integration.
     */
    public void step(double dt) {
        // First: update positions based on current velocity and acceleration
        for (CelestialBody body : bodies) {
            Vector3D displacement = body.getVelocity().scale(dt)
                    .add(body.getAcceleration().scale(0.5 * dt * dt));
            body.setPosition(body.getPosition().add(displacement));
        }

        // Second: compute new forces (and thus new accelerations)
        Map<CelestialBody, Vector3D> newForces = new HashMap<>();
        for (CelestialBody body : bodies) {
            newForces.put(body, Vector3D.zero());
        }

        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody a = bodies.get(i);
            for (int j = 0; j < bodies.size(); j++) {
                if (i != j) {
                    CelestialBody b = bodies.get(j);
                    Vector3D force = computeGravitationalForce(a, b);
                    newForces.put(a, newForces.get(a).add(force));
                }
            }
        }

        // Third: update velocities using average acceleration
        for (CelestialBody body : bodies) {
            Vector3D force = newForces.get(body);
            Vector3D newAccel = force.scale(1.0 / body.getMass());
            Vector3D avgAccel = body.getAcceleration().add(newAccel).scale(0.5);
            body.setVelocity(body.getVelocity().add(avgAccel.scale(dt)));
            body.setAcceleration(newAccel);
        }
    }
}
