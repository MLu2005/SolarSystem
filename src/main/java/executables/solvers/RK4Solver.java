package executables.solvers;

import java.util.Arrays;
import java.util.function.BiFunction;

import static com.example.demo.ODEUtility.addVectors;
import static com.example.demo.ODEUtility.scaleVector;
import static executables.solvers.ODEUtility.*;

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

    public double[] solveStep(
            BiFunction<Double, double[], double[]> f,
            double t,
            double[] y,
            double stepSize
    ) {
        double[] k1 = f.apply(t, y);
        double[] k2 = f.apply(t + stepSize / 2.0, addVectors(y, scaleVector(k1, stepSize / 2.0)));
        double[] k3 = f.apply(t + stepSize / 2.0, addVectors(y, scaleVector(k2, stepSize / 2.0)));
        double[] k4 = f.apply(t + stepSize, addVectors(y, scaleVector(k3, stepSize)));

        double[] nextY = new double[y.length];
        for (int j = 0; j < y.length; j++) {
            nextY[j] = y[j] + (stepSize / 6.0) * (k1[j] + 2 * k2[j] + 2 * k3[j] + k4[j]);
        }
        return nextY;
    }



}
