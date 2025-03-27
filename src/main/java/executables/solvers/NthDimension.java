package executables.solvers;

import java.util.Arrays;
import java.util.function.BiFunction;

import static executables.solvers.ODEUtility.addVectors;
import static executables.solvers.ODEUtility.scaleVector;

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

    /**
     * RK4 solver with an optional stopping condition.
     * Stops early if the condition evaluates to true.
     */
    public static double[][] rungeKutta4(
            BiFunction<Double, double[], double[]> f,
            double t0,
            double[] y0,
            double stepSize,
            int maxSteps,
            BiFunction<Double, double[], Boolean> stopCondition
    ) {
        int dim = y0.length;
        double[][] valuePairs = new double[maxSteps][dim + 1]; // first column = time
        double t = t0;
        double[] y = Arrays.copyOf(y0, dim); // copy of initial state

        for (int i = 0; i < maxSteps; i++) {
            valuePairs[i][0] = t;
            System.arraycopy(y, 0, valuePairs[i], 1, dim);

            // Stop early if stopping condition is met
            if (stopCondition != null && stopCondition.apply(t, y)) {
                return Arrays.copyOf(valuePairs, i + 1);
            }

            // RK4 steps
            double[] k1 = f.apply(t, y);
            double[] k2 = f.apply(t + stepSize / 2.0, addVectors(y, scaleVector(k1, stepSize / 2.0)));
            double[] k3 = f.apply(t + stepSize / 2.0, addVectors(y, scaleVector(k2, stepSize / 2.0)));
            double[] k4 = f.apply(t + stepSize, addVectors(y, scaleVector(k3, stepSize)));

            // Weighted average of slopes
            for (int j = 0; j < dim; j++) {
                y[j] += (stepSize / 6.0) * (k1[j] + 2 * k2[j] + 2 * k3[j] + k4[j]);
            }

            t += stepSize;
        }

        return valuePairs;
    }
    /**
     * RK4 solver with a fixed number of steps (no stopping condition).
     */
    public static double[][] rungeKutta4(
            BiFunction<Double, double[], double[]> f,
            double t0,
            double[] y0,
            double stepSize,
            int maxSteps
    ) {
        return rungeKutta4(f, t0, y0, stepSize, maxSteps, null);
    }
}
