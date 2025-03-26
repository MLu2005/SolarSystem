package executables.solvers;
import java.util.Arrays;
import java.util.function.BiFunction;
import static executables.solvers.ODEUtility.addVectors;
import static executables.solvers.ODEUtility.scaleVector;

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
            for (int j = 0; j < dim; j++) {
                valuePairs[i][j + 1] = y[j];
            }

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
