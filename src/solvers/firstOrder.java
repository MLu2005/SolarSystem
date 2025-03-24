package src.solvers;
import java.util.Arrays;
import java.util.function.BiFunction;

public class firstOrder {

    // for easy testing, delete later
    public static void main(String[] args) {
        BiFunction<Double, Double, Double> testEquation = (x, y) -> x + y;

        System.out.println(Arrays.deepToString(euler(testEquation, 0, 1, 1, 100)));

        double[][] list = euler(testEquation, 0, 1, 1, 100);

        double val = ODEUtility.getValueAt(list, 1.5);
        System.out.println(val);
    }

    /**
     * This is the implementation of Euler's method of approximating differential equations
     * O(n) complexity in time and space
     *
     * @param f is the differential equation to be solved by Euler
     * @param x0 starting x
     * @param y0 starting y
     * @param stepSize step size, meaning in which intervals we should approximate
     * @param steps the total number of steps, how far we want to go
     * @return a list value pairs
     */
    public static double[][] euler(BiFunction<Double, Double, Double> f, double x0, double y0, double stepSize,
                                      int steps) {
        double[][] valuePairs = new double[steps][2];
        double x = x0, y = y0;

        for (int i = 0; i < steps; i++) {
            // appending to the list that is to be returned
            valuePairs[i][0] = x;
            valuePairs[i][1] = y;

            // computing slope for Eulers Formula
            double slope_f_at_i= f.apply(x, y);

            // calculating new y to corresponding ith step
            y= y + stepSize * slope_f_at_i;

            x = x + stepSize;
        }
        return valuePairs;
    }


}
