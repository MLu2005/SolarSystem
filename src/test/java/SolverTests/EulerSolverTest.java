package SolverTests;

import static org.junit.jupiter.api.Assertions.*;

import executables.solvers.EulerSolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

/**
 * Unit tests for the EulerSolver class.
 * Tests accuracy, robustness, and edge cases.
 */
class EulerSolverTest {

    private EulerSolver solver;

    @BeforeEach
    void setUp() {
        solver = new EulerSolver();
    }

    /**
     * Tests Euler's method on the simple ODE dy/dx = y with y(0) = 1.
     * The exact solution is y = e^x. This test checks for numerical accuracy,
     * allowing for a generous tolerance since Euler's method is not very precise.
     */
    @Test
    void testExponentialGrowthODE() {
        BiFunction<Double, double[], double[]> f = (x, y) -> new double[]{y[0]};
        double[] y0 = {1.0};
        double stepSize = 0.1;
        int steps = 10;

        double[][] result = solver.solve(f, 0.0, y0.clone(), stepSize, steps, null);

        assertEquals(steps + 1, result.length);
        for (int i = 0; i <= steps; i++) {
            double expected = Math.exp(i * stepSize);
            double actual = result[i][1];
            double tolerance = 0.15;
            assertEquals(expected, actual, tolerance, "Mismatch at step " + i);
        }
    }

    /**
     * Verifies that when step count is zero, the solver returns only the initial state.
     */
    @Test
    void testZeroStepsReturnsInitialStateOnly() {
        BiFunction<Double, double[], double[]> f = (x, y) -> new double[]{1.0};
        double[] y0 = {5.0};

        double[][] result = solver.solve(f, 0.0, y0.clone(), 0.1, 0, null);

        assertEquals(1, result.length);
        assertEquals(0.0, result[0][0]);
        assertEquals(5.0, result[0][1]);
    }

    /**
     * Checks that the solver works properly with zero-dimensional input (empty system),
     * and only returns time steps with no variables.
     */
    @Test
    void testZeroDimensionInput() {
        BiFunction<Double, double[], double[]> f = (x, y) -> new double[0];
        double[] y0 = new double[0];

        double[][] result = solver.solve(f, 0.0, y0.clone(), 0.1, 5, null);

        assertEquals(6, result.length);
        for (double[] row : result) {
            assertEquals(1, row.length); // Only time column
        }
    }

    /**
     * Verifies that the stop condition is respected and terminates integration early
     * when a specific time is reached (here, t >= 0.3).
     */
    @Test
    void testStopConditionStopsEarly() {
        BiFunction<Double, double[], double[]> f = (x, y) -> new double[]{1.0};
        double[] y0 = {0.0};

        BiFunction<Double, double[], Boolean> stop = (x, y) -> x >= 0.3;

        double[][] result = solver.solve(f, 0.0, y0.clone(), 0.1, 10, stop);

        assertTrue(result.length <= 4);
        assertEquals(0.3, result[result.length - 1][0], 1e-10);
    }

    /**
     * Tests that the solver handles a system of two ODEs correctly (harmonic oscillator).
     * Checks that values are not NaN and structure is consistent.
     */
    @Test
    void testMultiDimensionalODE() {
        BiFunction<Double, double[], double[]> f = (x, y) -> new double[]{y[1], -y[0]};
        double[] y0 = {1.0, 0.0};

        double[][] result = solver.solve(f, 0.0, y0.clone(), 0.1, 10, null);

        assertEquals(11, result.length);
        assertEquals(3, result[0].length); // t, y1, y2

        for (double[] row : result) {
            for (double val : row) {
                assertFalse(Double.isNaN(val));
            }
        }
    }
}
