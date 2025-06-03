package com.example.utilities.Ship;

import com.example.utilities.Vector3D;

/**
 * Thruster represents a propulsion system that can apply force to a celestial body.
 * It has properties for thrust force, direction, and fuel consumption.
 */
public class Thruster {
    private double maxThrust; // Maximum thrust force in Newtons
    private Vector3D direction; // Direction of thrust (normalized)
    private double fuelConsumptionRate; // kg/s at max thrust
    private double currentThrottleLevel; // 0.0 to 1.0
    private boolean isActive;

    /**
     * Constructs a new Thruster with specified maximum thrust, direction, and fuel consumption rate.
     * 
     * @param maxThrust Maximum thrust force in Newtons
     * @param direction Direction vector for the thrust (will be normalized)
     * @param fuelConsumptionRate Fuel consumption rate in kg/s at maximum thrust
     */
    public Thruster(double maxThrust, Vector3D direction, double fuelConsumptionRate) {
        this.maxThrust = maxThrust;
        this.direction = direction.normalize(); // Ensure direction is normalized
        this.fuelConsumptionRate = fuelConsumptionRate;
        this.currentThrottleLevel = 0.0; // Start with thruster off
        this.isActive = false;
    }

    /**
     * Activates the thruster at the specified throttle level.
     * 
     * @param throttleLevel Value between 0.0 (off) and 1.0 (full thrust)
     * @throws IllegalArgumentException if throttleLevel is outside valid range
     */
    public void activate(double throttleLevel) {
        if (throttleLevel < 0.0 || throttleLevel > 1.0) {
            throw new IllegalArgumentException("Throttle level must be between 0.0 and 1.0");
        }
        this.currentThrottleLevel = throttleLevel;
        this.isActive = throttleLevel > 0.0;
    }

    /**
     * Deactivates the thruster.
     */
    public void deactivate() {
        this.currentThrottleLevel = 0.0;
        this.isActive = false;
    }

    /**
     * Calculates the current thrust force vector based on direction and throttle level.
     * 
     * @return Vector3D representing the current thrust force
     */
    public Vector3D getThrustForce() {
        if (!isActive) {
            return Vector3D.zero();
        }
        return direction.scale(maxThrust * currentThrottleLevel);
    }

    /**
     * Calculates the current fuel consumption rate based on throttle level.
     * 
     * @return Current fuel consumption in kg/s
     */
    public double getCurrentFuelConsumption() {
        return fuelConsumptionRate * currentThrottleLevel;
    }

    /**
     * Changes the direction of the thruster.
     * 
     * @param newDirection New direction vector (will be normalized)
     */
    public void setDirection(Vector3D newDirection) {
        this.direction = newDirection.normalize();
    }

    /**
     * Returns the current direction of the thruster.
     * 
     * @return Normalized direction vector
     */
    public Vector3D getDirection() {
        return direction;
    }

    /**
     * Returns the maximum thrust of the thruster.
     * 
     * @return Maximum thrust in Newtons
     */
    public double getMaxThrust() {
        return maxThrust;
    }

    /**
     * Sets a new maximum thrust value.
     * 
     * @param maxThrust New maximum thrust in Newtons
     */
    public void setMaxThrust(double maxThrust) {
        this.maxThrust = maxThrust;
    }

    /**
     * Returns the fuel consumption rate at maximum thrust.
     * 
     * @return Fuel consumption rate in kg/s
     */
    public double getFuelConsumptionRate() {
        return fuelConsumptionRate;
    }

    /**
     * Sets a new fuel consumption rate.
     * 
     * @param fuelConsumptionRate New fuel consumption rate in kg/s
     */
    public void setFuelConsumptionRate(double fuelConsumptionRate) {
        this.fuelConsumptionRate = fuelConsumptionRate;
    }

    /**
     * Returns the current throttle level.
     * 
     * @return Current throttle level (0.0 to 1.0)
     */
    public double getCurrentThrottleLevel() {
        return currentThrottleLevel;
    }

    /**
     * Checks if the thruster is currently active.
     * 
     * @return true if the thruster is active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }
}