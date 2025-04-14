package executables.solar_system;

import java.util.List;
import java.util.function.BiFunction;

public class SolarSystemODE {

    public static final double G = 6.67430e-20; // km^3 / (kg * s^2)

    /**
     * Tworzy funkcję ODE dla solvera RK4, na podstawie listy ciał niebieskich.
     * Pierwsze ciało (zwykle Słońce) jest utrzymywane w centrum – nie zmienia pozycji ani prędkości.
     */
    public static BiFunction<Double, double[], double[]> generateODE(List<CelestialBody> bodies) {
        return (t, state) -> {
            int n = bodies.size();
            double[] derivatives = new double[n * 6]; // 3 pozycje + 3 prędkości na ciało

            for (int i = 0; i < n; i++) {
                int idx = i * 6;

                // === ZABLOKOWANE CIAŁO (np. Słońce) ===
                if (i == 0) {
                    // Zero ruchu – nie aktualizujemy położenia ani prędkości
                    derivatives[idx]     = 0;
                    derivatives[idx + 1] = 0;
                    derivatives[idx + 2] = 0;
                    derivatives[idx + 3] = 0;
                    derivatives[idx + 4] = 0;
                    derivatives[idx + 5] = 0;
                    continue;
                }

                // Pozostałe ciała
                Vector3D pos_i = new Vector3D(state[idx], state[idx + 1], state[idx + 2]);
                Vector3D vel_i = new Vector3D(state[idx + 3], state[idx + 4], state[idx + 5]);
                Vector3D acc_i = Vector3D.zero();

                for (int j = 0; j < n; j++) {
                    if (i == j) continue;

                    int jdx = j * 6;
                    Vector3D pos_j = new Vector3D(state[jdx], state[jdx + 1], state[jdx + 2]);
                    double mass_j = bodies.get(j).getMass();

                    Vector3D r_ij = pos_j.subtract(pos_i);
                    double distance = r_ij.magnitude() + 1e-9;

                    Vector3D forceDir = r_ij.scale(1.0 / Math.pow(distance, 3));
                    acc_i = acc_i.add(forceDir.scale(G * mass_j));
                }

                derivatives[idx]     = vel_i.x;
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
