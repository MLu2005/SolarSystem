package executables.solar_system;

import executables.solvers.NthDimension;
import java.util.List;
import java.util.function.BiFunction;

public class SolarSystemTest {

    public static void main(String[] args) {
        // 1. Wczytanie danych
        String filePath = "src/main/java/executables/solar_system/IC.csv";  // ← dostosuj ścieżkę
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV(filePath);

        // 2. Tworzenie solvera RK4
        double[] initialState = StateUtils.extractStateVector(bodies);
        BiFunction<Double, double[], double[]> ode = SolarSystemODE.generateODE(bodies);

        // 3. Parametry
        double stepSize = 86400;  // 1 dzień (w sekundach)
        int steps = 365;          // 1 rok

        // 4. Uruchomienie solvera
        double[][] result = NthDimension.rungeKutta4(ode, 0, initialState, stepSize, steps);

        // 5. Wypisz co 30 dni
        int interval = 30;
        for (int i = 0; i < result.length; i += interval) {
            System.out.printf("Day %d:\n", i);
            StateUtils.applyStateVector(result[i], bodies);
            for (CelestialBody body : bodies) {
                Vector3D pos = body.getPosition();
                System.out.printf("  %s → x: %.2e, y: %.2e, z: %.2e\n",
                        body.getName(), pos.x, pos.y, pos.z);
            }
            System.out.println();
        }
    }
}
