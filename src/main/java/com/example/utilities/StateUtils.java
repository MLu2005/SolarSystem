package com.example.utilities;

import com.example.solar_system.CelestialBody;
import executables.Constants;
import java.util.List;

/**
 * StateUtils provides utility methods to convert between a list of celestial bodies
 * and a flat state vector used by numerical solvers.
 * It supports extracting and applying position and velocity data.
 */
public class StateUtils {

    public static final double G = 6.6743E-20; // Gravitational constant in km^3 kg^-1 s^-2

    /**
     * Converts a list of celestial bodies into a flat state vector.
     * Each body contributes 6 values: x, y, z for position and x, y, z for velocity.
     *
     * @param bodies list of celestial bodies
     * @return a double array representing the simulation state
     */
    public static double[] extractStateVector(List<CelestialBody> bodies) {
        int n = bodies.size();
        double[] state = new double[n * 6];

        for (int i = 0; i < n; i++) {
            CelestialBody body = bodies.get(i);
            int idx = i * 6;

            state[idx]     = body.getPosition().x;
            state[idx + 1] = body.getPosition().y;
            state[idx + 2] = body.getPosition().z;
            state[idx + 3] = body.getVelocity().x;
            state[idx + 4] = body.getVelocity().y;
            state[idx + 5] = body.getVelocity().z;
        }

        return state;
    }

    /**
     * Applies a state vector to a list of celestial bodies, updating their position and velocity.
     * This is typically used after each step of a numerical integrator.
     *
     * @param state  flat array of positions and velocities
     * @param bodies list of celestial bodies to update
     */
    public static void applyStateVector(double[] state, List<CelestialBody> bodies) {
        int n = bodies.size();

        for (int i = 0; i < n; i++) {
            CelestialBody body = bodies.get(i);
            int idx = i * 6;

            Vector3D pos = new Vector3D(state[idx], state[idx + 1], state[idx + 2]);
            Vector3D vel = new Vector3D(state[idx + 3], state[idx + 4], state[idx + 5]);

            body.setPosition(pos);
            body.setVelocity(vel);
        }
    }

    /**
     * Computes the derivatives (velocity and acceleration) of the given state vector.
     * Used by RK4 solver.
     *
     * @param state  current flat state vector [x,y,z,vx,vy,vz,...]
     * @param bodies list of celestial bodies to get masses from and update positions for gravity
     * @return the derivative vector (dx/dt = velocity, dv/dt = acceleration)
     */
    public static double[] computeDerivatives(double[] state, List<CelestialBody> bodies) {
        // ✅ Ensure positions and velocities are updated before using them
        applyStateVector(state, bodies);

        int n = bodies.size();
        double[] derivatives = new double[n * 6];

        for (int i = 0; i < n; i++) {
            int idx = i * 6;

            // Velocity derivatives (dx/dt = vx, etc.)
            derivatives[idx]     = state[idx + 3];
            derivatives[idx + 1] = state[idx + 4];
            derivatives[idx + 2] = state[idx + 5];

            // Initialize acceleration
            double ax = 0, ay = 0, az = 0;
            Vector3D posI = new Vector3D(state[idx], state[idx + 1], state[idx + 2]);

            for (int j = 0; j < n; j++) {
                if (i == j) continue;

                int jdx = j * 6;
                Vector3D posJ = new Vector3D(state[jdx], state[jdx + 1], state[jdx + 2]);

                Vector3D delta = posJ.subtract(posI);
                double distSq = delta.magnitudeSquared() + 1e-9;
                double dist = Math.sqrt(distSq);

                double force = G * bodies.get(j).getMass() / (distSq * dist); // = G * m / r^3

                ax += delta.x * force;
                ay += delta.y * force;
                az += delta.z * force;
            }

            derivatives[idx + 3] = ax;
            derivatives[idx + 4] = ay;
            derivatives[idx + 5] = az;
        }

        return derivatives;
    }
}
