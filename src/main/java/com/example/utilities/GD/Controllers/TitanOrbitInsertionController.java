package com.example.utilities.GD.Controllers;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.GD.Opitmizers.GradientOptimizer;
import com.example.utilities.Ship.SpacecraftSimulation;
import com.example.utilities.Ship.StateVector;
import com.example.utilities.GD.Utility.TitanOrbitParameters;
import com.example.utilities.Ship.Thruster;
import com.example.utilities.Vector3D;

import java.util.List;
import java.util.function.Function;

/**
 * TitanOrbitInsertionController implements a specialized control strategy for the
 * final approach to Titan's orbit and the orbital insertion phase.
 * 
 * This controller provides:
 * 1. Trajectory planning for the final approach to Titan
 * 2. Specialized control strategy for the orbital insertion phase
 * 3. Determination of optimal timing for orbital insertion burns
 */
public class TitanOrbitInsertionController {

    private final SpaceShip spacecraft;
    private final List<CelestialBody> celestialBodies;
    private final CelestialBody titan;
    private final ThrusterController thrusterController;
    private final GradientOptimizer optimizer;
    private final SpacecraftSimulation simulation;

    // Orbital insertion parameters
    private final TitanOrbitParameters targetOrbit;
    private boolean insertionPhaseActive = false;
    private boolean insertionComplete = false;
    private double timeToInsertionBurn = Double.MAX_VALUE;

    // Approach phases
    public enum ApproachPhase {
        INITIAL_APPROACH,    // Far from Titan, making course corrections
        FINAL_APPROACH,      // Close to Titan, preparing for insertion
        INSERTION_BURN,      // Executing the insertion burn
        ORBIT_STABILIZATION  // Fine-tuning the orbit after insertion
    }

    private ApproachPhase currentPhase = ApproachPhase.INITIAL_APPROACH;

    /**
     * Constructs a new TitanOrbitInsertionController with the specified parameters.
     *
     * @param spacecraft The spacecraft to control
     * @param celestialBodies List of celestial bodies in the simulation
     * @param titan The Titan celestial body
     * @param thrusterController Controller for managing thrusters
     * @param optimizer Gradient optimizer for computing corrections
     * @param simulation Spacecraft simulation for trajectory prediction
     * @param targetOrbit The target orbit parameters around Titan
     */
    public TitanOrbitInsertionController(
            SpaceShip spacecraft,
            List<CelestialBody> celestialBodies,
            CelestialBody titan,
            ThrusterController thrusterController,
            GradientOptimizer optimizer,
            SpacecraftSimulation simulation,
            TitanOrbitParameters targetOrbit) {

        this.spacecraft = spacecraft;
        this.celestialBodies = celestialBodies;
        this.titan = titan;
        this.thrusterController = thrusterController;
        this.optimizer = optimizer;
        this.simulation = simulation;
        this.targetOrbit = targetOrbit;
    }

    /**
     * Updates the controller with the elapsed time and manages the approach and insertion phases.
     *
     * @param deltaTime Time elapsed since last update in seconds
     * @return true if a control action was applied, false otherwise
     */
    public boolean update(double deltaTime) {
        // Update time to insertion burn
        if (timeToInsertionBurn != Double.MAX_VALUE) {
            timeToInsertionBurn -= deltaTime;
        }

        // Determine current phase based on distance to Titan and relative velocity
        updateApproachPhase();

        // Handle each phase
        switch (currentPhase) {
            case INITIAL_APPROACH:
                return handleInitialApproach();
            case FINAL_APPROACH:
                return handleFinalApproach();
            case INSERTION_BURN:
                return executeInsertionBurn();
            case ORBIT_STABILIZATION:
                return stabilizeOrbit();
            default:
                return false;
        }
    }

    /**
     * Updates the current approach phase based on the spacecraft's position and velocity
     * relative to Titan.
     */
    private void updateApproachPhase() {
        // Get current state relative to Titan
        Vector3D relativePosition = spacecraft.getPosition().subtract(titan.getPosition());
        Vector3D relativeVelocity = spacecraft.getVelocity().subtract(titan.getVelocity());
        double distance = relativePosition.magnitude();

        // Since we're starting close to Titan, immediately set to FINAL_APPROACH
        // Determine phase based on insertion status
        if (insertionComplete) {
            currentPhase = ApproachPhase.ORBIT_STABILIZATION;
        } else if (insertionPhaseActive) {
            currentPhase = ApproachPhase.INSERTION_BURN;
        } else {
            // Skip INITIAL_APPROACH and go straight to FINAL_APPROACH
            currentPhase = ApproachPhase.FINAL_APPROACH;

            // If we haven't calculated insertion burn time yet, do it now
            if (timeToInsertionBurn == Double.MAX_VALUE) {
                timeToInsertionBurn = 60.0; // Set a short time to insertion burn (1 minute)
            }
        }
    }

    /**
     * Handles the initial approach phase, making course corrections to ensure
     * the spacecraft is on a trajectory that will intercept Titan's sphere of influence.
     *
     * @return true if a control action was applied, false otherwise
     */
    private boolean handleInitialApproach() {
        // Get current position and velocity relative to Titan
        Vector3D relativePosition = spacecraft.getPosition().subtract(titan.getPosition());
        Vector3D relativeVelocity = spacecraft.getVelocity().subtract(titan.getVelocity());

        // Calculate direction to Titan
        Vector3D directionToTitan = relativePosition.scale(-1.0).safeNormalize();

        // Calculate current velocity component toward Titan
        double velocityTowardTitan = relativeVelocity.dot(directionToTitan);

        // If we're moving away from Titan or too slowly toward it, apply thrust toward Titan
        if (velocityTowardTitan < 5.0) { // 5 km/s is a reasonable approach velocity
            // Create a simple thrust vector pointing toward Titan
            double thrustMagnitude = 10000.0; // Use a moderate thrust level
            Vector3D thrustDirection = directionToTitan;
            Vector3D thrustForce = thrustDirection.scale(thrustMagnitude);

            // Convert thrust force to throttle levels for the thrusters
            double[] throttleLevels = new double[thrusterController.getThrusters().size()];
            List<Thruster> thrusters = thrusterController.getThrusters();

            // Set main thruster to full throttle if it's pointing in the right direction
            for (int i = 0; i < thrusters.size(); i++) {
                Thruster thruster = thrusters.get(i);
                double alignment = thruster.getDirection().dot(thrustDirection);

                // If thruster is pointing in roughly the right direction, use it
                if (alignment > 0.7) {
                    throttleLevels[i] = 1.0; // Full throttle
                } else if (alignment > 0.3) {
                    throttleLevels[i] = 0.5; // Half throttle
                } else if (alignment > 0) {
                    throttleLevels[i] = 0.2; // Low throttle
                } else {
                    throttleLevels[i] = 0.0; // No throttle
                }
            }

            // Apply the throttle levels
            thrusterController.applyThrottleLevels(throttleLevels);
        } else {
            // We're already moving toward Titan at a good speed, no need for thrust
            double[] noThrust = new double[thrusterController.getThrusters().size()];
            thrusterController.applyThrottleLevels(noThrust);
        }

        return true;
    }

    /**
     * Handles the final approach phase, preparing for orbital insertion by
     * aligning the spacecraft's trajectory for an optimal insertion burn.
     *
     * @return true if a control action was applied, false otherwise
     */
    private boolean handleFinalApproach() {
        // Get current state relative to Titan
        Vector3D relativePosition = spacecraft.getPosition().subtract(titan.getPosition());
        Vector3D relativeVelocity = spacecraft.getVelocity().subtract(titan.getVelocity());

        // Calculate the optimal approach trajectory for orbit insertion
        Vector3D optimalApproachVector = calculateOptimalApproachVector();

        // Create an objective function that aligns the approach trajectory
        Function<double[], Double> objectiveFunction = (double[] throttleLevels) -> {
            // Calculate the thrust force from these throttle levels
            Vector3D thrustForce = calculateThrustForce(throttleLevels);

            // Simulate the effect of this thrust
            StateVector currentState = spacecraft.getStateVector();
            StateVector futureState = simulateTrajectory(currentState, thrustForce, 600.0); // 10 minutes

            // Calculate the approach vector after applying thrust
            Vector3D futureRelPos = futureState.getPosition().subtract(titan.getPosition());
            Vector3D futureRelVel = futureState.getVelocity().subtract(titan.getVelocity());

            // Calculate alignment error with optimal approach
            double alignmentError = 1.0 - Math.abs(futureRelVel.safeNormalize().dot(optimalApproachVector.safeNormalize()));

            // Calculate distance to periapsis
            double periapsisError = Math.abs(calculatePeriapsisDistance(futureRelPos, futureRelVel) - 
                                           (TitanOrbitParameters.TITAN_RADIUS + targetOrbit.getAltitude()));

            // Combine errors with weights
            return 0.7 * alignmentError + 0.3 * periapsisError / 1000.0;
        };

        // Use gradient descent to find optimal thruster settings
        GradientOptimizer.GradientDescentResult result = optimizer.gradientDescent(
            objectiveFunction,
            getInitialThrottleLevels(),
            true // minimize error
        );

        // Apply the optimal throttle levels
        thrusterController.applyThrottleLevels(result.getParameters());

        // Check if we're close to the insertion burn time
        if (timeToInsertionBurn <= 60.0) { // Within 1 minute of burn time
            insertionPhaseActive = true;
        }

        return true;
    }

    /**
     * Executes the orbital insertion burn to establish the desired orbit around Titan.
     *
     * @return true if the burn was executed, false otherwise
     */
    private boolean executeInsertionBurn() {
        // Get current state relative to Titan
        Vector3D relativePosition = spacecraft.getPosition().subtract(titan.getPosition());
        Vector3D relativeVelocity = spacecraft.getVelocity().subtract(titan.getVelocity());

        // Calculate the required delta-V for orbit insertion
        double approachVelocity = relativeVelocity.magnitude();
        double requiredDeltaV = targetOrbit.calculateOrbitInsertionDeltaV(approachVelocity);

        // Calculate the direction for the burn (opposite to velocity vector)
        Vector3D burnDirection = relativeVelocity.safeNormalize().scale(-1.0);

        // Create an objective function that applies the required delta-V
        Function<double[], Double> objectiveFunction = (double[] throttleLevels) -> {
            // Calculate the thrust force from these throttle levels
            Vector3D thrustForce = calculateThrustForce(throttleLevels);

            // Calculate the alignment of thrust with the required burn direction
            double alignment = thrustForce.safeNormalize().dot(burnDirection);

            // Penalize misalignment and thrust that's too weak or too strong
            double thrustMagnitude = thrustForce.magnitude();
            double thrustError = Math.abs(thrustMagnitude - requiredDeltaV * spacecraft.getMass() / 60.0); // Aim for 1-minute burn

            // Combine errors with weights
            return (1.0 - alignment) * 0.7 + thrustError / 1000.0 * 0.3;
        };

        // Use gradient descent to find optimal thruster settings
        GradientOptimizer.GradientDescentResult result = optimizer.gradientDescent(
            objectiveFunction,
            getInitialThrottleLevels(),
            true // minimize error
        );

        // Apply the optimal throttle levels
        thrusterController.applyThrottleLevels(result.getParameters());

        // Check if insertion is complete by verifying we're in a stable orbit
        if (TitanOrbitParameters.isStableOrbit(spacecraft.getStateVector(), titan)) {
            insertionComplete = true;
            insertionPhaseActive = false;
        }

        return true;
    }

    /**
     * Stabilizes the orbit after insertion by making small corrections to achieve
     * the exact target orbital parameters.
     *
     * @return true if a correction was applied, false otherwise
     */
    private boolean stabilizeOrbit() {
        // Get current state relative to Titan
        Vector3D relativePosition = spacecraft.getPosition().subtract(titan.getPosition());
        Vector3D relativeVelocity = spacecraft.getVelocity().subtract(titan.getVelocity());

        // Calculate current orbital parameters
        TitanOrbitParameters currentOrbit = TitanOrbitParameters.calculateFromStateVectors(
            relativePosition, relativeVelocity);

        // Calculate distance between current and target orbit
        double orbitError = currentOrbit.distanceToOrbit(targetOrbit);

        // If orbit is close enough to target, no correction needed
        if (orbitError < 0.05) {
            return false;
        }

        // Create an objective function that minimizes orbit error
        Function<double[], Double> objectiveFunction = (double[] throttleLevels) -> {
            // Calculate the thrust force from these throttle levels
            Vector3D thrustForce = calculateThrustForce(throttleLevels);

            // Simulate the effect of this thrust
            StateVector currentState = spacecraft.getStateVector();
            StateVector futureState = simulateTrajectory(currentState, thrustForce, 300.0); // 5 minutes

            // Calculate future orbit parameters
            Vector3D futureRelPos = futureState.getPosition().subtract(titan.getPosition());
            Vector3D futureRelVel = futureState.getVelocity().subtract(titan.getVelocity());
            TitanOrbitParameters futureOrbit = TitanOrbitParameters.calculateFromStateVectors(
                futureRelPos, futureRelVel);

            // Return the distance to target orbit
            return futureOrbit.distanceToOrbit(targetOrbit);
        };

        // Use gradient descent to find optimal thruster settings
        GradientOptimizer.GradientDescentResult result = optimizer.gradientDescent(
            objectiveFunction,
            getInitialThrottleLevels(),
            true // minimize error
        );

        // Apply the optimal throttle levels
        thrusterController.applyThrottleLevels(result.getParameters());

        return true;
    }

    /**
     * Determines the optimal time for the orbital insertion burn based on the
     * current trajectory and target orbit.
     *
     * @return Time in seconds until the optimal burn point
     */
    public double determineOptimalBurnTime() {
        // Get current state
        StateVector currentState = spacecraft.getStateVector();

        // Predict trajectory without corrections
        double[][] predictedTrajectory = predictTrajectory(currentState, 600.0, 20); // 3-hour prediction with 10-minute steps

        double bestTime = 0.0;
        double minDeltaV = Double.MAX_VALUE;

        // Analyze each point in the trajectory to find where insertion requires minimum delta-V
        for (int i = 0; i < predictedTrajectory.length; i++) {
            // Calculate position and velocity at this point
            Vector3D position = new Vector3D(
                predictedTrajectory[i][0],
                predictedTrajectory[i][1],
                predictedTrajectory[i][2]
            );

            // Get the state at this point
            StateVector state = simulateToTime(currentState, i * 600.0);
            Vector3D velocity = state.getVelocity();

            // Calculate position and velocity relative to Titan
            Vector3D titanPos = simulateTitanPosition(i * 600.0);
            Vector3D relPosition = position.subtract(titanPos);
            Vector3D relVelocity = velocity.subtract(titan.getVelocity());

            // Calculate periapsis distance for this trajectory
            double periapsisDistance = calculatePeriapsisDistance(relPosition, relVelocity);

            // Skip if periapsis is too far from desired altitude
            double targetPeriapsis = TitanOrbitParameters.TITAN_RADIUS + targetOrbit.getAltitude();
            if (Math.abs(periapsisDistance - targetPeriapsis) > 500.0) {
                continue;
            }

            // Calculate delta-V required for insertion at this point
            double approachVelocity = relVelocity.magnitude();
            double deltaV = targetOrbit.calculateOrbitInsertionDeltaV(approachVelocity);

            // Update best time if this requires less delta-V
            if (deltaV < minDeltaV) {
                minDeltaV = deltaV;
                bestTime = i * 600.0;
            }
        }

        return bestTime;
    }

    /**
     * Calculates the periapsis distance for a hyperbolic approach trajectory.
     *
     * @param position Position vector relative to Titan
     * @param velocity Velocity vector relative to Titan
     * @return Periapsis distance in km
     */
    private double calculatePeriapsisDistance(Vector3D position, Vector3D velocity) {
        double mu = 6.6743E-20 * TitanOrbitParameters.TITAN_MASS; // G * M

        // Calculate specific angular momentum
        Vector3D h = position.cross(velocity);

        // Calculate eccentricity vector
        Vector3D eVec = velocity.cross(h).scale(1.0/mu).subtract(position.safeNormalize());
        double eccentricity = eVec.magnitude();

        // Calculate semi-major axis
        double v2 = velocity.dot(velocity);
        double r = position.magnitude();
        double a = 1.0 / (2.0/r - v2/mu);

        // Calculate periapsis distance
        double periapsis = a * (1 - eccentricity);

        return periapsis;
    }

    /**
     * Calculates the optimal approach vector for orbit insertion.
     *
     * @return The optimal approach vector
     */
    private Vector3D calculateOptimalApproachVector() {
        // For a prograde orbit, the optimal approach is perpendicular to the position vector
        // and in the orbital plane defined by the target inclination

        // Get position relative to Titan
        Vector3D relativePosition = spacecraft.getPosition().subtract(titan.getPosition());

        // Define the orbital plane normal based on target inclination
        double inclinationRad = Math.toRadians(targetOrbit.getInclination());
        Vector3D orbitalPlaneNormal = new Vector3D(
            Math.sin(inclinationRad),
            0,
            Math.cos(inclinationRad)
        );

        // Calculate the approach vector (perpendicular to position and in the orbital plane)
        Vector3D approachVector = relativePosition.cross(orbitalPlaneNormal).safeNormalize();

        // Scale to the required velocity for the target orbit
        double requiredVelocity = targetOrbit.calculatePeriapsisVelocity();
        return approachVector.scale(requiredVelocity);
    }

    /**
     * Predicts the trajectory based on the current state.
     *
     * @param initialState The initial state vector
     * @param timeStep Time step for prediction in seconds
     * @param numSteps Number of prediction steps
     * @return Predicted trajectory points
     */
    private double[][] predictTrajectory(StateVector initialState, double timeStep, int numSteps) {
        double[][] trajectory = new double[numSteps][3]; // Store position vectors

        // Clone the spacecraft to avoid modifying the original
        SpaceShip shipClone = new SpaceShip(
            spacecraft.getName(),
            spacecraft.getThrust(),
            initialState.getVelocity(),
            initialState.getMass(),
            spacecraft.getFuel(),
            initialState.getPosition(),
            initialState.getOrientation()
        );

        // Set up a simulation for prediction
        SpacecraftSimulation predictionSim = new SpacecraftSimulation(shipClone);
        for (CelestialBody body : celestialBodies) {
            predictionSim.addCelestialBody(body);
        }
        predictionSim.setTimeStep(timeStep);

        // Run the prediction
        for (int i = 0; i < numSteps; i++) {
            predictionSim.stepRK4();
            Vector3D position = shipClone.getPosition();
            trajectory[i] = new double[] {position.x, position.y, position.z};
        }

        return trajectory;
    }

    /**
     * Simulates the spacecraft's state after a given time with a constant thrust force.
     *
     * @param initialState The initial state vector
     * @param thrustForce The constant thrust force to apply
     * @param duration The duration of the simulation in seconds
     * @return The final state vector
     */
    private StateVector simulateTrajectory(StateVector initialState, Vector3D thrustForce, double duration) {
        // Create a new spacecraft with the initial state
        SpaceShip shipClone = new SpaceShip(
            spacecraft.getName(),
            spacecraft.getThrust(),
            initialState.getVelocity(),
            initialState.getMass(),
            spacecraft.getFuel(),
            initialState.getPosition(),
            initialState.getOrientation()
        );

        // Set up a simulation for prediction
        SpacecraftSimulation predictionSim = new SpacecraftSimulation(shipClone);
        for (CelestialBody body : celestialBodies) {
            predictionSim.addCelestialBody(body);
        }

        // Run the simulation
        double timeStep = 10.0; // 10-second time step

        // ---------------------------------------------------------
        // FIX: make the prediction simulation actually use 10 s
        predictionSim.setTimeStep(timeStep);
        // ---------------------------------------------------------
        int steps = (int) (duration / timeStep);

        if (thrustForce.magnitude() < 1e-12) {
            // passive coast
            for (int i = 0; i < steps; i++) predictionSim.stepRK4();
        } else {
            Thruster temp = new Thruster(
                thrustForce.magnitude(),
                thrustForce.safeNormalize(),
                0.0
            );
            for (int i = 0; i < steps; i++) {
                predictionSim.simulateThrusterActivation(temp, timeStep, 1.0);
                predictionSim.stepRK4();
            }
        }

        // Return the final state
        return new StateVector(
            shipClone.getPosition(),
            shipClone.getVelocity(),
            shipClone.getOrientation(),
            shipClone.getMass()
        );
    }

    /**
     * Simulates the spacecraft's state at a specific time in the future.
     *
     * @param initialState The initial state vector
     * @param time The time to simulate to in seconds
     * @return The state vector at the specified time
     */
    private StateVector simulateToTime(StateVector initialState, double time) {
        // Clone the spacecraft to avoid modifying the original
        SpaceShip shipClone = new SpaceShip(
            spacecraft.getName(),
            spacecraft.getThrust(),
            initialState.getVelocity(),
            initialState.getMass(),
            spacecraft.getFuel(),
            initialState.getPosition(),
            initialState.getOrientation()
        );

        // Set up a simulation for prediction
        SpacecraftSimulation predictionSim = new SpacecraftSimulation(shipClone);
        for (CelestialBody body : celestialBodies) {
            predictionSim.addCelestialBody(body);
        }

        // Run the simulation
        double timeStep = 10.0; // 10-second time step
        int steps = (int)(time / timeStep);
        for (int i = 0; i < steps; i++) {
            predictionSim.stepRK4();
        }

        // Return the final state
        return new StateVector(
            shipClone.getPosition(),
            shipClone.getVelocity(),
            shipClone.getOrientation(),
            shipClone.getMass()
        );
    }

    /**
     * Simulates Titan's position at a specific time in the future.
     *
     * @param time The time to simulate to in seconds
     * @return Titan's position at the specified time
     */
    private Vector3D simulateTitanPosition(double time) {
        // Clone Titan to avoid modifying the original
        CelestialBody titanClone = new CelestialBody(
            titan.getName(),
            titan.getMass(),
            titan.getPosition(),
            titan.getVelocity()
        );

        // Set up a simple simulation
        SpacecraftSimulation predictionSim = new SpacecraftSimulation(null);
        predictionSim.addCelestialBody(titanClone);

        // Run the simulation
        double timeStep = 60.0; // 1-minute time step
        int steps = (int)(time / timeStep);
        for (int i = 0; i < steps; i++) {
            predictionSim.stepRK4();
        }

        // Return Titan's final position
        return titanClone.getPosition();
    }

    /**
     * Calculates the thrust force produced by the thrusters at the given throttle levels.
     *
     * @param throttleLevels The throttle levels for each thruster
     * @return The total thrust force vector
     */
    private Vector3D calculateThrustForce(double[] throttleLevels) {
        Vector3D totalForce = Vector3D.zero();
        List<Thruster> thrusters = thrusterController.getThrusters();

        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double throttle = throttleLevels[i];
            Vector3D force = thruster.getDirection().scale(thruster.getMaxThrust() * throttle);
            totalForce = totalForce.add(force);
        }

        return totalForce;
    }

    /**
     * Gets the initial throttle levels for optimization.
     *
     * @return Initial throttle levels
     */
    private double[] getInitialThrottleLevels() {
        List<Thruster> thrusters = thrusterController.getThrusters();
        double[] throttleLevels = new double[thrusters.size()];

        // Start with moderate throttle for all thrusters
        for (int i = 0; i < thrusters.size(); i++) {
            throttleLevels[i] = 0.5;
        }

        return throttleLevels;
    }

    /**
     * Gets the current approach phase.
     *
     * @return The current approach phase
     */
    public ApproachPhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Gets the time until the optimal insertion burn.
     *
     * @return Time in seconds until the optimal burn
     */
    public double getTimeToInsertionBurn() {
        return timeToInsertionBurn;
    }

    /**
     * Checks if the orbital insertion is complete.
     *
     * @return true if insertion is complete, false otherwise
     */
    public boolean isInsertionComplete() {
        return insertionComplete;
    }
}
