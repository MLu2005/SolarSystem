package executables.solar_system;

import java.util.List;

public class StateUtils {

    /**
     * Konwertuje listę ciał niebieskich do wektora stanu (pozycje i prędkości).
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
     * Aktualizuje pozycje i prędkości w liście ciał na podstawie wektora stanu.
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
