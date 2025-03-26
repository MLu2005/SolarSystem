package executables.solvers;

import java.util.Arrays;
import java.util.function.BiFunction;

public class NthDimension {


    /**
     * Euler's method for solving an N-dimensional system of ODEs.
     * based partly on: https://www.youtube.com/watch?v=KYlPFptGDQA
     *
     * @param f        The system of ODEs represented as a function (x, Y) -> dYdx.
     * @param x        Initial x value.
     * @param y        Initial values of y as an array.
     * @param stepSize Step size (h).
     * @param steps    Number of steps to compute.
     * @return A 2D array containing x values and corresponding Y values at each step.
     */
    public static double[][] eulerNth(BiFunction<Double, Double[], Double[]> f, Double x, Double[] y, double stepSize,
                                      int steps) {
        int n = y.length;
        double[][] values = new double[steps + 1][n + 1];

        values[0][0] = x;

        // Copy initial y values
        for (int i = 0; i < n; i++) {
            values[0][i + 1] = y[i];
        }

        for (int i = 0; i < steps; i++) {
            // Compute derivatives dydx = f(x, y)
            Double[] dydx = f.apply(x, y);

            // y_next = y + h * dydx
            for (int k = 0; k < n; k++) {
                y[k] += stepSize * dydx[k];
            }

            x += stepSize;
            values[i + 1][0] = x;

            for (int j = 0; j < n; j++) {
                values[i + 1][j + 1] = y[j];
            }
        }
        return values;
    }
}
