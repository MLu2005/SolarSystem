import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.PhysicsEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PhysicsEngine class.
 */
class PhysicsEngineTest {

    private PhysicsEngine engine;
    private CelestialBody earth;
    private CelestialBody moon;

    @BeforeEach
    void setUp() {
        engine = new PhysicsEngine();

        // Earth is placed at origin with no initial movement.
        earth = new CelestialBody("Earth", 5.972e24,
                new Vector3D(0, 0, 0), new Vector3D(0, 0, 0));

        // Moon is placed 384400 km away with a sideways velocity of about 1 km/s.
        moon = new CelestialBody("Moon", 7.348e22,
                new Vector3D(384400, 0, 0), new Vector3D(0, 1.022, 0));

        engine.addBody(earth);
        engine.addBody(moon);
    }

    // This test checks if the engine stores bodies correctly after we add them.
    @Test
    void testBodiesAreAddedCorrectly() {
        List<CelestialBody> bodies = engine.getBodies();
        assertEquals(2, bodies.size(), "Engine should contain exactly 2 bodies.");
        assertTrue(bodies.contains(earth), "Earth should be present in the engine.");
        assertTrue(bodies.contains(moon), "Moon should be present in the engine.");
    }

    // This test ensures that calling step() doesn’t throw any errors with a reasonable timestep.
    @Test
    void testStepDoesNotCrash() {
        assertDoesNotThrow(() -> engine.step(60), "Simulation step with dt=60s should not crash.");
    }

    // This test checks if body positions actually change after a simulation step.
    @Test
    void testPositionsChangeAfterStep() {
        Vector3D initialEarthPos = earth.getPosition();
        Vector3D initialMoonPos = moon.getPosition();

        engine.step(60); // advance by 1 minute

        assertNotEquals(initialEarthPos, earth.getPosition(), "Earth’s position should change.");
        assertNotEquals(initialMoonPos, moon.getPosition(), "Moon’s position should change.");
    }

    // This test checks if the total momentum is roughly conserved (which should be true in a closed system).
    @Test
    void testApproximateMomentumConservation() {
        Vector3D initialMomentum = earth.getVelocity().scale(earth.getMass())
                .add(moon.getVelocity().scale(moon.getMass()));

        engine.step(60); // 1 minute

        Vector3D finalMomentum = earth.getVelocity().scale(earth.getMass())
                .add(moon.getVelocity().scale(moon.getMass()));

        double diff = initialMomentum.subtract(finalMomentum).magnitude();
        assertTrue(diff < 1e16, "Total momentum should be approximately conserved.");
    }
}

