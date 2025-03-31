package com.example.demo;

import java.util.Arrays;
import java.util.function.BiFunction;

import static com.example.demo.ODEUtility.addVectors;
import static com.example.demo.ODEUtility.scaleVector;

public class FirstDimension {

    // * But honestly i think we dont need this class and we dont even need to apply it to the GUI since NthDimension class is better and more flexible.

    // * Euler method for solving a 1st-order ODE
    public static double[][] euler1st(BiFunction<Double, Double[], Double[]> f, double x0, Double[] initialState, double stepSize, int steps) {
        int numVariables = initialState.length;
        double[][] valuePairs = new double[steps][numVariables + 1];
        double x = x0;

        // creating a copy of initialState to avoid side-effects (DEADLY)
        Double[] y = Arrays.copyOf(initialState, initialState.length);

        for (int i = 0; i < steps; i++) {
            valuePairs[i][0] = x;


            for (int j = 0; j < numVariables; j++) {
                valuePairs[i][j + 1] = y[j];
            }


            Double[] derivatives = f.apply(x, y);


            for (int j = 0; j < numVariables; j++) {
                y[j] = y[j] + stepSize * derivatives[j];
            }


            x = x + stepSize;


            x = Math.round(x * 100.0) / 100.0;
        }
        return valuePairs;
    }

}
