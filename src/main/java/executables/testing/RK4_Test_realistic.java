package executables.testing;

import executables.solvers.NthDimension;
import java.util.function.BiFunction;

public class RK4_Test_realistic {

    public static void testLotkaVolterra() {
        double alpha = 1.0;
        double beta = 0.1;
        double delta = 0.075;
        double gamma = 1.5;

        BiFunction<Double, double[], double[]> f = (t, y) -> {
            double prey = y[0];
            double predator = y[1];

            double dPrey = alpha * prey - beta * prey * predator;
            double dPredator = delta * prey * predator - gamma * predator;

            return new double[]{dPrey, dPredator};
        };

        double[] y0 = {10, 5};
        double t0 = 0.0;
        double h = 0.01;
        int steps = 1000;

        double[][] result = NthDimension.rungeKutta4(f, t0, y0, h, steps);


        System.out.printf("%-10s %-15s %-15s\n", "t", "Prey (x)", "Predator (y)");
        for (int i = 0; i <= 100; i++) {
            System.out.printf("%.2f       %-15.8f %-15.8f\n", result[i][0], result[i][1], result[i][2]);
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
     * @param t Time value at each step (seconds).
     * @param x Prey population (rabbits).
     * @param y Predator population (foxes).
     *
     * The data represents a classic biological system where:
     * - The prey population grows but is reduced by predators.
     * - The predator population depends on whether preys are growing
     */

}


