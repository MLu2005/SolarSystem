package com.example.utilities.solvers;

import java.util.Arrays;
import java.util.function.BiFunction;

import static com.example.utilities.solvers.ODEUtility.*;

public class EulerSolver implements ODESolver {
    public double[][] solve(
            BiFunction<Double, double[], double[]> f,
            double x,
            double[]y0,
            double stepSize,
            int steps,
            BiFunction<Double, double[], Boolean> stopCondition
    ) {
        int dim = y0.length;
        double[][] values = initStorage(steps, x, y0);

        for (int i = 0; i < steps; i++) {
            if (stopCondition != null && stopCondition.apply(x, y0)) {
                return Arrays.copyOf(values, i + 1);
            }

            double[] dydx = f.apply(x, y0);
            for (int k = 0; k < dim; k++) {
                y0[k] += stepSize * dydx[k];
            }

            x = x + stepSize;
            values[i + 1][0] = x;
            for (int j = 0; j < dim; j++) {
                values[i + 1][j + 1] = y0[j];
            }
        }
        return values;
    }
}
