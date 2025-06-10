package executables.solvers;

import executables.Constants;

import java.util.Arrays;
import java.util.function.BiFunction;

import static executables.solvers.ODEUtility.*;

/**
 * An RKF 45 implementation to increase accuracy compared to RK4 solver
 *
 */
public class RKF45Solver implements ODESolver {

    // Maximum allowed step size to prevent excessive jumps
    private static final double MAX_STEP_SIZE = 1.0;

    // Safety factor for step size control
    private static final double SAFETY_FACTOR = 0.84;

    // Minimum and maximum scaling factors for step size adjustment
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 5.0;

    @Override
    public double[][] solve(BiFunction<Double, double[], double[]> f, double t0, double[] y0,
                            double initialStepSize, int steps, BiFunction<Double, double[], Boolean> stopCondition) {

        int dim = y0.length;
        double[][] values = initStorage(steps, t0, y0);

        // Limit initial step size to prevent excessive jumps
        double stepSize = Math.min(initialStepSize, MAX_STEP_SIZE);
        int i = 0;
        double t = t0;
        double[] y = Arrays.copyOf(y0, dim);

        while (i < steps) {
            if (stopCondition != null && stopCondition.apply(t, y)) {
                return Arrays.copyOf(values, i + 1);
            }

            // Calculate the six k values for the RKF45 method
            double[] k1 = f.apply(t, y);
            double[] k2 = f.apply(t + 0.25 * stepSize,
                    addVectors(y, scaleVector(k1, 0.25 * stepSize)));

            double[] k3 = f.apply(t + (3.0 / 8.0) * stepSize,
                    addVectors(y, addVectors(
                        scaleVector(k1, (3.0 / 32.0) * stepSize),
                        scaleVector(k2, (9.0 / 32.0) * stepSize))));

            double[] k4 = f.apply(t + (12.0 / 13.0) * stepSize,
                    addVectors(y, addVectors(
                        addVectors(
                            scaleVector(k1, (1932.0 / 2197.0) * stepSize),
                            scaleVector(k2, (-7200.0 / 2197.0) * stepSize)),
                        scaleVector(k3, (7296.0 / 2197.0) * stepSize))));

            double[] k5 = f.apply(t + stepSize,
                    addVectors(y, addVectors(
                        addVectors(
                            addVectors(
                                scaleVector(k1, (439.0 / 216.0) * stepSize),
                                scaleVector(k2, -8.0 * stepSize)),
                            scaleVector(k3, (3680.0 / 513.0) * stepSize)),
                        scaleVector(k4, (-845.0 / 4104.0) * stepSize))));

            double[] k6 = f.apply(t + 0.5 * stepSize,
                    addVectors(y, addVectors(
                        addVectors(
                            addVectors(
                                addVectors(
                                    scaleVector(k1, (-8.0 / 27.0) * stepSize),
                                    scaleVector(k2, 2.0 * stepSize)),
                                scaleVector(k3, (-3544.0 / 2565.0) * stepSize)),
                            scaleVector(k4, (1859.0 / 4104.0) * stepSize)),
                        scaleVector(k5, (-11.0 / 40.0) * stepSize))));

            // Calculate 4th order solution
            double[] yNext = new double[dim];
            for (int j = 0; j < dim; j++) {
                yNext[j] = y[j] +
                        (25.0 / 216.0) * k1[j] * stepSize +
                        (1408.0 / 2565.0) * k3[j] * stepSize +
                        (2197.0 / 4104.0) * k4[j] * stepSize -
                        (1.0 / 5.0) * k5[j] * stepSize;
            }

            // Calculate 5th order solution
            double[] zNext = new double[dim];
            for (int j = 0; j < dim; j++) {
                zNext[j] = y[j] +
                        (16.0 / 135.0) * k1[j] * stepSize +
                        (6656.0 / 12825.0) * k3[j] * stepSize +
                        (28561.0 / 56430.0) * k4[j] * stepSize -
                        (9.0 / 50.0) * k5[j] * stepSize +
                        (2.0 / 55.0) * k6[j] * stepSize;
            }

            double err = 0;
            for (int j = 0; j < dim; j++) {
                double e = Math.abs(zNext[j] - yNext[j]);
                err += e * e;
            }
            err = Math.sqrt(err / dim);

            double tol = Constants.TOLERANCE;

            double s;
            if (err < 1e-15) {
                s = MAX_SCALE;
            } else {
                s = SAFETY_FACTOR * Math.pow(tol / err, 0.25);
                s = Math.max(MIN_SCALE, Math.min(MAX_SCALE, s));
            }

            if (err <= tol) {
                t += stepSize;
                y = Arrays.copyOf(zNext, dim);
                values[i + 1][0] = t;
                for (int j = 0; j < dim; j++) {
                    values[i + 1][j + 1] = y[j];
                }
                i++;

                stepSize = Math.min(stepSize * s, MAX_STEP_SIZE);
            } else {
                stepSize *= s;
            }
        }
        return values;
    }
}
