package com.example.utilities.physics_utilities;

import com.example.utilities.Vector3D;
import com.example.solar_system.CelestialBody;
import executables.Constants;
import executables.solvers.RK4Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Now USES RK4 instead of VERLET!
 */
public class PhysicsEngine {

    private static final double G = Constants.G;
    private final List<CelestialBody> bodies = new ArrayList<>();

    public void addBody(CelestialBody body) {
        bodies.add(body);
    }

    public List<CelestialBody> getBodies() {
        return bodies;
    }

    public void step(double dt) {
        int n = bodies.size();


        double[] y = new double[n * 6];
        for (int i = 0; i < n; i++) {
            CelestialBody b = bodies.get(i);
            int idx = i * 6;
            y[idx] = b.getPosition().x;
            y[idx + 1] = b.getPosition().y;
            y[idx + 2] = b.getPosition().z;
            y[idx + 3] = b.getVelocity().x;
            y[idx + 4] = b.getVelocity().y;
            y[idx + 5] = b.getVelocity().z;
        }

        BiFunction<Double, double[], double[]> f = (t, state) -> {
            double[] dydt = new double[n * 6];

            for (int i = 0; i < n; i++) {
                int idx = i * 6;
                dydt[idx] = state[idx + 3];
                dydt[idx + 1] = state[idx + 4];
                dydt[idx + 2] = state[idx + 5];
            }

            for (int i = 0; i < n; i++) {
                CelestialBody bi = bodies.get(i);
                Vector3D pos_i = new Vector3D(state[i * 6], state[i * 6 + 1], state[i * 6 + 2]);
                Vector3D acc = Vector3D.zero();

                for (int j = 0; j < n; j++) {
                    if (i == j) continue;

                    CelestialBody bj = bodies.get(j);
                    Vector3D pos_j = new Vector3D(state[j * 6], state[j * 6 + 1], state[j * 6 + 2]);
                    Vector3D r = pos_j.subtract(pos_i);
                    double distSq = r.magnitudeSquared();

                    if (distSq < 1e-6) continue;

                    double forceMag = G * bj.getMass() / distSq;
                    acc = acc.add(r.safeNormalize().scale(forceMag));
                }

                int idx = i * 6;
                dydt[idx + 3] = acc.x;
                dydt[idx + 4] = acc.y;
                dydt[idx + 5] = acc.z;
            }

            return dydt;
        };

        // RK4 integration
        RK4Solver rk4 = new RK4Solver();
        double[] newState = rk4.solveStep(f, 0, y, dt);

        for (int i = 0; i < n; i++) {
            int idx = i * 6;
            Vector3D pos = new Vector3D(newState[idx], newState[idx + 1], newState[idx + 2]);
            Vector3D vel = new Vector3D(newState[idx + 3], newState[idx + 4], newState[idx + 5]);
            CelestialBody body = bodies.get(i);

            body.setPosition(pos);
            body.setVelocity(vel);

            double[] finalDerivatives = f.apply(0.0, newState);
            Vector3D a = new Vector3D(
                    finalDerivatives[idx + 3],
                    finalDerivatives[idx + 4],
                    finalDerivatives[idx + 5]
            );
            body.setAcceleration(a);

        }
    }
}
