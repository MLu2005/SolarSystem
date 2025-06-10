package executables.solvers;

import executables.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static executables.solvers.ODEUtility.*;

/**
 * An RKF45 implementation with adaptive step sizing for improved accuracy.
 */
public class RKF45Solver implements ODESolver {

    // Maximum allowed step size to prevent excessive jumps
    private static final double MAX_STEP_SIZE = 1.0;

    // Safety factor for step size control
    private static final double SAFETY_FACTOR = 0.84;

    // Minimum and maximum scaling factors for step size adjustment
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 5.0;

    // Maximum time to integrate (override 'steps' with reasonable t span) that according to
    // experiments done during development does not interfere with the orbital insertion
    private static final double MAX_TIME = 1000.0;

    @Override
    public double[][] solve(BiFunction<Double, double[], double[]> f, double t0, double[] y0,
                            double initialStepSize, int unusedSteps, BiFunction<Double, double[], Boolean> stopCondition) {
        int dim = y0.length;
        List<double[]> resultList = new ArrayList<>();

        double t = t0;
        double stepSize = Math.min(initialStepSize, MAX_STEP_SIZE);
        double[] y = Arrays.copyOf(y0, dim);

        resultList.add(mergeTimeAndState(t, y));

        while (t < MAX_TIME) {
            if (stopCondition != null && stopCondition.apply(t, y)) {
                break;
            }
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

            double[] yNext4 = new double[dim];
            for (int j = 0; j < dim; j++) {
                yNext4[j] = y[j]
                        + stepSize * ((25.0 / 216.0) * k1[j]
                        + (1408.0 / 2565.0) * k3[j]
                        + (2197.0 / 4104.0) * k4[j]
                        - (1.0 / 5.0) * k5[j]);
            }

            double[] yNext5 = new double[dim];
            for (int j = 0; j < dim; j++) {
                yNext5[j] = y[j]
                        + stepSize * ((16.0 / 135.0) * k1[j]
                        + (6656.0 / 12825.0) * k3[j]
                        + (28561.0 / 56430.0) * k4[j]
                        - (9.0 / 50.0) * k5[j]
                        + (2.0 / 55.0) * k6[j]);
            }

            // -> Error estimate between 5th and 4th order
            double err = 0.0;
            for (int j = 0; j < dim; j++) {
                double diff = yNext5[j] - yNext4[j];
                err += diff * diff;
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
                y = Arrays.copyOf(yNext5, dim);
                resultList.add(mergeTimeAndState(t, y));
            }


            stepSize = Math.min(stepSize * s, MAX_STEP_SIZE);


            if (t + stepSize > MAX_TIME) {
                stepSize = MAX_TIME - t;
            }
        }

        return resultList.toArray(new double[0][]);
    }

    /**
     * Combines time and state into one array: [t, y0, y1, ...]
     */
    private double[] mergeTimeAndState(double t, double[] y) {
        double[] result = new double[y.length + 1];
        result[0] = t;
        System.arraycopy(y, 0, result, 1, y.length);
        return result;
    }

    public static double[] solveStep(
            BiFunction<Double, double[], double[]> f,
            double t,
            double[] y,
            double stepSize
    ) {
        double[] result = new double[y.length];
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

        for (int j = 0; j < y.length; j++) {
            result[j] = y[j] +
                    (16.0 / 135.0) * k1[j] * stepSize +
                    (6656.0 / 12825.0) * k3[j] * stepSize +
                    (28561.0 / 56430.0) * k4[j] * stepSize -
                    (9.0 / 50.0) * k5[j] * stepSize +
                    (2.0 / 55.0) * k6[j] * stepSize;
        }
        return result;
    }
}
