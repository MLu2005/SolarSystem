package com.example.solarSystem.Physics;

import com.example.solarSystem.SolarSystemODE;
import com.example.solarSystem.Vector3D;
import com.example.solarSystem.CelestialBody;
import executables.solvers.RK4Solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * PhysicsEngine handles the gravitational interactions between celestial bodies
 * and updates their positions and velocities using a basic integration step.
 */
public class PhysicsEngine {

    private static final double G = 6.6743E-20; // Gravitational constant in km^3 kg^-1 s^-2
    private final List<CelestialBody> bodies = new ArrayList<>();
    private double t = 0.0;

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
     * Advances the simulation with the rk4 step function
     */
    public void step(double dt) {
        RK4Solver rk4 = new RK4Solver();
        int n = bodies.size();

        double[] y0 = new double[n * 6];
        for (int i = 0; i < n; i++) {
            CelestialBody b = bodies.get(i);
            int k = i * 6;
            Vector3D p = b.getPosition();
            Vector3D v = b.getVelocity();
            y0[k] = p.getX();
            y0[k+ 1] = p.getY();
            y0[k+2] = p.getZ();
            y0[k +3] = v.getX();
            y0[k+ 4] = v.getY();
            y0[k+5] = v.getZ();
        }

        BiFunction<Double, double[], double[]> f =
                SolarSystemODE.generateODE(bodies);

        double[] y1 = rk4.solveStep(f, t, y0, dt);
        t += dt;

        for (int i = 0; i < n; i++) {
            CelestialBody b = bodies.get(i);
            int k = i * 6;
            b.getPosition().x = y1[k];
            b.getPosition().y = y1[k+1];
            b.getPosition().z = y1[k+2];
            b.getVelocity().x = y1[k+3];
            b.getVelocity().y = y1[k+4];
            b.getVelocity().z = y1[k+ 5];
        }
    }
}
