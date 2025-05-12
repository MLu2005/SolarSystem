package com.example.spaceMissions;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Vector3D;

public class SpaceShip extends CelestialBody {

    private double thrust;
    private FuelTracker fuelTracker;

    public SpaceShip(String name, double thrust, Vector3D velocity, double mass, double initialFuel, Vector3D position) {
        super(name, mass, position, velocity);
        this.thrust = thrust;
        this.fuelTracker = new FuelTracker(initialFuel);
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
}
