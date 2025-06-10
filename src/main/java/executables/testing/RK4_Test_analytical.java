package executables.testing;

import com.example.utilities.solvers.RK4Solver;

import java.util.function.BiFunction;

public class RK4_Test_analytical {

    public static void rk4Error(BiFunction<Double, double[], double[]> f, double[] stepSizes) {
        double x0 = 0.0;
        double[] y0 = {1.0};
        double xEnd = 1.0;

        System.out.printf("%-10s %-15s %-15s\n", "StepSize", "Max Error", "Avg Error");

        for (double h : stepSizes) {
            int steps = (int) ((xEnd - x0) / h);

            RK4Solver rk4 = new RK4Solver();
            double[][] result = rk4.solve(f, x0, y0.clone(), h, steps, null);

            double maxError = 0.0;
            double sumError = 0.0;

            for (double[] row : result) {
                double x = row[0];
                double y_rk4 = row[1];
                double y_exact = Math.exp(x);
                double error = Math.abs(y_rk4 - y_exact);

                sumError += error;
                if (error > maxError) {
                    maxError = error;
                }
            }

            double avgError = sumError / result.length;

            System.out.printf("%-10.4f %-15.8f %-15.8f\n", h, maxError, avgError);
        }
    }

    public static void rk4ErrorDefault() {
        BiFunction<Double, double[], double[]> f = (x, y) -> new double[]{y[0]};
        double[] stepSizes = {1.0, 0.5, 0.1, 0.01};

        rk4Error(f, stepSizes);
    }


    /**
     * Analytical error testing for the Runge-Kutta method.
     *
     * Solves the ODE dy/dx = y with y(0) = 1, where the exact solution is y(x) = e^x.
     * Compares RK4 numerical results against the true solution and outputs:
     * - Max error
     * - Avg error
     *
     * @param f The differential equation represented as a function (x, y) -> dy/dx.
     * @param stepSizes Array of step sizes to evaluate accuracy.
     *
     * Demonstrates the high accuracy and 4th-order convergence of RK4.
     */

}



