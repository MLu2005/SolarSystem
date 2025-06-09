package SolverTests;

import executables.solvers.RK4Solver;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RK4Solver class.
 * Verifies correct integration results for known ODEs and edge cases.
 */
class RK4SolverTest {

    private final RK4Solver solver = new RK4Solver();

    /**
     * Simple ODE: dy/dt = 1 with y(0) = 0 → solution is y(t) = t
     */
    @Test
    void testLinearODE_dyEquals1() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{1.0};
        double t0 = 0.0;
        double[] y0 = {0.0};
        double h = 0.1;
        int steps = 10;

        double[][] result = solver.solve(f, t0, y0.clone(), h, steps, null);

        // Expected: y = t
        for (int i = 0; i <= steps; i++) {
            double expected = i * h;
            assertEquals(expected, result[i][1], 1e-6, "y(t) should equal t for dy/dt = 1");
        }
    }

    /**
     * ODE: dy/dt = y, y(0) = 1 → exact solution y(t) = e^t
     */
    @Test
    void testExponentialGrowth() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{y[0]};
        double t0 = 0.0;
        double[] y0 = {1.0};
        double h = 0.1;
        int steps = 10;

        double[][] result = solver.solve(f, t0, y0.clone(), h, steps, null);

        // Compare y(t) with e^t
        for (int i = 0; i <= steps; i++) {
            double expected = Math.exp(i * h);
            double actual = result[i][1];
            assertEquals(expected, actual, 1e-3, "y(t) should approximate exp(t)");
        }
    }

    /**
     * Test that solve returns correct shape even with 0 steps.
     */
    @Test
    void testZeroSteps() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{1.0};
        double[][] result = solver.solve(f, 0.0, new double[]{0.0}, 0.1, 0, null);

        assertEquals(1, result.length, "Only initial state should be returned");
        assertEquals(0.0, result[0][0], 1e-9);
        assertEquals(0.0, result[0][1], 1e-9);
    }

    /**
     * Tests RK4 step on dy/dt = t with y(0) = 0.
     * Exact integral over [0, h] is h² / 2
     */
    @Test
    void testSolveStepMatchesIntegralEstimate() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{t};
        double t = 0.0;
        double[] y = {0.0};
        double h = 0.1;

        double[] nextY = solver.solveStep(f, t, y, h);

        double expected = 0.5 * h * h;
        assertEquals(expected, nextY[0], 1e-6, "Step result should match analytical integral of t");
    }

    /**
     * Tests stopCondition: should stop immediately if condition is met.
     */
    @Test
    void testStopConditionStopsEarly() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{1.0};
        BiFunction<Double, double[], Boolean> stopAtStart = (t, y) -> true;

        double[][] result = solver.solve(f, 0.0, new double[]{0.0}, 0.1, 10, stopAtStart);

        assertEquals(1, result.length, "Solver should stop immediately if condition is true at t0");
        assertEquals(0.0, result[0][0], 1e-9);
        assertEquals(0.0, result[0][1], 1e-9);
    }

    /**
     * Tests solveStep for constant derivative dy/dt = 5, y(0) = 2.
     * RK4 should return y(h) ≈ y0 + h * 5
     */
    @Test
    void testSolveStepConstantRate() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{5.0};
        double h = 0.2;
        double[] y0 = {2.0};
        double[] result = solver.solveStep(f, 0.0, y0.clone(), h);

        assertEquals(3.0, result[0], 1e-9, "With constant rate 5, y(h) = y0 + 5*h");
    }
}
