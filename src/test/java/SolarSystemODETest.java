import com.example.solar_system.CelestialBody;
import com.example.utilities.SolarSystemODE;
import com.example.utilities.Vector3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SolarSystemODE class.
 * Verifies the correct computation of velocities and accelerations
 * for celestial bodies based on gravitational interaction.
 */
class SolarSystemODETest {

    private CelestialBody sun;
    private CelestialBody earth;

    @BeforeEach
    void setUp() {
        // Sun (massive body at origin, not moving)
        sun = new CelestialBody(
                "Sun",
                1.989e30,
                new Vector3D(0, 0, 0),
                new Vector3D(0, 0, 0)
        );

        // Earth (smaller mass, some position/velocity)
        earth = new CelestialBody(
                "Earth",
                5.972e24,
                new Vector3D(1.496e8, 0, 0),
                new Vector3D(0, 29.78, 0)
        );
    }

    /**
     * Tests that a single body results in zero velocity and acceleration change.
     */
    @Test
    void testOneBodyResultsInZeroAcceleration() {
        List<CelestialBody> bodies = List.of(sun);
        BiFunction<Double, double[], double[]> ode = SolarSystemODE.generateODE(bodies);

        double[] state = new double[]{
                sun.getPosition().x, sun.getPosition().y, sun.getPosition().z,
                sun.getVelocity().x, sun.getVelocity().y, sun.getVelocity().z
        };

        double[] derivatives = ode.apply(0.0, state);

        // No velocity change or acceleration for one isolated body
        assertArrayEquals(new double[]{0, 0, 0, 0, 0, 0}, derivatives, 1e-9);
    }

    /**
     * Tests that Earth is correctly attracted by the Sun and not the other way around (Sun fixed).
     */
    @Test
    void testTwoBodyGravityDirection() {
        List<CelestialBody> bodies = List.of(sun, earth);
        BiFunction<Double, double[], double[]> ode = SolarSystemODE.generateODE(bodies);

        double[] state = new double[]{
                sun.getPosition().x, sun.getPosition().y, sun.getPosition().z,
                sun.getVelocity().x, sun.getVelocity().y, sun.getVelocity().z,

                earth.getPosition().x, earth.getPosition().y, earth.getPosition().z,
                earth.getVelocity().x, earth.getVelocity().y, earth.getVelocity().z
        };

        double[] derivatives = ode.apply(0.0, state);

        // Earth index
        int earthIdx = 6;

        // Earth velocity should match input
        assertEquals(0, derivatives[earthIdx], 1e-9);       // dx/dt
        assertEquals(29.78, derivatives[earthIdx + 1], 1e-9); // dy/dt
        assertEquals(0, derivatives[earthIdx + 2], 1e-9);     // dz/dt

        // Earth acceleration should point toward the Sun (negative x)
        double ax = derivatives[earthIdx + 3];
        double ay = derivatives[earthIdx + 4];
        double az = derivatives[earthIdx + 5];

        assertTrue(ax < 0, "Acceleration in x should be negative (towards Sun)");
        assertEquals(0.0, ay, 1e-6, "No y acceleration when Sun is along x-axis");
        assertEquals(0.0, az, 1e-6, "No z acceleration in 2D test");
    }

    /**
     * Verifies that mutual gravitational influence between two bodies
     * is symmetric and opposite in Newton's third law.
     */
    @Test
    void testNewtonThirdLawSymmetry() {
        // Symmetric bodies
        CelestialBody a = new CelestialBody("A", 1.0e10, new Vector3D(-1, 0, 0), new Vector3D(0, 0, 0));
        CelestialBody b = new CelestialBody("B", 1.0e10, new Vector3D(+1, 0, 0), new Vector3D(0, 0, 0));
        List<CelestialBody> bodies = List.of(a, b);

        BiFunction<Double, double[], double[]> ode = SolarSystemODE.generateODE(bodies);

        double[] state = new double[]{
                -1.0, 0, 0, 0, 0, 0,
                1.0, 0, 0, 0, 0, 0
        };

        double[] derivatives = ode.apply(0.0, state);

        double axA = derivatives[3]; // acceleration x of A
        double axB = derivatives[9]; // acceleration x of B

        // A and B should experience equal and opposite acceleration
        assertEquals(-axB, axA, 1e-9, "Accelerations should be equal and opposite");
    }

    /**
     * Tests that the derivative size is 6 * number of bodies.
     */
    @Test
    void testDerivativeLengthMatchesBodyCount() {
        List<CelestialBody> bodies = List.of(sun, earth);
        BiFunction<Double, double[], double[]> ode = SolarSystemODE.generateODE(bodies);

        double[] state = new double[12]; // 2 bodies * 6
        double[] derivatives = ode.apply(0.0, state);

        assertEquals(12, derivatives.length, "Output array size must match 6 * number of bodies");
    }
}
