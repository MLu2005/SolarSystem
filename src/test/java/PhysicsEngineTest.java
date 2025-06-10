import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.PhysicsEngine;
import com.example.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PhysicsEngine class using RK4 integration.
 * These tests verify the correctness of gravitational physics over short timesteps,
 * assuming Newtonian gravity and a closed system with no external forces.
 */
class PhysicsEngineTest {

    private PhysicsEngine engine;
    private CelestialBody earth;
    private CelestialBody moon;

    private static final double G = Constants.G;

    /**
     * Sets up the simulation with Earth and Moon system before each test.
     * Earth is fixed at origin, Moon is offset and given a tangential velocity.
     */
    @BeforeEach
    void setUp() {
        engine = new PhysicsEngine();

        earth = new CelestialBody("Earth", 5.972e24,
                new Vector3D(0, 0, 0), new Vector3D(0, 0, 0));

        moon = new CelestialBody("Moon", 7.348e22,
                new Vector3D(384400, 0, 0), new Vector3D(0, 1.022, 0));

        engine.addBody(earth);
        engine.addBody(moon);
    }

    /**
     * Tests whether the celestial bodies are correctly stored in the engine.
     */
    @Test
    void testBodiesAreAddedCorrectly() {
        List<CelestialBody> bodies = engine.getBodies();
        assertEquals(2, bodies.size(), "Engine should contain exactly 2 bodies.");
        assertTrue(bodies.contains(earth), "Earth should be present in the engine.");
        assertTrue(bodies.contains(moon), "Moon should be present in the engine.");
    }

    /**
     * Tests that the simulation step runs without throwing exceptions.
     */
    @Test
    void testStepDoesNotCrash() {
        assertDoesNotThrow(() -> engine.step(60),
                "Simulation step with dt=60s should not throw an exception.");
    }

    /**
     * Tests that the positions of celestial bodies change after a simulation step.
     */
    @Test
    void testPositionsChangeAfterStep() {
        Vector3D initialEarthPos = earth.getPosition();
        Vector3D initialMoonPos = moon.getPosition();

        engine.step(60); // advance by 1 minute

        assertNotEquals(initialEarthPos, earth.getPosition(),
                "Earth's position should change after step.");
        assertNotEquals(initialMoonPos, moon.getPosition(),
                "Moon's position should change after step.");
    }

    /**
     * Tests that the accelerations of celestial bodies are computed and updated.
     */
    @Test
    void testAccelerationIsUpdated() {
        engine.step(60);

        Vector3D earthAcc = earth.getAcceleration();
        Vector3D moonAcc = moon.getAcceleration();

        assertNotNull(earthAcc, "Earth should have a non-null acceleration.");
        assertNotNull(moonAcc, "Moon should have a non-null acceleration.");

        assertTrue(earthAcc.magnitude() > 0,
                "Earth should experience non-zero acceleration due to Moon.");
        assertTrue(moonAcc.magnitude() > 0,
                "Moon should experience non-zero acceleration due to Earth.");
    }

    /**
     * Tests that total momentum is approximately conserved after a short simulation step.
     * RK4 is not symplectic, so this is only an approximate check.
     */
    @Test
    void testApproximateMomentumConservation() {
        Vector3D initialMomentum = earth.getVelocity().scale(earth.getMass())
                .add(moon.getVelocity().scale(moon.getMass()));

        engine.step(60);

        Vector3D finalMomentum = earth.getVelocity().scale(earth.getMass())
                .add(moon.getVelocity().scale(moon.getMass()));

        double diff = initialMomentum.subtract(finalMomentum).magnitude();

        assertTrue(diff < 1e16, "Total momentum should be approximately conserved over one RK4 step.");
    }

    /**
     * Tests that total mechanical energy (kinetic + potential) is approximately conserved
     * over several short RK4 steps. Some drift is acceptable due to RK4 limitations.
     */
    @Test
    void testApproximateEnergyConservation() {
        double initialKinetic = 0.5 * earth.getMass() * earth.getVelocity().magnitudeSquared()
                + 0.5 * moon.getMass() * moon.getVelocity().magnitudeSquared();

        double distance = earth.getPosition().subtract(moon.getPosition()).magnitude();
        double initialPotential = -G * earth.getMass() * moon.getMass() / distance;

        double initialEnergy = initialKinetic + initialPotential;

        for (int i = 0; i < 10; i++) {
            engine.step(60);
        }

        double finalKinetic = 0.5 * earth.getMass() * earth.getVelocity().magnitudeSquared()
                + 0.5 * moon.getMass() * moon.getVelocity().magnitudeSquared();

        double newDistance = earth.getPosition().subtract(moon.getPosition()).magnitude();
        double finalPotential = -G * earth.getMass() * moon.getMass() / newDistance;

        double finalEnergy = finalKinetic + finalPotential;

        double energyDrift = Math.abs(finalEnergy - initialEnergy);
        assertTrue(energyDrift < 1e24,
                "Total energy should remain approximately constant over 10 steps of RK4.");
    }

    /**
     * Tests that a symmetric configuration of two equal-mass bodies moving toward each other
     * behaves symmetrically (approximate mirror velocities and positions).
     */
    @Test
    void testSymmetricBodiesBehaveSymmetrically() {
        CelestialBody bodyA = new CelestialBody("A", 1e20,
                new Vector3D(-100, 0, 0), new Vector3D(0, 1, 0));
        CelestialBody bodyB = new CelestialBody("B", 1e20,
                new Vector3D(100, 0, 0), new Vector3D(0, -1, 0));

        PhysicsEngine symmetricEngine = new PhysicsEngine();
        symmetricEngine.addBody(bodyA);
        symmetricEngine.addBody(bodyB);

        symmetricEngine.step(60);

        Vector3D posA = bodyA.getPosition();
        Vector3D posB = bodyB.getPosition();

        assertEquals(posA.x, -posB.x, 1e-3, "Positions should be symmetric on X-axis.");
        assertEquals(posA.y, -posB.y, 1e-3, "Positions should be symmetric on Y-axis.");
    }
}
