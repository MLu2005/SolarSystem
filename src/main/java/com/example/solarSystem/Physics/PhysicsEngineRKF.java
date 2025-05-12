package com.example.solarSystem.Physics;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Vector3D;
import executables.solvers.ODESolver;
import executables.solvers.RKF45Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class PhysicsEngineRKF {

    private static final double G = 6.6743E-20; // Gravitational constant in km^3 kg^-1 s^-2
    private static final double SOFTENING_LENGTH = 100.0; // km
    private static final double INITIAL_STEP_SIZE = 60.0; // seconds
    private static final int MAX_STEPS = 500;

    private final List<CelestialBody> bodies = new ArrayList<>();
    private final ODESolver solver;

    public PhysicsEngineRKF() {
        this.solver = new RKF45Solver(); // still uses RKF
    }

    public void addBody(CelestialBody body) {
        bodies.add(body);
    }

    public List<CelestialBody> getBodies() {
        return bodies;
    }

    public void step(double dt) {
        if (bodies.isEmpty()) return;

        double[] state = flattenState(bodies);
        BiFunction<Double, double[], double[]> derivative = createDerivative();

        BiFunction<Double, double[], Boolean> stopCondition = (t, y) -> t >= dt;

        double[][] result = solver.solve(derivative, 0.0, state, INITIAL_STEP_SIZE, MAX_STEPS, stopCondition);

        if (result.length < 2) {
            System.err.printf(" RKF solver failed — only %d step(s) returned. Skipping update.%n", result.length);
            return;
        }

        updateBodiesFromState(result[result.length - 1]);
    }

    private BiFunction<Double, double[], double[]> createDerivative() {
        return (t, y) -> {
            int n = bodies.size();
            double[] dydt = new double[6 * n];

            Vector3D[] positions = new Vector3D[n];
            Vector3D[] velocities = new Vector3D[n];

            for (int i = 0; i < n; i++) {
                positions[i] = new Vector3D(y[6 * i], y[6 * i + 1], y[6 * i + 2]);
                velocities[i] = new Vector3D(y[6 * i + 3], y[6 * i + 4], y[6 * i + 5]);
            }

            Vector3D[] accelerations = computeAccelerations(positions);

            for (int i = 0; i < n; i++) {
                dydt[6 * i]     = velocities[i].getX();
                dydt[6 * i + 1] = velocities[i].getY();
                dydt[6 * i + 2] = velocities[i].getZ();
                dydt[6 * i + 3] = accelerations[i].getX();
                dydt[6 * i + 4] = accelerations[i].getY();
                dydt[6 * i + 5] = accelerations[i].getZ();
            }

            return dydt;
        };
    }

    private Vector3D[] computeAccelerations(Vector3D[] positions) {
        int n = bodies.size();
        Vector3D[] accelerations = new Vector3D[n];
        double softening2 = SOFTENING_LENGTH * SOFTENING_LENGTH;

        for (int i = 0; i < n; i++) {
            Vector3D totalForce = new Vector3D(0, 0, 0);
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                Vector3D r = positions[j].subtract(positions[i]);
                double r2 = r.dot(r);
                double distSqr = r2 + softening2;
                double forceMag = G * bodies.get(i).getMass() * bodies.get(j).getMass() / (distSqr * Math.sqrt(distSqr));
                totalForce = totalForce.add(r.scale(forceMag));
            }
            accelerations[i] = totalForce.scale(1.0 / bodies.get(i).getMass());
        }

        return accelerations;
    }

    private double[] flattenState(List<CelestialBody> bodies) {
        double[] state = new double[6 * bodies.size()];
        for (int i = 0; i < bodies.size(); i++) {
            Vector3D pos = bodies.get(i).getPosition();
            Vector3D vel = bodies.get(i).getVelocity();
            state[6 * i]     = pos.getX();
            state[6 * i + 1] = pos.getY();
            state[6 * i + 2] = pos.getZ();
            state[6 * i + 3] = vel.getX();
            state[6 * i + 4] = vel.getY();
            state[6 * i + 5] = vel.getZ();
        }
        return state;
    }

    private void updateBodiesFromState(double[] state) {
        for (int i = 0; i < bodies.size(); i++) {
            Vector3D pos = new Vector3D(state[6 * i], state[6 * i + 1], state[6 * i + 2]);
            Vector3D vel = new Vector3D(state[6 * i + 3], state[6 * i + 4], state[6 * i + 5]);
            bodies.get(i).setPosition(pos);
            bodies.get(i).setVelocity(vel);
        }
    }
}

