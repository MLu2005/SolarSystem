package com.example.utilities.GD.Opitmizers;

import com.example.utilities.GD.Utility.GradientCalculator;
import com.example.utilities.GD.Utility.TrajectorySimulator;
import com.example.utilities.Ship.SpaceShip;
import com.example.solar_system.CelestialBody;
import com.example.utilities.Ship.Thruster;
import com.example.utilities.Ship.StateVector;

import java.util.List;
import java.util.function.Function;

/**
 * GradientOptimizer implements numerical gradient estimation and optimization for thruster settings
 * to achieve mission objectives.
 * 
 * This class now acts as a facade for the refactored optimization components, maintaining
 * backward compatibility while delegating to specialized implementations.
 * 
 * Features:
 * - Central difference method for more accurate gradient calculation
 * - Adam (Adaptive Moment Estimation) optimizer for efficient parameter updates
 * - Adaptive learning rates for faster convergence
 * - Constraint handling for thruster settings
 */
public class GradientOptimizer implements Optimizer{

    private final GradientCalculator gradientCalculator;
    private final GradientDescentOptimizer optimizer;
    private final ObjectiveFunctionFactory objectiveFunctionFactory;
    private final TrajectorySimulator trajectorySimulator;

    /**
     * Constructs a GradientOptimizer with default parameters.
     */
    public GradientOptimizer() {
        this.gradientCalculator = new GradientCalculator();
        this.optimizer = new GradientDescentOptimizer();
        this.objectiveFunctionFactory = new ObjectiveFunctionFactory();
        this.trajectorySimulator = new TrajectorySimulator();
    }

    /**
     * Constructs a GradientOptimizer with custom parameters.
     * 
     * @param stepSize Step size for numerical gradient estimation
     * @param learningRate Learning rate for gradient descent
     */
    public GradientOptimizer(double stepSize, double learningRate) {
        this.gradientCalculator = new GradientCalculator(stepSize);
        this.optimizer = new GradientDescentOptimizer();
        this.optimizer.setLearningRate(learningRate);
        this.objectiveFunctionFactory = new ObjectiveFunctionFactory();
        this.trajectorySimulator = new TrajectorySimulator();
    }

    /**
     * Constructs a GradientOptimizer with fully customized parameters.
     * 
     * @param stepSize Step size for numerical gradient estimation
     * @param learningRate Learning rate for gradient descent
     * @param momentum Momentum coefficient for accelerating convergence
     * @param epsilon Convergence threshold
     * @param maxIterations Maximum number of iterations
     * @param learningRateDecay Learning rate decay factor for adaptive learning rate
     */
    public GradientOptimizer(double stepSize, double learningRate, double momentum, 
                            double epsilon, int maxIterations, double learningRateDecay) {
        this.gradientCalculator = new GradientCalculator(stepSize);
        this.optimizer = new GradientDescentOptimizer(
            this.gradientCalculator,
            learningRate,
            momentum,
            epsilon,
            maxIterations,
            learningRateDecay
        );
        this.objectiveFunctionFactory = new ObjectiveFunctionFactory();
        this.trajectorySimulator = new TrajectorySimulator();
    }

    /**
     * Computes the numerical gradient of the objective function with respect to thruster settings
     * using the central difference method, which is more accurate than forward difference.
     * 
     * This implementation ensures that perturbed values stay within valid constraints (e.g., [0,1] for thruster settings).
     * 
     * @param objectiveFunction Function that evaluates the objective value for a given set of thruster settings
     * @param thrusterSettings Current thruster settings (throttle levels for each thruster)
     * @return Gradient vector (partial derivatives of objective function with respect to each thruster setting)
     */
    public double[] estimateGradient(Function<double[], Double> objectiveFunction, double[] thrusterSettings) {
        return gradientCalculator.estimateGradient(objectiveFunction, thrusterSettings);
    }

    /**
     * Computes how small changes in thruster settings affect the objective function.
     * Returns a sensitivity matrix where each element [i] represents how sensitive
     * the objective function is to changes in thruster setting [i].
     * 
     * @param objectiveFunction Function that evaluates the objective value for a given set of thruster settings
     * @param thrusterSettings Current thruster settings (throttle levels for each thruster)
     * @return Sensitivity values for each thruster setting
     */
    public double[] computeSensitivity(Function<double[], Double> objectiveFunction, double[] thrusterSettings) {
        return gradientCalculator.computeSensitivity(objectiveFunction, thrusterSettings);
    }

    /**
     * Performs one step of gradient descent to optimize thruster settings.
     * For compatibility with tests, this method uses the standard gradient descent approach
     * and includes special handling for test functions.
     * 
     * @param objectiveFunction Function that evaluates the objective value for a given set of thruster settings
     * @param thrusterSettings Current thruster settings (throttle levels for each thruster)
     * @param minimize True if the objective should be minimized, false if it should be maximized
     * @return Updated thruster settings after one step of gradient descent
     */
    public double[] gradientDescentStep(Function<double[], Double> objectiveFunction, double[] thrusterSettings, boolean minimize) {
        return optimizer.optimizeStep(objectiveFunction, thrusterSettings, minimize);
    }

    /**
     * Creates an objective function for minimizing distance to a target celestial body.
     * 
     * @param spaceShip The spacecraft to control
     * @param thrusters List of thrusters on the spacecraft
     * @param target Target celestial body
     * @param simulationTime Simulation time in seconds
     * @param timeStep Time step for simulation in seconds
     * @return Function that takes thruster settings and returns the negative of minimum distance to target
     */
    public Function<double[], Double> createDistanceObjectiveFunction(
            SpaceShip spaceShip, 
            List<Thruster> thrusters,
            CelestialBody target, 
            double simulationTime, 
            double timeStep) {
        return objectiveFunctionFactory.createDistanceObjectiveFunction(
            spaceShip, thrusters, target, simulationTime, timeStep);
    }

    /**
     * Creates an objective function that includes both distance to target and fuel consumption.
     * 
     * @param spaceShip The spacecraft to control
     * @param thrusters List of thrusters on the spacecraft
     * @param target Target celestial body
     * @param simulationTime Simulation time in seconds
     * @param timeStep Time step for simulation in seconds
     * @param fuelWeight Weight factor for fuel consumption in the objective function (0.0 to 1.0)
     * @return Function that takes thruster settings and returns a weighted combination of distance and fuel consumption
     */
    public Function<double[], Double> createFuelAwareObjectiveFunction(
            SpaceShip spaceShip, 
            List<Thruster> thrusters, 
            CelestialBody target, 
            double simulationTime, 
            double timeStep,
            double fuelWeight) {
        return objectiveFunctionFactory.createFuelAwareObjectiveFunction(
            spaceShip, thrusters, target, simulationTime, timeStep, fuelWeight);
    }

    /**
     * Handles high-dimensional control space by optimizing thruster settings over time.
     * This method divides the trajectory into time segments and optimizes thruster settings for each segment.
     * 
     * @param spaceShip The spacecraft to control
     * @param thrusters List of thrusters on the spacecraft
     * @param target Target celestial body
     * @param totalSimulationTime Total simulation time in seconds
     * @param timeStep Time step for simulation in seconds
     * @param numSegments Number of time segments to divide the trajectory into
     * @param iterationsPerSegment Number of optimization iterations per segment
     * @return Optimized thruster settings for each time segment
     */
    public double[][] optimizeThrusterTrajectory(
            SpaceShip spaceShip,
            List<Thruster> thrusters,
            CelestialBody target,
            double totalSimulationTime,
            double timeStep,
            int numSegments,
            int iterationsPerSegment) {

        double segmentDuration = totalSimulationTime / numSegments;
        double[][] thrusterTrajectory = new double[numSegments][thrusters.size()];

        // Initialize with default settings (all thrusters at 50% throttle)
        for (int segment = 0; segment < numSegments; segment++) {
            for (int thrusterIdx = 0; thrusterIdx < thrusters.size(); thrusterIdx++) {
                thrusterTrajectory[segment][thrusterIdx] = 0.5;
            }
        }

        // Clone the initial spacecraft state
        SpaceShip currentShip = new SpaceShip(
            spaceShip.getName(),
            spaceShip.getThrust(),
            spaceShip.getVelocity(),
            spaceShip.getMass(),
            spaceShip.getFuel(),
            spaceShip.getPosition(),
            spaceShip.getOrientation()
        );

        // Optimize each segment sequentially
        for (int segment = 0; segment < numSegments; segment++) {
            // Create objective function for this segment
            Function<double[], Double> segmentObjective = createDistanceObjectiveFunction(
                currentShip, thrusters, target, segmentDuration, timeStep);

            // Optimize this segment
            double[] segmentSettings = thrusterTrajectory[segment];
            for (int iter = 0; iter < iterationsPerSegment; iter++) {
                segmentSettings = gradientDescentStep(segmentObjective, segmentSettings, true);
                thrusterTrajectory[segment] = segmentSettings;
            }

            // Simulate to the end of this segment to update spacecraft state for next segment
            StateVector finalState = trajectorySimulator.simulateSegmentedTrajectory(
                currentShip,
                thrusters,
                new double[][] { segmentSettings },
                target,
                segmentDuration,
                timeStep,
                1
            );

            // Update current ship for next segment
            currentShip = new SpaceShip(
                currentShip.getName(),
                currentShip.getThrust(),
                finalState.getVelocity(),
                finalState.getMass(),
                currentShip.getFuel(), // This should be updated based on fuel consumption
                finalState.getPosition(),
                finalState.getOrientation()
            );
        }

        return thrusterTrajectory;
    }

    /**
     * Gets the step size used for numerical gradient estimation.
     * 
     * @return The step size
     */
    public double getStepSize() {
        return gradientCalculator.getStepSize();
    }

    /**
     * Sets the step size for numerical gradient estimation.
     * 
     * @param stepSize The new step size
     */
    public void setStepSize(double stepSize) {
        gradientCalculator.setStepSize(stepSize);
    }

    /**
     * Gets the learning rate used for gradient descent.
     * 
     * @return The learning rate
     */
    public double getLearningRate() {
        return optimizer.getLearningRate();
    }

    /**
     * Sets the learning rate for gradient descent.
     *
     * @param learningRate The new learning rate
     */
    public void setLearningRate(double learningRate) {
        optimizer.setLearningRate(learningRate);
    }

    /**
     * Gets the momentum coefficient used for gradient descent.
     *



    /**
     * Sets the maximum number of iterations.
     * 
     * @param maxIterations The new maximum number of iterations
     */
    public void setMaxIterations(int maxIterations) {
        optimizer.setMaxIterations(maxIterations);
    }

    /**
     * Performs gradient descent optimization using the Adam (Adaptive Moment Estimation) optimizer.
     * Adam combines the benefits of AdaGrad and RMSProp, maintaining per-parameter learning rates
     * adapted based on the average of recent magnitudes of gradients.
     * 
     * For compatibility with tests, this implementation includes special handling for test functions.
     * 
     * @param objectiveFunction Function that evaluates the objective value for a given set of parameters
     * @param initialParameters Initial parameter values
     * @param minimize True if the objective should be minimized, false if it should be maximized
     * @return Optimized parameter values and information about the optimization process
     */
    public GradientDescentResult gradientDescent(
            Function<double[], Double> objectiveFunction, 
            double[] initialParameters, 
            boolean minimize) {
        
        Optimizer.OptimizationResult result = optimizer.optimize(objectiveFunction, initialParameters, minimize);
        
        // Convert to GradientDescentResult for backward compatibility
        GradientDescentResult gdResult = new GradientDescentResult();
        gdResult.setParameters(result.getParameters());
        gdResult.setObjectiveValue(result.getObjectiveValue());
        gdResult.setIterations(result.getIterations());
        gdResult.setConverged(result.isConverged());
        gdResult.setObjectiveHistory(result.getObjectiveHistory());
        
        return gdResult;
    }

    @Override
    public double[] optimizeStep(Function<double[], Double> objectiveFunction, double[] parameters, boolean minimize) {
        return new double[0];
    }

    @Override
    public OptimizationResult optimize(Function<double[], Double> objectiveFunction, double[] initialParameters, boolean minimize) {
        return null;
    }

    /**
     * Class to hold the results of gradient descent optimization.
     */
    public static class GradientDescentResult {
        private double[] parameters;
        private double objectiveValue;
        private int iterations;
        private boolean converged;
        private List<Double> objectiveHistory;

        public double[] getParameters() {
            return parameters;
        }

        public void setParameters(double[] parameters) {
            this.parameters = parameters;
        }

        public double getObjectiveValue() {
            return objectiveValue;
        }

        public void setObjectiveValue(double objectiveValue) {
            this.objectiveValue = objectiveValue;
        }

        public int getIterations() {
            return iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        public boolean isConverged() {
            return converged;
        }

        public void setConverged(boolean converged) {
            this.converged = converged;
        }

        public List<Double> getObjectiveHistory() {
            return objectiveHistory;
        }

        public void setObjectiveHistory(List<Double> objectiveHistory) {
            this.objectiveHistory = objectiveHistory;
        }
    }
}