package executables.testing;

import executables.solvers.firstOrder;
import java.util.function.BiFunction;

public class EulerTest {

    public static void eulerError(BiFunction<Double, Double, Double> f, double[] stepSizes) {

        double x0 = 0.0;
        double y0 = 1.0;
        double xEnd = 1.0;

        System.out.printf("%-10s %-15s %-15s\n", "StepSize", "Max Error", "Avg Error");

        for (double h : stepSizes) {
            int steps = (int) ((xEnd - x0) / h);

            double[][] result = firstOrder.euler(f, x0, y0, h, steps);

            double maxError = 0.0;
            double sumError = 0.0;

            for (int i = 0; i < result.length; i++) {
                double x = result[i][0];
                double y_euler = result[i][1];
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

        BiFunction<Double, Double, Double> f = (x, y) -> y;
        double x0 = 0.0;
        double y0 = 1.0;
        double xEnd = 1.0;
        double[] stepSizes = {1.0, 0.5, 0.1, 0.01};

        System.out.printf("%-10s %-15s %-15s\n", "StepSize", "Max Error", "Avg Error");

        for (double h : stepSizes) {
            int steps = (int) ((xEnd - x0) / h);

            double[][] result = firstOrder.euler(f, x0, y0, h, steps);

            double maxError = 0.0;
            double sumError = 0.0;

            for (int i = 0; i < result.length; i++) {
                double x = result[i][0];
                double y_euler = result[i][1];
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
