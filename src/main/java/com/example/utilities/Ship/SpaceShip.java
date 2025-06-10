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

    public SpaceShip(Vector3D initialPosition,
                      Vector3D initialVelocity,
                      double mass,
                      double thrust,
                      FuelTracker fuelTracker) {
        // We simply name all ships “SpaceShip” in the CelestialBody list.
        super("SpaceShip", mass, initialPosition, initialVelocity);
        this.thrust = thrust;
        this.fuelTracker = fuelTracker;
        this.orientation = new Vector3D(1.0, 0.0, 0.0); // default “+x” direction
        this.missionCapabilities = new java.util.HashMap<>();
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
        double fuelPerManeuver = 10.0; // kg

        return Math.floor(fuelAmount / fuelPerManeuver);
    }

    public double getThrust() {
        return thrust;
    }

    public void setThrust(double newThrust) {
        this.thrust = newThrust;
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

    public StateVector getState() {
        return new StateVector(getPosition(), getVelocity(), orientation, getMass());
    }

    public void applyImpulse(Vector3D vector3D) {
        double deltaV_mps = vector3D.norm();
        if (deltaV_mps < 1e-12) {
            return; // zero‐magnitude → no change
        }

        // 1) Compute the actual ΔV in km/s
        double deltaV_kmps = deltaV_mps / 1000.0;

        // 2) Compute the impulse “vector” in km·kg/s = (deltaV_kmps × mass_kg).
        //    But since CelestialBody stores velocity in km/s, we do:
        Vector3D impulseVec = orientation.scale(deltaV_kmps * this.getMass());

        // 3) Update ship’s velocity: v_new = v_old + impulseVec / mass
        Vector3D newVel = this.getVelocity().add(impulseVec.scale(1.0 / this.getMass()));
        this.setVelocity(newVel);

        // 4) Consume fuel: e.g. 0.01 kg of propellant per 1 m/s of ΔV
        double fuelConsumed = deltaV_mps * 0.01;
        if (fuelConsumed > fuelTracker.getRemaining()) {
            throw new IllegalStateException("Not enough fuel: needed "
                    + fuelConsumed
                    + " kg, have "
                    + fuelTracker.getRemainingFuel());
        }
        fuelTracker.consume(fuelConsumed);

    }
}
