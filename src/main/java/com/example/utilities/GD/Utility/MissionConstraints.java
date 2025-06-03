package com.example.utilities.GD.Utility;

import com.example.spaceMissions.FuelTracker;

/**
 * MissionConstraints defines the limitations and constraints for a spacecraft mission,
 * including fuel limitations and thruster capabilities.
 */
public class MissionConstraints {
    
    private final double maxFuel;              // Maximum fuel capacity in kg
    private final double maxDeltaV;            // Maximum delta-V capability in km/s
    private final double maxThrust;            // Maximum thrust in Newtons
    private final double specificImpulse;      // Specific impulse in seconds
    private final double minSafeAltitude;      // Minimum safe altitude above Titan in km
    private final double maxAcceleration;      // Maximum safe acceleration in m/s²
    
    /**
     * Constructs a new MissionConstraints object with the specified parameters.
     *
     * @param maxFuel          Maximum fuel capacity in kg
     * @param maxDeltaV        Maximum delta-V capability in km/s
     * @param maxThrust        Maximum thrust in Newtons
     * @param specificImpulse  Specific impulse in seconds
     * @param minSafeAltitude  Minimum safe altitude above Titan in km
     * @param maxAcceleration  Maximum safe acceleration in m/s²
     */
    public MissionConstraints(double maxFuel, double maxDeltaV, double maxThrust, 
                             double specificImpulse, double minSafeAltitude, double maxAcceleration) {
        this.maxFuel = maxFuel;
        this.maxDeltaV = maxDeltaV;
        this.maxThrust = maxThrust;
        this.specificImpulse = specificImpulse;
        this.minSafeAltitude = minSafeAltitude;
        this.maxAcceleration = maxAcceleration;
    }
    
    /**
     * Checks if a maneuver is possible given the current fuel level and required delta-V.
     *
     * @param fuelTracker The current fuel tracker
     * @param deltaV      The required delta-V in km/s
     * @return true if the maneuver is possible, false otherwise
     */
    public boolean isManeuverPossible(FuelTracker fuelTracker, double deltaV) {
        if (deltaV > maxDeltaV) {
            return false; // Exceeds maximum delta-V capability
        }
        
        // Calculate fuel required using the rocket equation
        double fuelRequired = fuelTracker.getRemaining() * (1 - Math.exp(-deltaV / (specificImpulse * 9.81 / 1000)));
        
        return fuelRequired <= fuelTracker.getRemaining();
    }
    
    /**
     * Calculates the maximum possible delta-V given the current fuel level.
     *
     * @param fuelTracker The current fuel tracker
     * @param currentMass The current mass of the spacecraft in kg
     * @return The maximum possible delta-V in km/s
     */
    public double calculateMaxDeltaV(FuelTracker fuelTracker, double currentMass) {
        double m0 = currentMass;
        double m1 = currentMass - fuelTracker.getRemaining();
        
        if (m1 <= 0) {
            return 0.0; // No fuel left
        }
        
        // Rocket equation: deltaV = Isp * g0 * ln(m0/m1)
        double deltaV = specificImpulse * 9.81 / 1000 * Math.log(m0 / m1);
        
        return Math.min(deltaV, maxDeltaV);
    }
    
    /**
     * Calculates the burn time required for a given delta-V.
     *
     * @param deltaV      The required delta-V in km/s
     * @param currentMass The current mass of the spacecraft in kg
     * @return The burn time in seconds
     */
    public double calculateBurnTime(double deltaV, double currentMass) {
        // F = m * a, a = F/m, t = deltaV / a
        double acceleration = maxThrust / currentMass; // m/s²
        
        // Convert km/s to m/s
        double deltaVInMetersPerSecond = deltaV * 1000;
        
        return deltaVInMetersPerSecond / acceleration;
    }
    
    /**
     * Checks if the spacecraft is at a safe altitude above Titan.
     *
     * @param altitude The current altitude above Titan in km
     * @return true if the altitude is safe, false otherwise
     */
    public boolean isSafeAltitude(double altitude) {
        return altitude >= minSafeAltitude;
    }
    
    /**
     * Checks if the current acceleration is within safe limits.
     *
     * @param acceleration The current acceleration in m/s²
     * @return true if the acceleration is safe, false otherwise
     */
    public boolean isSafeAcceleration(double acceleration) {
        return acceleration <= maxAcceleration;
    }
    
    // Getters
    
    public double getMaxFuel() {
        return maxFuel;
    }
    
    public double getMaxDeltaV() {
        return maxDeltaV;
    }
    
    public double getMaxThrust() {
        return maxThrust;
    }
    
    public double getSpecificImpulse() {
        return specificImpulse;
    }
    
    public double getMinSafeAltitude() {
        return minSafeAltitude;
    }
    
    public double getMaxAcceleration() {
        return maxAcceleration;
    }
}