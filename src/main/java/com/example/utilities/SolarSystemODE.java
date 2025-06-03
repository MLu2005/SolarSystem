package com.example.utilities;

import com.example.solar_system.CelestialBody;
import executables.Constants;

import java.util.List;
import java.util.function.BiFunction;

/**
 * SolarSystemODE generates the Ordinary Differential Equation (ODE) system
 * used by the RK4 solver to compute the motion of celestial bodies under mutual gravitational attraction.
 * The first body (usually the Sun) is fixed in place and does not move.
 */
public class SolarSystemODE {

    public static final double G = Constants.G; // Gravitational constant in km^3 / (kg * s^2)

    /**
     * Generates a function that computes the derivative (velocity and acceleration) for each body
     * based on their positions and masses.
     * The first body is treated as fixed (position and velocity are zero).
     *
     * @param bodies List of celestial bodies participating in the simulation
     * @return a function (t, state) -> derivatives, suitable for numerical ODE solvers
     */
    public static BiFunction<Double, double[], double[]> generateODE(List<CelestialBody> bodies) {
        return (t, state) -> {
            int n = bodies.size();
            double[] derivatives = new double[n * 6]; // Each body has 3 for position + 3 for velocity

            for (int i = 0; i < n; i++) {
                int idx = i * 6;

                Vector3D pos_i = new Vector3D(state[idx], state[idx + 1], state[idx + 2]);
                Vector3D vel_i = new Vector3D(state[idx + 3], state[idx + 4], state[idx + 5]);
                Vector3D acc_i = Vector3D.zero(); // Reset acceleration

                for (int j = 0; j < n; j++) {
                    if (i == j) continue;

                    int jdx = j * 6;
                    Vector3D pos_j = new Vector3D(state[jdx], state[jdx + 1], state[jdx + 2]);
                    double mass_j = bodies.get(j).getMass();

                    Vector3D r_ij = pos_j.subtract(pos_i);
                    double distance = r_ij.magnitude() + 1e-9;

                    // Newton's law (vector form): a_i += G * m_j * (r_ij / |r_ij|^3)
                    acc_i = acc_i.add(r_ij.scale(G * mass_j / Math.pow(distance, 3)));
                }

                // Derivatives
                derivatives[idx] = vel_i.x;
                derivatives[idx + 1] = vel_i.y;
                derivatives[idx + 2] = vel_i.z;

                derivatives[idx + 3] = acc_i.x;
                derivatives[idx + 4] = acc_i.y;
                derivatives[idx + 5] = acc_i.z;
            }

            return derivatives;
        };
    }
}
