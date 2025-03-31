package com.example.demo;

import java.util.Arrays;
import java.util.function.BiFunction;

public class FirstDimension {

    // * Euler method for solving a 1st-order ODE
    public static double[][] euler1st(BiFunction<Double, double[], double[]> f, double x0, double[] initialState, double stepSize, int steps) {
        int numVariables = initialState.length;
        double[][] valuePairs = new double[steps][numVariables + 1];
        double x = x0;


        double[] y = Arrays.copyOf(initialState, initialState.length);  // copy to double[] to avoid side-effects

        for (int i = 0; i < steps; i++) {
            valuePairs[i][0] = x;

            for (int j = 0; j < numVariables; j++) {
                valuePairs[i][j + 1] = y[j];
            }


            double[] derivatives = f.apply(x, y);


            for (int j = 0; j < numVariables; j++) {
                y[j] = y[j] + stepSize * derivatives[j];
            }


            x = x + stepSize;
        }

        return valuePairs;
    }
}
