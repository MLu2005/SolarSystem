package executables.testing;


import com.example.utilities.solvers.EulerSolver;

import java.util.function.BiFunction;


public class EulerTest_realistic {

    public static void lotkaVolterraEulerTest() {


        double a = 1, b = 0.2, c = 0.2, d = 0.1;

        BiFunction<Double, double[], double[]> f = (t, Y) -> {
            double x = Y[0];
            double y = Y[1];

            double dx = a * x - b * x * y;
            double dy = -c * y + d * x * y;

            return new double[]{dx, dy};
        };

        double t0 = 0.0;
        double[] y0 = {10.0, 5.0};
        double stepSize = 0.01;
        int steps = 1000;

        EulerSolver euler = new EulerSolver();

        double[][] result = euler.solve(f, t0, y0, stepSize, steps, null);


        System.out.printf("%-10s %-15s %-15s\n", "t", "x (prey)", "y (predators)");

        for (double[] row : result) {
            System.out.printf("%-10.2f %-15.8f %-15.8f\n", row[0], row[1], row[2]);
        }
    }

    /**
     * Data from the Lotka-Volterra predator-prey model simulation.
     *
     * Simulates population dynamics of rabbits (prey) and foxes (predators)
     * using the system of ODEs:
     *   dx/dt = αx - βxy
     *   dy/dt = δxy - γy
     *
     * @param t Time value at a given step (seconds).
     * @param x Number of prey (rabbits) at that time.
     * @param y Number of predators (foxes) at that time.
     *
     * The data represents a classic biological system where:
     * - The prey population grows but is reduced by predators.
     * - The predator population depends on whether preys are growing
     *
     */



}
