import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.OrbitalEnergyMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the OrbitalEnergyMonitor class.
 * Simulates two-body and edge cases to verify energy calculations.
 */
class OrbitalEnergyMonitorTest {

    private List<CelestialBody> bodies;
    private OrbitalEnergyMonitor monitor;

    /**
     * Sets up a basic Sun–Earth system for testing.
     * Units: km (distance), km/s (velocity), kg (mass).
     */
    @BeforeEach
    void setUp() {
        bodies = new ArrayList<>();

        CelestialBody sun = new CelestialBody(
                "Sun",
                1.989e30,
                new Vector3D(0, 0, 0),
                new Vector3D(0, 0, 0)
        );

        CelestialBody earth = new CelestialBody(
                "Earth",
                5.972e24,
                new Vector3D(1.496e8, 0, 0),
                new Vector3D(0, 29.78, 0)
        );

        bodies.add(sun);
        bodies.add(earth);

        monitor = new OrbitalEnergyMonitor(bodies);
    }

    /**
     * Tests that initial and current energy are equal when no change occurs.
     */
    @Test
    void testEnergyStaysConstantWithoutChange() {
        monitor.recordEnergy(0.0);
        monitor.recordEnergy(1000.0);

        assertEquals(
                monitor.getInitialEnergy(),
                monitor.getCurrentEnergy(),
                1e-6,
                "Energy should remain the same if system state is unchanged"
        );

        assertEquals(
                0.0,
                monitor.getRelativeEnergyDrift(),
                1e-6,
                "Energy drift should be 0 when no changes occur"
        );
    }

    /**
     * Tests that energy drift becomes non-zero if a velocity is artificially modified.
     */
    @Test
    void testEnergyDriftWhenVelocityChanges() {
        monitor.recordEnergy(0.0);

        CelestialBody earth = bodies.get(1);
        earth.setVelocity(new Vector3D(0, 40.0, 0)); // Simulated disturbance

        monitor.recordEnergy(500.0);

        assertNotEquals(
                0.0,
                monitor.getRelativeEnergyDrift(),
                1e-10,
                "Energy drift should be non-zero after velocity change"
        );
    }

    /**
     * Tests that a zero-mass body does not cause crashes in energy computation.
     */
    @Test
    void testZeroMassBodySafeToInclude() {
        CelestialBody ghost = new CelestialBody("Ghost", 0.0, new Vector3D(1, 0, 0), new Vector3D(0, 0, 0));
        bodies.add(ghost);

        assertDoesNotThrow(() -> {
            monitor.recordEnergy(0.0);
            monitor.recordEnergy(1000.0);
        });
    }

    /**
     * Tests that two bodies at the same position (zero distance) don't cause division errors.
     */
    @Test
    void testZeroDistanceDoesNotCrash() {
        CelestialBody clone = new CelestialBody(
                "EarthClone",
                5.972e24,
                new Vector3D(1.496e8, 0, 0),
                new Vector3D(0, 29.78, 0)
        );
        bodies.add(clone);

        assertDoesNotThrow(() -> {
            monitor.recordEnergy(0.0);
            monitor.recordEnergy(1000.0);
        });
    }

    /**
     * Tests system behavior when a high-velocity body is introduced.
     */
    @Test
    void testHighVelocityBodyDoesNotBreakComputation() {
        CelestialBody fast = new CelestialBody(
                "Speedy",
                1e20,
                new Vector3D(3e8, 0, 0),
                new Vector3D(1e5, -1e5, 0)
        );
        bodies.add(fast);

        assertDoesNotThrow(() -> {
            monitor.recordEnergy(0.0);
            monitor.recordEnergy(5000.0);
        });
    }

    /**
     * Tests that a large simulation time value doesn't affect energy logic.
     */
    @Test
    void testLargeTimeStepIsHandledCorrectly() {
        monitor.recordEnergy(0.0);
        monitor.recordEnergy(1e9); // Simulated very long time step

        assertEquals(
                0.0,
                monitor.getRelativeEnergyDrift(),
                1e-6,
                "Large time step alone should not cause drift if state is constant"
        );
    }

    /**
     * Explicitly tests that drift formula matches the expected manual computation.
     */
    @Test
    void testDriftMatchesManualComputation() {
        monitor.recordEnergy(0.0);

        // Introduce small change
        bodies.get(1).setVelocity(new Vector3D(0, 29.90, 0));

        monitor.recordEnergy(2000.0);

        double initial = monitor.getInitialEnergy();
        double current = monitor.getCurrentEnergy();
        double expectedDrift = (current - initial) / Math.abs(initial);

        assertEquals(
                expectedDrift,
                monitor.getRelativeEnergyDrift(),
                1e-9,
                "Drift formula should match manual computation"
        );
    }
}
