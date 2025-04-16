package executables.solvers;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ODESolver {
    double[][] solve(
            BiFunction<Double, double[], double[]> f,
            double t0,
            double[] y0,
            double stepSize,
            int steps,
            BiFunction<Double, double[], Boolean> stopCondition // optional for some solvers
    );
}
