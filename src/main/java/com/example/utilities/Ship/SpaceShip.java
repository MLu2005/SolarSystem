package com.example.utilities.Ship;

import com.example.solar_system.CelestialBody;
import com.example.spaceMissions.FuelTracker;
import com.example.utilities.Vector3D;

import java.util.HashMap;
import java.util.Map;

public class SpaceShip extends CelestialBody {

    private double thrust;
    private FuelTracker fuelTracker;
    private Vector3D orientation; // Orientation as a unit vector
    private Map<String, Double> missionCapabilities;

    public SpaceShip(String name, double thrust, Vector3D velocity, double mass, double initialFuel, Vector3D position) {
        super(name, mass, position, velocity);
        this.thrust = thrust;
        this.fuelTracker = new FuelTracker(initialFuel);
        this.orientation = velocity.normalize(); // Default orientation along velocity vector
        this.missionCapabilities = new HashMap<>();
        initializeMissionCapabilities(initialFuel);
    }

    public SpaceShip(String name, double thrust, Vector3D velocity, double mass, double initialFuel, Vector3D position, Vector3D orientation) {
        super(name, mass, position, velocity);
        this.thrust = thrust;
        this.fuelTracker = new FuelTracker(initialFuel);
        this.orientation = orientation.normalize(); // Ensure orientation is normalized
        this.missionCapabilities = new HashMap<>();
        initializeMissionCapabilities(initialFuel);
    }

    /**
     * Initializes the mission capabilities based on initial fuel.
     * 
     * @param initialFuel The initial fuel amount in kg
     */
    private void initializeMissionCapabilities(double initialFuel) {
        // Initialize default mission capabilities
        missionCapabilities.put("maxDeltaV", calculateMaxDeltaV(initialFuel));
        missionCapabilities.put("maxMissionDuration", calculateMaxMissionDuration(initialFuel));
        missionCapabilities.put("maxManeuvers", calculateMaxManeuvers(initialFuel));
        missionCapabilities.put("orbitCorrectionCapability", 1.0); // Full capability initially
    }

    /**
     * Calculates the maximum delta-V capability based on fuel amount.
     * Uses the Tsiolkovsky rocket equation: deltaV = ve * ln(m0/m1)
     * where ve is exhaust velocity, m0 is initial mass, m1 is final mass
     * 
     * @param fuelAmount The amount of fuel in kg
     * @return The maximum delta-V in m/s
     */
    private double calculateMaxDeltaV(double fuelAmount) {
        // Assuming a specific impulse of 300s (typical for chemical rockets)
        double exhaustVelocity = 300 * 9.81; // ve = Isp * g0
        double initialMass = getMass();
        double finalMass = initialMass - fuelAmount;

        // Prevent division by zero or negative mass
        if (finalMass <= 0) {
            finalMass = 0.1 * initialMass; // Assume 10% of initial mass is structural
        }

        return exhaustVelocity * Math.log(initialMass / finalMass);
    }

    /**
     * Calculates the maximum mission duration based on fuel amount.
     * 
     * @param fuelAmount The amount of fuel in kg
     * @return The maximum mission duration in seconds
     */
    private double calculateMaxMissionDuration(double fuelAmount) {
        // Assuming a baseline fuel consumption rate for life support and systems
        double baselineFuelRate = 0.01; // kg/s

        // Simple linear model: duration = fuel / consumption rate
        return fuelAmount / baselineFuelRate;
    }

    /**
     * Calculates the maximum number of maneuvers based on fuel amount.
     * 
     * @param fuelAmount The amount of fuel in kg
     * @return The maximum number of standard maneuvers
     */
    private double calculateMaxManeuvers(double fuelAmount) {
        // Assuming a standard maneuver consumes a fixed amount of fuel
        double fuelPerManeuver = 10.0; // kg

        return Math.floor(fuelAmount / fuelPerManeuver);
    }

    // --- Thrust ---
    public double getThrust() {
        return thrust;
    }

    public void setThrust(double newThrust) {
        this.thrust = newThrust;
    }

    // --- Fuel ---
    public void consumeFuel(double amount) {
        fuelTracker.consume(amount);
    }

    public double getFuel() {
        return fuelTracker.getRemaining();
    }

    public double getFuelUsed() {
        return fuelTracker.getUsed();
    }

    public void resetFuel() {
        fuelTracker.reset();
    }

    public FuelTracker getFuelTracker() {
        return fuelTracker;
    }

    /**
     * Estimates and updates the remaining mission capabilities based on current fuel status.
     * This method recalculates all mission capabilities based on the current fuel level
     * and updates the missionCapabilities map.
     * 
     * @return A map of mission capabilities and their current values
     */
    public Map<String, Double> estimateRemainingMissionCapabilities() {
        double currentFuel = getFuel();
        double initialFuel = fuelTracker.getInitialFuel();
        double fuelRatio = currentFuel / initialFuel;

        // Update capabilities based on current fuel
        missionCapabilities.put("maxDeltaV", calculateMaxDeltaV(currentFuel));
        missionCapabilities.put("maxMissionDuration", calculateMaxMissionDuration(currentFuel));
        missionCapabilities.put("maxManeuvers", calculateMaxManeuvers(currentFuel));

        // Orbit correction capability decreases non-linearly with fuel
        // At 50% fuel, we still have ~70% capability, but it drops more rapidly as fuel gets lower
        double orbitCorrectionCapability = Math.pow(fuelRatio, 0.7);
        missionCapabilities.put("orbitCorrectionCapability", orbitCorrectionCapability);

        // Add a summary capability metric (weighted average of all capabilities)
        double overallCapability = 0.3 * (missionCapabilities.get("maxDeltaV") / calculateMaxDeltaV(initialFuel)) +
                                  0.3 * (missionCapabilities.get("maxMissionDuration") / calculateMaxMissionDuration(initialFuel)) +
                                  0.2 * (missionCapabilities.get("maxManeuvers") / calculateMaxManeuvers(initialFuel)) +
                                  0.2 * orbitCorrectionCapability;

        missionCapabilities.put("overallCapability", overallCapability);

        return new HashMap<>(missionCapabilities);
    }

    /**
     * Gets the current mission capabilities without recalculating.
     * 
     * @return A map of mission capabilities and their current values
     */
    public Map<String, Double> getMissionCapabilities() {
        return new HashMap<>(missionCapabilities);
    }

    /**
     * Returns the current position of the spaceship.
     * @return the position vector (km)
     */
    public Vector3D getPosition() {
        return super.getPosition();
    }

    /**
     * Returns the current orientation of the spaceship.
     * @return the orientation vector (normalized)
     */
    public Vector3D getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation of the spaceship.
     * @param orientation the new orientation vector (will be normalized)
     */
    public void setOrientation(Vector3D orientation) {
        this.orientation = orientation.normalize();
    }

    /**
     * Returns a StateVector representing the complete state of the spacecraft.
     * @return the state vector containing position, velocity, orientation, and mass
     */
    public StateVector getStateVector() {
        return new StateVector(getPosition(), getVelocity(), orientation, getMass());
    }

    /**
     * Updates the spacecraft state from a StateVector.
     * @param stateVector the state vector to apply
     */
    public void applyStateVector(StateVector stateVector) {
        setPosition(stateVector.getPosition());
        setVelocity(stateVector.getVelocity());
        setOrientation(stateVector.getOrientation());
        // Note: Mass cannot be changed directly in CelestialBody
    }
}
