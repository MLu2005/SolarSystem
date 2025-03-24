package src.solvers;
import java.util.function.BiFunction;

public class firstOrder {
    /*
    public static void main(String[] args) {
        BiFunction<Double, Double, Double> testEquation = (x, y) -> x + y;

        System.out.println(Arrays.deepToString(euler(testEquation, 0, 1, 0.1, 100)));
    }
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
