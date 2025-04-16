package executables.solvers;

import java.util.Arrays;
import java.util.function.BiFunction;

public class EulerSolver extends AbstractODESolver {

    @Override
    public double[][] solve(
            BiFunction<Double, double[], double[]> f,
            double x,
            double[] y0,
            double stepSize,
            int steps,
            BiFunction<Double, double[], Boolean> stopCondition
    ) {
        int dim = y0.length;
        double[] y = Arrays.copyOf(y0, dim);
        double[][] values = initStorage(steps, x, y);

        for (int i = 0; i < steps; i++) {
            if (stopCondition != null && stopCondition.apply(x, y)) {
                return Arrays.copyOf(values, i + 1);
            }

            double[] dydx = f.apply(x, y);
            for (int k = 0; k < dim; k++) {
                y[k] += stepSize * dydx[k];
            }

            x = roundTime(x + stepSize);
            values[i + 1][0] = x;
            for (int j = 0; j < dim; j++) {
                values[i + 1][j + 1] = y[j];
            }
        }

        return values;
    }
}
