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
 * Simulates a basic system with two bodies to validate energy calculations.
 */
class OrbitalEnergyMonitorTest {

    private List<CelestialBody> bodies;
    private OrbitalEnergyMonitor monitor;

    /**
     * Creates a simple 2-body system: Sun and Earth.
     * Units are in km and km/s, masses in kg.
     */
    @BeforeEach
    void setUp() {
        bodies = new ArrayList<>();

        // Sun: mass = 1.989e30 kg, at origin
        CelestialBody sun = new CelestialBody(
                "Sun",
                1.989e30,
                new Vector3D(0, 0, 0),
                new Vector3D(0, 0, 0)
        );

        // Earth: mass = 5.972e24 kg, at 1 AU (approx 1.496e8 km), orbital velocity ~29.78 km/s
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
     * Verifies that energy is recorded and lists grow as expected.
     */
    @Test
    void testEnergyIsRecordedCorrectly() {
        monitor.recordEnergy(0.0);
        monitor.recordEnergy(10000.0);

        assertEquals(2, monitor.energyHistory.size(), "Energy history should contain 2 entries");
        assertEquals(2, monitor.timeHistory.size(), "Time history should contain 2 entries");
    }

    /**
     * Tests that initial and current energy are the same if no changes occur.
     */
    @Test
    void testInitialAndCurrentEnergyMatchWithoutChange() {
        monitor.recordEnergy(0.0);
        monitor.recordEnergy(500.0);

        double initial = monitor.getInitialEnergy();
        double current = monitor.getCurrentEnergy();

        assertEquals(initial, current, 1e-6, "Initial and current energy should be equal if state is unchanged");
    }

    /**
     * Simulates velocity change and checks that drift is non-zero.
     */
    @Test
    void testEnergyDriftAfterVelocityChange() {
        monitor.recordEnergy(0.0);

        // Boost Earth's velocity (simulate external disturbance or numerical error)
        CelestialBody earth = bodies.get(1);
        earth.setVelocity(new Vector3D(0, 40.0, 0)); // previously ~29.78 km/s

        monitor.recordEnergy(1000.0);

        double drift = monitor.getRelativeEnergyDrift();
        assertNotEquals(0.0, drift, "Energy drift should be non-zero after velocity change");
    }

    /**
     * Prints the drift to visually inspect it (manual check).
     */
    @Test
    void testPrintStatusDoesNotCrash() {
        monitor.recordEnergy(0.0);
        monitor.recordEnergy(2000.0);
        monitor.printStatus(); // Should print to console without errors
    }
}
