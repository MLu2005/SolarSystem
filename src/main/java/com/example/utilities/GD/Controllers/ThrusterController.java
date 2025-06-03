package com.example.utilities.GD.Controllers;

import com.example.utilities.GD.Opitmizers.Optimizer;
import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.Vector3D;
import com.example.utilities.GD.Utility.MissionConstraints;
import com.example.utilities.Ship.Thruster;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * ThrusterController manages the mapping from gradient descent updates to thruster commands,
 * enforces constraints on thruster usage, and optimizes for fuel efficiency.
 */
public class ThrusterController {

    private final List<Thruster> thrusters;
    private final MissionConstraints constraints;
    private final SpaceShip spaceShip;
    private final Optimizer optimizer;

    // Thruster constraints
    private final double minThrottleLevel = 0.0;
    private final double maxThrottleLevel = 1.0;
    private double maxTotalThrust;
    private boolean[] directionLimitations; // true if thruster can only fire in its default direction

    /**
     * Constructs a new ThrusterController with the specified parameters.
     *
     * @param spaceShip    The spacecraft to control
     * @param thrusters    List of thrusters on the spacecraft
     * @param constraints  Mission constraints
     * @param optimizer    Optimizer for computing thruster settings
     */
    public ThrusterController(SpaceShip spaceShip, List<Thruster> thrusters, 
                             MissionConstraints constraints, Optimizer optimizer) {
        this.spaceShip = spaceShip;
        this.thrusters = new ArrayList<>(thrusters);
        this.constraints = constraints;
        this.optimizer = optimizer;

        // Initialize constraints
        this.maxTotalThrust = constraints.getMaxThrust();
        this.directionLimitations = new boolean[thrusters.size()];

        // By default, assume all thrusters can only fire in their default direction
        for (int i = 0; i < directionLimitations.length; i++) {
            directionLimitations[i] = true;
        }
    }

    /**
     * Maps gradient descent updates to thruster commands.
     *
     * @param gradientUpdates The gradient updates from the optimizer
     * @return Array of thruster throttle levels
     */
    public double[] mapGradientToThrusters(double[] gradientUpdates) {
        if (gradientUpdates.length != thrusters.size()) {
            throw new IllegalArgumentException("Gradient updates length must match number of thrusters");
        }

        double[] throttleLevels = new double[thrusters.size()];

        // Apply gradient updates to get new throttle levels
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double currentThrottle = thruster.getCurrentThrottleLevel();
            double newThrottle = currentThrottle + gradientUpdates[i];

            // Apply basic constraints
            throttleLevels[i] = Math.max(minThrottleLevel, Math.min(maxThrottleLevel, newThrottle));
        }

        // Apply additional constraints
        throttleLevels = enforceConstraints(throttleLevels);

        return throttleLevels;
    }

    /**
     * Enforces constraints on thruster usage.
     *
     * @param throttleLevels The initial throttle levels
     * @return Constrained throttle levels
     */
    public double[] enforceConstraints(double[] throttleLevels) {
        double[] constrainedLevels = throttleLevels.clone();

        // Enforce maximum total thrust constraint
        double totalThrust = calculateTotalThrust(constrainedLevels);
        if (totalThrust > maxTotalThrust) {
            double scaleFactor = maxTotalThrust / totalThrust;
            for (int i = 0; i < constrainedLevels.length; i++) {
                constrainedLevels[i] *= scaleFactor;
            }
        }

        // Enforce direction limitations
        for (int i = 0; i < thrusters.size(); i++) {
            if (directionLimitations[i]) {
                // If direction is limited, ensure the thruster is only used in its default direction
                Thruster thruster = thrusters.get(i);
                Vector3D orientation = spaceShip.getOrientation();
                Vector3D thrusterDirection = thruster.getDirection();

                // If spacecraft orientation is opposite to thruster direction, disable the thruster
                if (orientation.dot(thrusterDirection) < 0) {
                    constrainedLevels[i] = 0.0;
                }
            }
        }

        // Enforce safe acceleration constraint
        Vector3D totalForce = calculateTotalForce(constrainedLevels);
        double acceleration = totalForce.magnitude() / spaceShip.getMass();

        if (!constraints.isSafeAcceleration(acceleration)) {
            double maxSafeAcceleration = constraints.getMaxAcceleration();
            double scaleFactor = maxSafeAcceleration / acceleration;
            for (int i = 0; i < constrainedLevels.length; i++) {
                constrainedLevels[i] *= scaleFactor;
            }
        }

        return constrainedLevels;
    }

    /**
     * Calculates the total thrust produced by all thrusters at the given throttle levels.
     *
     * @param throttleLevels The throttle levels for each thruster
     * @return The total thrust in Newtons
     */
    private double calculateTotalThrust(double[] throttleLevels) {
        double totalThrust = 0.0;
        for (int i = 0; i < thrusters.size(); i++) {
            totalThrust += thrusters.get(i).getMaxThrust() * throttleLevels[i];
        }
        return totalThrust;
    }

    /**
     * Calculates the total force vector produced by all thrusters at the given throttle levels.
     *
     * @param throttleLevels The throttle levels for each thruster
     * @return The total force vector
     */
    private Vector3D calculateTotalForce(double[] throttleLevels) {
        Vector3D totalForce = Vector3D.zero();
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double throttle = throttleLevels[i];
            Vector3D force = thruster.getDirection().scale(thruster.getMaxThrust() * throttle);
            totalForce = totalForce.add(force);
        }
        return totalForce;
    }

    /**
     * Optimizes thruster efficiency to minimize fuel consumption.
     *
     * @param desiredForce The desired force vector to produce
     * @return Optimized throttle levels for each thruster
     */
    public double[] optimizeFuelEfficiency(Vector3D desiredForce) {
        // Create an objective function that minimizes fuel consumption
        // while achieving the desired force vector
        Function<double[], Double> objectiveFunction = (double[] throttleLevels) -> {
            // Calculate the force produced by these throttle levels
            Vector3D actualForce = calculateTotalForce(throttleLevels);

            // Calculate the error (difference between desired and actual force)
            double forceError = actualForce.subtract(desiredForce).magnitude();

            // Calculate the total fuel consumption
            double fuelConsumption = calculateTotalFuelConsumption(throttleLevels);

            // Objective: minimize both force error and fuel consumption
            // We use a weighted sum where force error is much more important
            // to ensure we achieve the desired force first, then optimize fuel
            return 100.0 * forceError + fuelConsumption;
        };

        // Initial throttle levels - start with more efficient configuration
        // Prioritize thrusters with lower fuel consumption rates
        double[] initialThrottleLevels = new double[thrusters.size()];

        // First, identify the most fuel-efficient thrusters for each direction
        double[] efficiencyRatings = new double[thrusters.size()];
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            // Efficiency = thrust force per unit of fuel
            efficiencyRatings[i] = thruster.getMaxThrust() / thruster.getFuelConsumptionRate();
            // Start with low throttle for all thrusters
            initialThrottleLevels[i] = 0.1;
        }

        // Increase throttle for thrusters that align with the desired force
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double alignment = thruster.getDirection().dot(desiredForce.normalize());

            // If thruster is aligned with desired force, increase its throttle
            if (alignment > 0) {
                initialThrottleLevels[i] = 0.5 * alignment * (efficiencyRatings[i] / 
                    Math.max(1.0, getMaxEfficiencyRating(efficiencyRatings)));
            }
        }

        // Run multiple optimizations with different initial conditions to avoid local minima
        double[] bestThrottleLevels = runOptimization(objectiveFunction, initialThrottleLevels);

        // Also try equal distribution as starting point
        double[] equalThrottleLevels = new double[thrusters.size()];
        for (int i = 0; i < thrusters.size(); i++) {
            equalThrottleLevels[i] = 0.5;
        }

        double[] alternateThrottleLevels = runOptimization(objectiveFunction, equalThrottleLevels);

        // Compare results and choose the better one
        if (evaluateObjective(objectiveFunction, alternateThrottleLevels) < 
            evaluateObjective(objectiveFunction, bestThrottleLevels)) {
            bestThrottleLevels = alternateThrottleLevels;
        }

        // Apply constraints to the optimized throttle levels
        return enforceConstraints(bestThrottleLevels);
    }

    /**
     * Runs the optimization process with the given initial throttle levels.
     *
     * @param objectiveFunction The objective function to minimize
     * @param initialThrottleLevels The initial throttle levels
     * @return The optimized throttle levels
     */
    private double[] runOptimization(Function<double[], Double> objectiveFunction, double[] initialThrottleLevels) {
        // Use optimizer to optimize
        Optimizer.OptimizationResult result = optimizer.optimize(
            objectiveFunction, initialThrottleLevels, true);

        return result.getParameters();
    }

    /**
     * Evaluates the objective function for the given throttle levels.
     *
     * @param objectiveFunction The objective function
     * @param throttleLevels The throttle levels to evaluate
     * @return The objective value
     */
    private double evaluateObjective(Function<double[], Double> objectiveFunction, double[] throttleLevels) {
        return objectiveFunction.apply(throttleLevels);
    }

    /**
     * Gets the maximum efficiency rating from the array of efficiency ratings.
     *
     * @param efficiencyRatings Array of efficiency ratings
     * @return The maximum efficiency rating
     */
    private double getMaxEfficiencyRating(double[] efficiencyRatings) {
        double max = 0.0;
        for (double rating : efficiencyRatings) {
            if (rating > max) {
                max = rating;
            }
        }
        return max;
    }

    /**
     * Calculates the total fuel consumption rate for the given throttle levels.
     *
     * @param throttleLevels The throttle levels for each thruster
     * @return The total fuel consumption rate in kg/s
     */
    private double calculateTotalFuelConsumption(double[] throttleLevels) {
        double totalConsumption = 0.0;
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double throttle = throttleLevels[i];
            totalConsumption += thruster.getFuelConsumptionRate() * throttle;
        }
        return totalConsumption;
    }

    /**
     * Applies the throttle levels to the thrusters and consumes the appropriate amount of fuel.
     *
     * @param throttleLevels The throttle levels to apply
     */
    public void applyThrottleLevels(double[] throttleLevels) {
        if (throttleLevels.length != thrusters.size()) {
            throw new IllegalArgumentException("Throttle levels length must match number of thrusters");
        }

        // Calculate total fuel consumption based on throttle levels
        double fuelConsumption = calculateTotalFuelConsumption(throttleLevels);

        // Activate thrusters
        for (int i = 0; i < thrusters.size(); i++) {
            thrusters.get(i).activate(throttleLevels[i]);
        }

        // Consume fuel from the spacecraft
        if (fuelConsumption > 0) {
            spaceShip.consumeFuel(fuelConsumption);
        }
    }

    /**
     * Sets whether a thruster is limited to firing only in its default direction.
     *
     * @param thrusterIndex The index of the thruster
     * @param limited       True if the thruster is limited to its default direction
     */
    public void setDirectionLimitation(int thrusterIndex, boolean limited) {
        if (thrusterIndex < 0 || thrusterIndex >= directionLimitations.length) {
            throw new IllegalArgumentException("Invalid thruster index");
        }
        directionLimitations[thrusterIndex] = limited;
    }

    /**
     * Sets the maximum total thrust constraint.
     *
     * @param maxTotalThrust The maximum total thrust in Newtons
     */
    public void setMaxTotalThrust(double maxTotalThrust) {
        this.maxTotalThrust = maxTotalThrust;
    }

    /**
     * Gets the list of thrusters managed by this controller.
     *
     * @return The list of thrusters
     */
    public List<Thruster> getThrusters() {
        return new ArrayList<>(thrusters);
    }

    /**
     * Gets the mission constraints.
     *
     * @return The mission constraints
     */
    public MissionConstraints getConstraints() {
        return constraints;
    }
}
