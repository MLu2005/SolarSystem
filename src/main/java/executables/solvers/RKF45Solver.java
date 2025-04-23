package executables.solvers;


import java.util.Arrays;
import java.util.function.BiFunction;

import static executables.solvers.ODEUtility.*;

/**
 * An RKF 45 implementation to increase accuracy compared to RK4 solver
 *
 *
 * param
 *We decided against an RK45 implementation as it may be more efficient, but learning about fsal
 * bookkeeping would have taken away resources from more relevant topics
 * For transparency its based on <a href="https://maths.cnam.fr/IMG/pdf/RungeKuttaFehlbergProof.pdf">this proof/a>
 * In this proof there is a
 */
public class RKF45Solver implements ODESolver {

    public double[][] solve(BiFunction<Double, double[], double[]> f, double t0, double[] y0,
                            double initialStepSize, int steps, BiFunction<Double, double[], Boolean> stopCondition) {

        int dim =y0.length;
        double[][] values = initStorage(steps, t0,y0);

        double optimal_step_size = initialStepSize;
        int i = 0;
        double t = t0;

        while(i < steps) {
            if (stopCondition != null && stopCondition.apply(t, y0)) {
                return Arrays.copyOf(values, i + 1);
            }

            double[] k1 = f.apply(t, y0);
            double[] k2 = f.apply(t + 0.25*optimal_step_size, addVectors(y0, scaleVector(k1, 0.25*optimal_step_size)));

            double[] k3 = f.apply(t + (3.0/8.0)*optimal_step_size, addVectors(y0, addVectors
                    (scaleVector(k1,(3.0 /32.0)*optimal_step_size),scaleVector(k2, (9.0/32.0)*optimal_step_size))));

            double[] k4 = f.apply(t + (12.0/13.0) * optimal_step_size, addVectors(y0, addVectors
                    (addVectors(scaleVector(k1, (1932.0/2197.0)*optimal_step_size),
                    scaleVector(k2, (-7200.0/2197.0)*optimal_step_size)), scaleVector(k3, (7296.0/2197.0)*optimal_step_size))));

            double[] k5 = f.apply(t + optimal_step_size, addVectors(y0, addVectors(addVectors(
                    addVectors(scaleVector(k1, (439.0/216.0)*optimal_step_size), scaleVector(k2, -8.0*optimal_step_size)),
                            scaleVector(k3, (3680.0/513.0)*optimal_step_size)), scaleVector(k4,(-845.0/4104.0)*optimal_step_size))));

            double[] k6 = f.apply(t + 0.5 * optimal_step_size, addVectors(y0, addVectors(addVectors(
                    addVectors(addVectors(scaleVector(k1, (-8.0/27.0)*optimal_step_size), scaleVector(k2, 2.0*optimal_step_size)),
                            scaleVector(k3, (-3544.0/2565.0)*optimal_step_size)), scaleVector(k4, (1859.0/4104.0)*optimal_step_size)),
                    scaleVector(k5, (-11.0/40.0)*optimal_step_size))));

            double[] yNext = new double[dim];
            for (int j = 0; j < dim; j++) {
                yNext[j] = y0[j] +
                        (25.0/216.0)*k1[j] +
                        (1408.0/2565.0)*k3[j] +
                        (2197.0/4104.0)*k4[j] -
                        (1.0/5.0)*k5[j];
            }

            double[] zNext = new double[dim];
            for (int j = 0; j < dim; j++) {
                zNext[j] = y0[j] +
                        (16.0/135.0)*k1[j] +
                        (6656.0/12825.0)*k3[j] +
                        (28561.0/56430.0)*k4[j] -
                        (9.0/50.0)*k5[j] +
                        (2.0/55.0)*k6[j];
            }

            double err = 0;
            for (int j = 0; j < dim; j++) {
                double e = zNext[j] - yNext[j];
                err += e*e;
            }
            err = Math.sqrt(err / dim);

            double stepSizeTol = optimal_step_size*Constants.TOLERANCE;
            double s;

            if (err == 0) {
                // we need a large jump, but also not to large. I believe matlab uses 4, but im unsure
                s = 5.0;
            } else {
                s = 0.84 * Math.pow(stepSizeTol / err, 0.25);
            }
            s = Math.max(0.1, Math.min(5.0, s));

            if (err <= stepSizeTol) {

                t += optimal_step_size;
                y0 = Arrays.copyOf(zNext, dim);  // use the 5th‑order solution, as it has a better local error
                values[i+1][0] = t;
                for (int j = 0; j < dim; j++) {
                    values[i+1][j+1] = y0[j];
                }
                i++;
            }
            optimal_step_size *= s;
        }
        return values;
    }
}
