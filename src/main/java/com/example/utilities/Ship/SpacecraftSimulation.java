package com.example.utilities.Ship;

import com.example.solar_system.CelestialBody;
import com.example.utilities.DataLoader;
import com.example.utilities.Vector3D;
import executables.Constants;
import executables.solvers.RK4Solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * SpacecraftSimulation handles the physics simulation for a spacecraft,
 * including gravitational forces from celestial bodies and thruster effects.
 */
public class SpacecraftSimulation {
    private static final double G = Constants.G; // Gravitational constant in km^3 kg^-1 s^-2

    private final List<CelestialBody> celestialBodies = new ArrayList<>();
    private SpaceShip spacecraft;
    private final Map<String, CelestialBody> bodyMap = new HashMap<>();
    private double timeStep = 600; // Default time step: 1 hour in seconds

    /**
     * Creates a new spacecraft simulation with the specified spacecraft.
     * 
     * @param spacecraft The spacecraft to simulate
     */
    public SpacecraftSimulation(SpaceShip spacecraft) {
        this.spacecraft = spacecraft;
    }

    /**
     * Loads celestial bodies from the specified CSV file.
     * 
     * @param csvFilePath Path to the CSV file containing celestial body data
     */
    public void loadCelestialBodies(String csvFilePath) {
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV(csvFilePath);

        for (CelestialBody body : bodies) {
            celestialBodies.add(body);
            bodyMap.put(body.getName(), body);
        }
    }

    /**
     * Adds a celestial body to the simulation.
     * 
     * @param body The celestial body to add
     */
    public void addCelestialBody(CelestialBody body) {
        celestialBodies.add(body);
        bodyMap.put(body.getName(), body);
    }

    /**
     * Sets the time step for the simulation.
     * 
     * @param timeStep The time step in seconds
     */
    public void setTimeStep(double timeStep) {
        this.timeStep = timeStep;
    }

    /**
     * Gets a celestial body by name.
     * 
     * @param name The name of the celestial body
     * @return The celestial body, or null if not found
     */
    public CelestialBody getCelestialBody(String name) {
        return bodyMap.get(name);
    }

    /**
     * Computes the gravitational force exerted on the spacecraft by a celestial body.
     * 
     * @param body The celestial body exerting the force
     * @return The gravitational force vector
     */
    public Vector3D computeGravitationalForce(CelestialBody body) {
        if (spacecraft == null) {
            return Vector3D.zero();
        }
        Vector3D r = body.getPosition().subtract(spacecraft.getPosition());
        if (r.magnitudeSquared() < 1e-24) {              // skip self-interaction
            return Vector3D.zero();
        }
        Vector3D direction = r.safeNormalize();
        double distance = r.magnitude();
        double forceMagnitude = G * spacecraft.getMass() * body.getMass() / (distance * distance);
        return direction.scale(forceMagnitude);
    }

    /**
     * Computes the total gravitational force on the spacecraft from all celestial bodies.
     * 
     * @return The total gravitational force vector
     */
    public Vector3D computeTotalGravitationalForce() {
        Vector3D totalForce = Vector3D.zero();

        for (CelestialBody body : celestialBodies) {
            Vector3D force = computeGravitationalForce(body);
            totalForce = totalForce.add(force);
        }

        return totalForce;
    }

    /**
     * Computes the acceleration due to a thruster activation.
     * 
     * @param thruster The thruster being activated
     * @return The acceleration vector due to the thruster
     */
    public Vector3D computeThrusterAcceleration(Thruster thruster) {
        if (!thruster.isActive() || spacecraft == null) {
            return Vector3D.zero();
        }

        Vector3D thrustForce = thruster.getThrustForce();
        return thrustForce.scale(1.0 / spacecraft.getMass());
    }

    /**
     * Advances the simulation by one time step using a simple Verlet-like integration.
     */
    public void stepSimple() {
        // Calculate total force on spacecraft
        Vector3D gravitationalForce = computeTotalGravitationalForce();
        Vector3D totalAcceleration = gravitationalForce.scale(1.0 / spacecraft.getMass());

        // Update spacecraft position and velocity
        Vector3D displacement = spacecraft.getVelocity().scale(timeStep)
                .add(totalAcceleration.scale(0.5 * timeStep * timeStep));
        spacecraft.setPosition(spacecraft.getPosition().add(displacement));
        spacecraft.setVelocity(spacecraft.getVelocity().add(totalAcceleration.scale(timeStep)));
    }

    /**
     * Generates an ODE function for the spacecraft and celestial bodies.
     * 
     * @return A function that computes the derivatives of the state vector
     */
    private BiFunction<Double, double[], double[]> generateODE() {
        return (t, state) -> {
            // Extract spacecraft state
            Vector3D spacecraftPos = new Vector3D(state[0], state[1], state[2]);
            Vector3D spacecraftVel = new Vector3D(state[3], state[4], state[5]);

            // Calculate gravitational forces on spacecraft
            Vector3D totalAcceleration = Vector3D.zero();

            for (int i = 0; i < celestialBodies.size(); i++) {
                CelestialBody body = celestialBodies.get(i);
                int idx = 6 + i * 6;

                Vector3D bodyPos = new Vector3D(state[idx], state[idx + 1], state[idx + 2]);
                double bodyMass = body.getMass();

                Vector3D r = bodyPos.subtract(spacecraftPos);
                if (r.magnitudeSquared() < 1e-24) {
                    continue; // Skip if distance is too small
                }

                // Use safeNormalize to handle very small distances
                Vector3D direction = r.safeNormalize();
                double distance = r.magnitude();

                // Newton's law: a = G * m * r / |r|^3
                Vector3D acceleration = direction.scale(G * bodyMass / (distance * distance));
                totalAcceleration = totalAcceleration.add(acceleration);
            }

            // Derivatives array
            double[] derivatives = new double[state.length];

            // Spacecraft derivatives
            derivatives[0] = spacecraftVel.x;
            derivatives[1] = spacecraftVel.y;
            derivatives[2] = spacecraftVel.z;
            derivatives[3] = totalAcceleration.x;
            derivatives[4] = totalAcceleration.y;
            derivatives[5] = totalAcceleration.z;

            // Celestial body derivatives
            for (int i = 0; i < celestialBodies.size(); i++) {
                int idx = 6 + i * 6;

                // Extract body velocity
                Vector3D bodyVel = new Vector3D(state[idx + 3], state[idx + 4], state[idx + 5]);

                // Position derivatives = velocity
                derivatives[idx] = bodyVel.x;
                derivatives[idx + 1] = bodyVel.y;
                derivatives[idx + 2] = bodyVel.z;

                // Calculate gravitational forces on this body from other bodies
                Vector3D bodyPos = new Vector3D(state[idx], state[idx + 1], state[idx + 2]);
                Vector3D bodyAccel = Vector3D.zero();

                // Force from spacecraft
                Vector3D r = spacecraftPos.subtract(bodyPos);
                if (r.magnitudeSquared() < 1e-24) {
                    // Skip if distance is too small
                } else {
                    // Use safeNormalize to handle very small distances
                    Vector3D direction = r.safeNormalize();
                    double distance = r.magnitude();

                    // Newton's law: a = G * m * r / |r|^3
                    Vector3D acceleration = direction.scale(G * spacecraft.getMass() / (distance * distance));
                    bodyAccel = bodyAccel.add(acceleration);
                }

                // Force from other celestial bodies
                for (int j = 0; j < celestialBodies.size(); j++) {
                    if (i != j) {
                        CelestialBody otherBody = celestialBodies.get(j);
                        int jdx = 6 + j * 6;

                        Vector3D otherPos = new Vector3D(state[jdx], state[jdx + 1], state[jdx + 2]);
                        double otherMass = otherBody.getMass();

                        r = otherPos.subtract(bodyPos);
                        if (r.magnitudeSquared() < 1e-24) {
                            // Skip if distance is too small
                            continue;
                        }

                        // Use safeNormalize to handle very small distances
                        Vector3D dir = r.safeNormalize();
                        double dist = r.magnitude();

                        // Newton's law: a = G * m * r / |r|^3
                        Vector3D accel = dir.scale(G * otherMass / (dist * dist));
                        bodyAccel = bodyAccel.add(accel);
                    }
                }

                // Velocity derivatives = acceleration
                derivatives[idx + 3] = bodyAccel.x;
                derivatives[idx + 4] = bodyAccel.y;
                derivatives[idx + 5] = bodyAccel.z;
            }

            return derivatives;
        };
    }

    /**
     * Extracts the state vector from the spacecraft and celestial bodies.
     * 
     * @return The state vector
     */
    private double[] extractStateVector() {
        int n = celestialBodies.size();
        double[] state = new double[6 + n * 6]; // 6 for spacecraft, 6 for each celestial body

        // Spacecraft state
        if (spacecraft != null) {
            state[0] = spacecraft.getPosition().x;
            state[1] = spacecraft.getPosition().y;
            state[2] = spacecraft.getPosition().z;
            state[3] = spacecraft.getVelocity().x;
            state[4] = spacecraft.getVelocity().y;
            state[5] = spacecraft.getVelocity().z;
        } else {
            // If spacecraft is null, use zero values
            for (int i = 0; i < 6; i++) {
                state[i] = 0.0;
            }
        }

        // Celestial body states
        for (int i = 0; i < n; i++) {
            CelestialBody body = celestialBodies.get(i);
            int idx = 6 + i * 6;

            state[idx] = body.getPosition().x;
            state[idx + 1] = body.getPosition().y;
            state[idx + 2] = body.getPosition().z;
            state[idx + 3] = body.getVelocity().x;
            state[idx + 4] = body.getVelocity().y;
            state[idx + 5] = body.getVelocity().z;
        }

        return state;
    }

    /**
     * Applies the state vector to the spacecraft and celestial bodies.
     * 
     * @param state The state vector
     */
    private void applyStateVector(double[] state) {
        // Spacecraft state
        if (spacecraft != null) {
            Vector3D spacecraftPos = new Vector3D(state[0], state[1], state[2]);
            Vector3D spacecraftVel = new Vector3D(state[3], state[4], state[5]);
            spacecraft.setPosition(spacecraftPos);
            spacecraft.setVelocity(spacecraftVel);

            // Fail-fast sanity check
            if (!Double.isFinite(spacecraft.getPosition().x) || 
                !Double.isFinite(spacecraft.getPosition().y) || 
                !Double.isFinite(spacecraft.getPosition().z) ||
                !Double.isFinite(spacecraft.getVelocity().x) || 
                !Double.isFinite(spacecraft.getVelocity().y) || 
                !Double.isFinite(spacecraft.getVelocity().z)) {
                throw new IllegalStateException("NaN detected in spacecraft state vector");
            }
        }

        // Celestial body states
        for (int i = 0; i < celestialBodies.size(); i++) {
            CelestialBody body = celestialBodies.get(i);
            int idx = 6 + i * 6;

            Vector3D bodyPos = new Vector3D(state[idx], state[idx + 1], state[idx + 2]);
            Vector3D bodyVel = new Vector3D(state[idx + 3], state[idx + 4], state[idx + 5]);

            body.setPosition(bodyPos);
            body.setVelocity(bodyVel);

            // Fail-fast sanity check for celestial bodies
            if (!Double.isFinite(body.getPosition().x) || 
                !Double.isFinite(body.getPosition().y) || 
                !Double.isFinite(body.getPosition().z) ||
                !Double.isFinite(body.getVelocity().x) || 
                !Double.isFinite(body.getVelocity().y) || 
                !Double.isFinite(body.getVelocity().z)) {
                throw new IllegalStateException("NaN detected in celestial body state vector: " + body.getName());
            }
        }
    }

    /**
     * Advances the simulation by one time step using the RK4 solver.
     */
    public void stepRK4() {
        try {
            BiFunction<Double, double[], double[]> ode = generateODE();
            double[] initialState = extractStateVector();

            RK4Solver solver = new RK4Solver();
            double[][] result = solver.solve(ode, 0, initialState, timeStep, 1, null);

            // Apply the final state
            applyStateVector(result[1]);
        } catch (Exception e) {
            // Log the error and rethrow with more context
            System.err.println("Error in stepRK4: " + e.getMessage());
            throw new IllegalStateException("RK4 step failed: " + e.getMessage(), e);
        }
    }

    public double getTimeStep() {
        return timeStep;
    }

    /**
     * Simulates the effect of a thruster activation on the spacecraft's trajectory.
     * 
     * @param thruster The thruster to activate
     * @param duration The duration of the activation in seconds
     * @param throttleLevel The throttle level (0.0 to 1.0)
     */
    public void simulateThrusterActivation(Thruster thruster, double duration, double throttleLevel) {
        if (spacecraft == null) {
            return;
        }

        // Activate the thruster
        thruster.activate(throttleLevel);

        // Calculate the acceleration due to the thruster
        Vector3D thrusterAcceleration = computeThrusterAcceleration(thruster);

        // Update spacecraft velocity
        spacecraft.setVelocity(spacecraft.getVelocity().add(thrusterAcceleration.scale(duration)));

        // Calculate fuel consumption
        double fuelConsumed = thruster.getCurrentFuelConsumption() * duration;
        spacecraft.consumeFuel(fuelConsumed);

        // Deactivate the thruster
        thruster.deactivate();
    }
}
