package executables.testing;



import executables.solvers.EulerSolver;

import java.util.function.BiFunction;

public class EulerTest_analytical {

    /**
     * Analytical error testing for the Euler method using a known exact solution.
     *
     * This class solves the ODE dy/dx = y with y(0) = 1, where the exact solution is y(x) = e^x.
     * It compares the numerical Euler solution to the true solution and computes:
     * - Max error
     * - Avg error
     *
     * @param f Function representing dy/dx.
     * @param stepSizes Array of step sizes to test.
     */
    public static void eulerError(BiFunction<Double, double[], double[]> f, double[] stepSizes) {

        double x0 = 0;
        double[] y0 = new double[1];
        double xEnd = 1.0;


        y0[0] = 1.00;

        System.out.printf("%-10s %-15s %-15s\n", "StepSize", "Max Error", "Avg Error");

        for (double h : stepSizes) {
            int steps = (int) ((xEnd - x0) / h);

            EulerSolver euler = new EulerSolver();
            double[][] result = euler.solve(f, x0, y0, h, steps, null);
            double maxError = 0.0;
            double sumError = 0.0;

            for (double[] doubles : result) {
                double x = doubles[0];
                double y_euler = doubles[1];
                double y_exact = Math.exp(x);
                double error = Math.abs(y_euler - y_exact);

                sumError += error;
                if (error > maxError) {
                    maxError = error;
                }
            }

            double avgError = sumError / result.length;

            System.out.printf("%-10.4f %-15.8f %-15.8f\n", h, maxError, avgError);
        }
    }

    public static void eulerErrorDefault() {

        BiFunction<Double, double[], double[]> f = (x, y) -> y;
        double x0 = 0.0;
        double[] y0 = new double[1];
        double xEnd = 1.0;
        double[] stepSizes = {1.0, 0.5, 0.1, 0.01};

        y0[0] = 1.00;

        System.out.printf("%-10s %-15s %-15s\n", "StepSize", "Max Error", "Avg Error");

        for (double h : stepSizes) {
            int steps = (int) ((xEnd - x0) / h);

            EulerSolver euler = new EulerSolver();
            double[][] result = euler.solve(f, x0, y0, h, steps, null);

            double maxError = 0.0;
            double sumError = 0.0;

            for (double[] doubles : result) {
                double x = doubles[0];
                double y_euler = doubles[1];
                double y_exact = Math.exp(x);
                double error = Math.abs(y_euler - y_exact);

                sumError += error;
                if (error > maxError) {
                    maxError = error;
                }
            }

            double avgError = sumError / result.length;

            System.out.printf("%-10.4f %-15.8f %-15.8f\n", h, maxError, avgError);
        }
    }
}
