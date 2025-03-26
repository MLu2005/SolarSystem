package executables.testing;

import executables.solvers.firstOrder;

import java.util.function.BiFunction;
import java.util.Arrays;


public class euler_analytical {


    public static void main(String[] args) {

        BiFunction<Double, Double, Double> f = (x, y) -> y;


        double x0 = 0.0;
        double y0 = 1.0;
        double h = 0.1;
        int steps = 10;


        double[][] result = firstOrder.euler(f, x0, y0, h, steps);


        System.out.printf("%-10s %-15s %-15s %-15s\n", "x", "Euler y(x)", "Exact y(x)", "Error");

        for (int i = 0; i < result.length; i++) {
            double x = result[i][0];
            double y_euler = result[i][1];
            double y_exact = Math.exp(x);
            double error = Math.abs(y_euler - y_exact);

            System.out.printf("%-10.4f %-15.8f %-15.8f %-15.8f\n", x, y_euler, y_exact, error);
        }
    }
}


