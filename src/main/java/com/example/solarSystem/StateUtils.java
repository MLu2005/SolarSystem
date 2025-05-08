package com.example.solarSystem;

import java.util.List;

/**
 * StateUtils provides utility methods to convert between a list of celestial bodies
 * and a flat state vector used by numerical solvers.
 * It supports extracting and applying position and velocity data.
 */
public class StateUtils {

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
     * @param state flat array of positions and velocities
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
}
