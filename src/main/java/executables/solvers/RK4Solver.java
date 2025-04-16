package executables.solvers;

import java.util.Arrays;
import java.util.function.BiFunction;

import static executables.solvers.ODEUtility.*;

public class RK4Solver extends AbstractODESolver {

    @Override
    public double[][] solve(
            BiFunction<Double, double[], double[]> f,
            double t,
            double[] y0,
            double stepSize,
            int steps,
            BiFunction<Double, double[], Boolean> stopCondition
    ) {
        int dim = y0.length;
        double[] y = Arrays.copyOf(y0, dim);
        double[][] values = initStorage(steps, t, y);

        for (int i = 0; i < steps; i++) {
            if (stopCondition != null && stopCondition.apply(t, y)) {
                return Arrays.copyOf(values, i + 1);
            }

            double[] k1 = f.apply(t, y);
            double[] k2 = f.apply(t + stepSize / 2.0, addVectors(y, scaleVector(k1, stepSize / 2.0)));
            double[] k3 = f.apply(t + stepSize / 2.0, addVectors(y, scaleVector(k2, stepSize / 2.0)));
            double[] k4 = f.apply(t + stepSize, addVectors(y, scaleVector(k3, stepSize)));

            for (int j = 0; j < dim; j++) {
                y[j] += (stepSize / 6.0) * (k1[j] + 2 * k2[j] + 2 * k3[j] + k4[j]);
            }

            t = roundTime(t + stepSize);
            values[i + 1][0] = t;
            for (int j = 0; j < dim; j++) {
                values[i + 1][j + 1] = y[j];
            }
        }

        return values;
    }


}
