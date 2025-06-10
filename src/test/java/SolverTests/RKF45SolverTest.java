package SolverTests;

import com.example.utilities.solvers.RKF45Solver;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the RKF45Solver class.
 * Verifies correct integration results for known ODEs and edge cases.
 * Tests the adaptive step size control specific to RKF45.
 * 
 * Note: RKF45 uses adaptive step sizes, so we need to check the actual time values
 * in the result array rather than assuming fixed time steps.
 */
public class RKF45SolverTest {

    private final RKF45Solver solver = new RKF45Solver();

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

        // Debug output to understand what's happening
        System.out.println("[DEBUG_LOG] Linear ODE test results:");
        for (int i = 0; i <= steps; i++) {
            System.out.println("[DEBUG_LOG] Step " + i + ": t=" + result[i][0] + ", y=" + result[i][1]);
        }

        // With adaptive step size, we need to check the actual time values
        for (int i = 0; i <= steps; i++) {
            double t = result[i][0]; // Actual time
            double y = result[i][1]; // Actual solution
            double expected = t;     // For dy/dt = 1, y(t) = t
            assertEquals(expected, y, 1e-6, "y(t) should equal t for dy/dt = 1");
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

        // Compare y(t) with e^t at the actual time points
        for (int i = 0; i <= steps; i++) {
            double t = result[i][0]; // Actual time
            double expected = Math.exp(t);
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
     * Tests a system of two ODEs: harmonic oscillator
     * d²x/dt² + x = 0 → x(t) = cos(t), v(t) = -sin(t)
     * Rewritten as first-order system:
     * dx/dt = v
     * dv/dt = -x
     */
    @Test
    void testHarmonicOscillator() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{y[1], -y[0]};
        double t0 = 0.0;
        double[] y0 = {1.0, 0.0}; // x(0) = 1, v(0) = 0
        double h = 0.01; // Use smaller step size for better accuracy
        int steps = 20;

        double[][] result = solver.solve(f, t0, y0.clone(), h, steps, null);

        // Check solution at actual time points
        for (int i = 0; i <= steps; i++) {
            double t = result[i][0]; // Actual time
            double expectedX = Math.cos(t);
            double expectedV = -Math.sin(t);

            assertEquals(expectedX, result[i][1], 1e-2, "x(t) should approximate cos(t)");
            assertEquals(expectedV, result[i][2], 1e-2, "v(t) should approximate -sin(t)");
        }
    }

    /**
     * Tests adaptive step size behavior with a stiff ODE
     * dy/dt = -50y, y(0) = 1 → y(t) = e^(-50t)
     * This equation has a rapidly decaying solution that requires small step sizes
     */
    @Test
    void testAdaptiveStepSizeWithStiffODE() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{-50.0 * y[0]};
        double t0 = 0.0;
        double[] y0 = {1.0};
        double initialH = 0.001; // Use much smaller initial step size for stiff equation
        int steps = 10;

        double[][] result = solver.solve(f, t0, y0.clone(), initialH, steps, null);

        // The solver should complete successfully despite the stiffness
        assertEquals(steps + 1, result.length, "Solver should complete all steps");

        // Check each point against the analytical solution
        for (int i = 0; i <= steps; i++) {
            double t = result[i][0]; // Actual time
            double expectedY = Math.exp(-50.0 * t);
            assertEquals(expectedY, result[i][1], 1e-2, 
                    "Value at t=" + t + " should match analytical solution");
        }
    }

    /**
     * Tests that the solver can handle a simple ODE with a very small step size
     */
    @Test
    void testSmallStepSize() {
        BiFunction<Double, double[], double[]> f = (t, y) -> new double[]{1.0};
        double t0 = 0.0;
        double[] y0 = {0.0};
        double h = 1e-6; // Very small step size
        int steps = 5;

        double[][] result = solver.solve(f, t0, y0.clone(), h, steps, null);

        // Check that we have the expected number of steps
        assertEquals(steps + 1, result.length);

        // Check that the solution is correct at each time point
        for (int i = 0; i <= steps; i++) {
            double t = result[i][0];
            assertEquals(t, result[i][1], 1e-9, "y(t) should equal t for dy/dt = 1");
        }
    }
}
