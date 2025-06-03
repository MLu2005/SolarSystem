package com.example.utilities.GD.Utility;

import com.example.solar_system.CelestialBody;
import com.example.utilities.GD.Controllers.ThrusterController;
import com.example.utilities.GD.Opitmizers.GradientOptimizer;
import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.Vector3D;
import com.example.utilities.Ship.SpacecraftSimulation;
import com.example.utilities.Ship.StateVector;
import com.example.utilities.Ship.Thruster;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * TrajectoryCorrector provides mechanisms for periodically reassessing and correcting
 * a spacecraft's trajectory during its journey.
 * 
 * It implements three main functionalities:
 * 1. Periodic trajectory reassessment
 * 2. Determination of optimal correction points
 * 3. Handling of unexpected deviations from the planned trajectory
 */
public class TrajectoryCorrector {
    
    private final SpaceShip spacecraft;
    private final List<CelestialBody> celestialBodies;
    private final CelestialBody targetBody;
    private final GradientOptimizer optimizer;
    private final ThrusterController thrusterController;
    private final SpacecraftSimulation simulation;
    
    // Trajectory correction parameters
    private double reassessmentInterval; // Time between reassessments in seconds
    private double timeSinceLastReassessment; // Time elapsed since last reassessment
    private double deviationThreshold; // Threshold for trajectory deviation that requires correction
    private double[] plannedTrajectory; // Reference trajectory points
    private List<CorrectionPoint> optimalCorrectionPoints; // List of optimal correction points
    
    /**
     * Constructs a new TrajectoryCorrector with the specified parameters.
     *
     * @param spacecraft The spacecraft to control
     * @param celestialBodies List of celestial bodies in the simulation
     * @param targetBody The target celestial body
     * @param optimizer Gradient optimizer for computing corrections
     * @param thrusterController Controller for managing thrusters
     * @param simulation Spacecraft simulation for trajectory prediction
     * @param reassessmentInterval Time between reassessments in seconds
     * @param deviationThreshold Threshold for trajectory deviation that requires correction
     */
    public TrajectoryCorrector(
            SpaceShip spacecraft,
            List<CelestialBody> celestialBodies,
            CelestialBody targetBody,
            GradientOptimizer optimizer,
            ThrusterController thrusterController,
            SpacecraftSimulation simulation,
            double reassessmentInterval,
            double deviationThreshold) {
        
        this.spacecraft = spacecraft;
        this.celestialBodies = new ArrayList<>(celestialBodies);
        this.targetBody = targetBody;
        this.optimizer = optimizer;
        this.thrusterController = thrusterController;
        this.simulation = simulation;
        this.reassessmentInterval = reassessmentInterval;
        this.deviationThreshold = deviationThreshold;
        this.timeSinceLastReassessment = 0.0;
        this.optimalCorrectionPoints = new ArrayList<>();
    }
    
    /**
     * Updates the trajectory corrector with the elapsed time and checks if
     * trajectory reassessment is needed.
     *
     * @param deltaTime Time elapsed since last update in seconds
     * @return true if a correction was applied, false otherwise
     */
    public boolean update(double deltaTime) {
        timeSinceLastReassessment += deltaTime;
        
        // Check if it's time for a reassessment
        if (timeSinceLastReassessment >= reassessmentInterval) {
            boolean correctionApplied = reassessTrajectory();
            timeSinceLastReassessment = 0.0;
            return correctionApplied;
        }
        
        // Check if we're at an optimal correction point
        CorrectionPoint nextPoint = getNextCorrectionPoint();
        if (nextPoint != null && isAtCorrectionPoint(nextPoint)) {
            applyCorrection(nextPoint);
            optimalCorrectionPoints.remove(nextPoint);
            return true;
        }
        
        return false;
    }
    
    /**
     * Reassesses the current trajectory and applies corrections if needed.
     *
     * @return true if a correction was applied, false otherwise
     */
    public boolean reassessTrajectory() {
        // Get current state
        StateVector currentState = spacecraft.getStateVector();
        
        // Predict future trajectory without corrections
        double[][] predictedTrajectory = predictTrajectory(currentState, 3600.0, 10);
        
        // Check if trajectory deviation exceeds threshold
        if (calculateTrajectoryDeviation(predictedTrajectory) > deviationThreshold) {
            // Determine optimal correction
            double[] correction = calculateOptimalCorrection(currentState, predictedTrajectory);
            
            // Apply the correction
            thrusterController.applyThrottleLevels(correction);
            
            // Determine future optimal correction points
            determineOptimalCorrectionPoints(currentState);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Predicts the future trajectory based on the current state.
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
     * Calculates the deviation of the predicted trajectory from the planned trajectory.
     *
     * @param predictedTrajectory The predicted trajectory points
     * @return The maximum deviation in km
     */
    private double calculateTrajectoryDeviation(double[][] predictedTrajectory) {
        // If no planned trajectory exists yet, there's no deviation
        if (plannedTrajectory == null) {
            return 0.0;
        }
        
        double maxDeviation = 0.0;
        
        // Compare predicted trajectory with planned trajectory
        for (int i = 0; i < predictedTrajectory.length; i++) {
            // Calculate distance between predicted and planned positions
            double dx = predictedTrajectory[i][0] - plannedTrajectory[i * 3];
            double dy = predictedTrajectory[i][1] - plannedTrajectory[i * 3 + 1];
            double dz = predictedTrajectory[i][2] - plannedTrajectory[i * 3 + 2];
            
            double deviation = Math.sqrt(dx*dx + dy*dy + dz*dz);
            maxDeviation = Math.max(maxDeviation, deviation);
        }
        
        return maxDeviation;
    }
    
    /**
     * Calculates the optimal correction to apply based on the current state and predicted trajectory.
     *
     * @param currentState The current state vector
     * @param predictedTrajectory The predicted trajectory without corrections
     * @return Optimal thruster settings for correction
     */
    private double[] calculateOptimalCorrection(StateVector currentState, double[][] predictedTrajectory) {
        // Create an objective function that minimizes distance to target
        Function<double[], Double> objectiveFunction = optimizer.createDistanceObjectiveFunction(
            spacecraft,
            thrusterController.getThrusters(),
            targetBody,
            3600.0, // 1 hour simulation
            60.0    // 1 minute time step
        );
        
        // Use gradient descent to find optimal thruster settings
        GradientOptimizer.GradientDescentResult result = optimizer.gradientDescent(
            objectiveFunction,
            getInitialThrottleLevels(),
            true // minimize distance
        );
        
        return result.getParameters();
    }
    
    /**
     * Determines optimal points during the journey for trajectory corrections.
     *
     * @param currentState The current state vector
     */
    private void determineOptimalCorrectionPoints(StateVector currentState) {
        optimalCorrectionPoints.clear();
        
        // Predict long-term trajectory
        double[][] longTermTrajectory = predictTrajectory(currentState, 3600.0, 24); // 24-hour prediction
        
        // Analyze trajectory to find points of maximum gravitational influence
        // These are good points for corrections as small adjustments have larger effects
        for (int i = 1; i < longTermTrajectory.length - 1; i++) {
            Vector3D position = new Vector3D(
                longTermTrajectory[i][0],
                longTermTrajectory[i][1],
                longTermTrajectory[i][2]
            );
            
            // Calculate total gravitational force at this point
            double totalGravForce = calculateTotalGravitationalForce(position).magnitude();
            
            // Calculate rate of change of gravitational force
            Vector3D prevPos = new Vector3D(
                longTermTrajectory[i-1][0],
                longTermTrajectory[i-1][1],
                longTermTrajectory[i-1][2]
            );
            double prevGravForce = calculateTotalGravitationalForce(prevPos).magnitude();
            
            double gravForceGradient = Math.abs(totalGravForce - prevGravForce);
            
            // Points with high gravitational force gradient are good for corrections
            if (gravForceGradient > 0.1 * totalGravForce) {
                optimalCorrectionPoints.add(new CorrectionPoint(i * 3600.0, position));
            }
        }
        
        // Add additional correction points at regular intervals if needed
        if (optimalCorrectionPoints.size() < 3) {
            double interval = 24.0 * 3600.0 / 4.0; // 4 points over 24 hours
            for (int i = 1; i <= 3; i++) {
                double time = i * interval;
                int index = (int)(time / 3600.0);
                if (index < longTermTrajectory.length) {
                    Vector3D position = new Vector3D(
                        longTermTrajectory[index][0],
                        longTermTrajectory[index][1],
                        longTermTrajectory[index][2]
                    );
                    optimalCorrectionPoints.add(new CorrectionPoint(time, position));
                }
            }
        }
    }
    
    /**
     * Calculates the total gravitational force at a given position.
     *
     * @param position The position to calculate gravitational force at
     * @return The total gravitational force vector
     */
    private Vector3D calculateTotalGravitationalForce(Vector3D position) {
        Vector3D totalForce = Vector3D.zero();
        double G = 6.6743E-20; // Gravitational constant in km^3 kg^-1 s^-2
        
        for (CelestialBody body : celestialBodies) {
            Vector3D r = body.getPosition().subtract(position);
            double distance = r.magnitude() + 1e-10; // avoid division by zero
            double forceMagnitude = G * spacecraft.getMass() * body.getMass() / (distance * distance);
            totalForce = totalForce.add(r.normalize().scale(forceMagnitude));
        }
        
        return totalForce;
    }
    
    /**
     * Checks if the spacecraft is at a correction point.
     *
     * @param point The correction point to check
     * @return true if the spacecraft is at the correction point, false otherwise
     */
    private boolean isAtCorrectionPoint(CorrectionPoint point) {
        // Calculate distance to the correction point
        double distance = spacecraft.getPosition().subtract(point.position).magnitude();
        
        // Consider the spacecraft at the correction point if it's within 100km
        return distance < 100.0;
    }
    
    /**
     * Gets the next correction point based on the current position.
     *
     * @return The next correction point, or null if none exists
     */
    private CorrectionPoint getNextCorrectionPoint() {
        if (optimalCorrectionPoints.isEmpty()) {
            return null;
        }
        
        // Find the closest correction point
        CorrectionPoint closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (CorrectionPoint point : optimalCorrectionPoints) {
            double distance = spacecraft.getPosition().subtract(point.position).magnitude();
            if (distance < minDistance) {
                minDistance = distance;
                closest = point;
            }
        }
        
        return closest;
    }
    
    /**
     * Applies a correction at the specified correction point.
     *
     * @param point The correction point
     */
    private void applyCorrection(CorrectionPoint point) {
        // Calculate optimal correction
        StateVector currentState = spacecraft.getStateVector();
        double[][] predictedTrajectory = predictTrajectory(currentState, 3600.0, 10);
        double[] correction = calculateOptimalCorrection(currentState, predictedTrajectory);
        
        // Apply the correction
        thrusterController.applyThrottleLevels(correction);
    }
    
    /**
     * Sets the planned trajectory for deviation calculations.
     *
     * @param plannedTrajectory The planned trajectory points
     */
    public void setPlannedTrajectory(double[] plannedTrajectory) {
        this.plannedTrajectory = plannedTrajectory;
    }
    
    /**
     * Gets the current list of optimal correction points.
     *
     * @return List of optimal correction points
     */
    public List<CorrectionPoint> getOptimalCorrectionPoints() {
        return new ArrayList<>(optimalCorrectionPoints);
    }
    
    /**
     * Sets the reassessment interval.
     *
     * @param reassessmentInterval The new reassessment interval in seconds
     */
    public void setReassessmentInterval(double reassessmentInterval) {
        this.reassessmentInterval = reassessmentInterval;
    }
    
    /**
     * Sets the deviation threshold.
     *
     * @param deviationThreshold The new deviation threshold in km
     */
    public void setDeviationThreshold(double deviationThreshold) {
        this.deviationThreshold = deviationThreshold;
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
     * Inner class representing a point in the trajectory where a correction should be applied.
     */
    public static class CorrectionPoint {
        private final double time;
        private final Vector3D position;
        
        /**
         * Constructs a new CorrectionPoint.
         *
         * @param time The time at which the correction should be applied
         * @param position The position at which the correction should be applied
         */
        public CorrectionPoint(double time, Vector3D position) {
            this.time = time;
            this.position = position;
        }
        
        /**
         * Gets the time at which the correction should be applied.
         *
         * @return The time in seconds
         */
        public double getTime() {
            return time;
        }
        
        /**
         * Gets the position at which the correction should be applied.
         *
         * @return The position vector
         */
        public Vector3D getPosition() {
            return position;
        }
    }
}