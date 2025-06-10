package com.example.utilities.solvers;

import java.util.Arrays;
import java.util.function.BiFunction;

import static com.example.utilities.solvers.ODEUtility.*;

public class RK4Solver implements ODESolver {

    @Override
    public double[][] solve(
            BiFunction<Double, double[], double[]> f,
            double t0,
            double[]y0,
            double stepSize,
            int steps,
            BiFunction<Double, double[], Boolean> stopCondition
    ) {
        int dim =y0.length;
        double[][] values = initStorage(steps, t0,y0);

        for (int i = 0; i < steps; i++) {
            if (stopCondition != null && stopCondition.apply(t0,y0)) {
                return Arrays.copyOf(values, i + 1);
            }

            double[] k1 = f.apply(t0,y0);
            double[] k2 = f.apply(t0 + stepSize / 2.0, addVectors(y0, scaleVector(k1, stepSize / 2.0)));
            double[] k3 = f.apply(t0 + stepSize / 2.0, addVectors(y0, scaleVector(k2, stepSize / 2.0)));
            double[] k4 = f.apply(t0 + stepSize, addVectors(y0, scaleVector(k3, stepSize)));

            for (int j = 0; j < dim; j++) {
               y0[j] += (stepSize / 6.0) * (k1[j] + 2 * k2[j] + 2 * k3[j] + k4[j]);
            }

            t0 = t0 + stepSize;
            values[i + 1][0] = t0;
            for (int j = 0; j < dim; j++) {
                values[i + 1][j + 1] =y0[j];
            }
        }

        return values;
    }

    /**
     * I got really weird Abstract Pipeline errors, so I changed the code here to do everything it need to do.
     * THis somehow seemed to fix it!
     *
     * @param f
     * @param t
     * @param y
     * @param h
     * @return
     */
    public double[] solveStep(
            BiFunction<Double, double[], double[]> f,
            double t,
            double[] y,
            double h
    ) {
        int dim = y.length;
        double halfH = 0.5 * h;
        double tPlusHalfH = t + halfH;
        double tPlusH = t + h;
        double inv6 = h / 6.0;

        double[] yTemp = new double[dim];
        double[] result = new double[dim];

        double[] k1 = f.apply(t, y);

        for (int j = 0; j < dim; j++) {
            yTemp[j] = y[j] + halfH * k1[j];
        }
        double[] k2 = f.apply(tPlusHalfH, yTemp);

        for (int j = 0; j < dim; j++) {
            yTemp[j] = y[j] + halfH * k2[j];
        }
        double[] k3 = f.apply(tPlusHalfH, yTemp);

        for (int j = 0; j < dim; j++) {
            yTemp[j] = y[j] + h * k3[j];
        }
        double[] k4 = f.apply(tPlusH, yTemp);

        for (int j = 0; j < dim; j++) {
            // Combine terms to minimize operations
            double k23 = k2[j] + k3[j];  // Calculate k2+k3 once
            result[j] = y[j] + inv6 * (k1[j] + 2 * k23 + k4[j]);
        }

        return result;
    }



}
