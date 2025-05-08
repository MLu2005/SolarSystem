
import com.example.solarSystem.*;
import executables.solvers.RKF45Solver;

import java.util.List;
import java.util.function.BiFunction;

/**
 * This class runs a console-based test of the solar system simulation using the Runge-Kutta 4 solver.
 * It loads initial data, runs the simulation for one Earth year (365 days),
 * and prints positions of celestial bodies every 30 days.
 */
public class SolarSystemTest {

    /**
     * Main method for running the simulation in console mode.
     * It prints the solar system state every 30 days for 1 year.
     */
    public static void main(String[] args) {
        // 1. Load celestial body data from CSV
        String filePath = "src/main/java/com/example/solarSystem/IC.csv";  // ← Adjust path if needed
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV(filePath);

        // 2. Create the ODE system using Newtonian gravity
        double[] initialState = StateUtils.extractStateVector(bodies);
        BiFunction<Double, double[], double[]> ode = SolarSystemODE.generateODE(bodies);

        // 3. Simulation parameters
        double stepSize = 86400; // one day in seconds
        int steps = 365;         // simulate 1 year

        // 4. Run RK4 solver
        RKF45Solver rkf45 = new RKF45Solver();
        double[][] result = rkf45.solve(ode, 0, initialState, stepSize, steps, null);

        // 5. Output system state every 30 days
        int interval = 30;
        for (int i = 0; i < result.length; i += interval) {
            System.out.printf("Day %d:\n", i);
            StateUtils.applyStateVector(result[i], bodies);

            for (int j = 0; j < bodies.size(); j++) {
                CelestialBody body = bodies.get(j);
                Vector3D pos = body.getPosition();
                System.out.printf("  %s → x: %.2e, y: %.2e, z: %.2e\n",
                        body.getName(), pos.x, pos.y, pos.z);
            }

            System.out.println();
        }
    }
}
