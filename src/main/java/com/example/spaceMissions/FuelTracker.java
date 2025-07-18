package com.example.spaceMissions;

public class FuelTracker {
    private final double initialFuel;
    private double remainingFuel;

    public FuelTracker(double initialFuel) {
        this.initialFuel = initialFuel;
        this.remainingFuel = initialFuel;
    }

    public void consume(double amount) {
        remainingFuel = Math.max(0, remainingFuel - amount);
    }

    public double getRemaining() {
        return remainingFuel;
    }

    public double getUsed() {
        return initialFuel - remainingFuel;
    }

    public boolean hasFuel() {
        return remainingFuel > 0;
    }

    public void reset() {
        remainingFuel = initialFuel;
    }

    /**
     * Returns the initial fuel amount.
     * 
     * @return The initial fuel amount in kg
     */
    public double getInitialFuel() {
        return initialFuel;
    }

    public double getRemainingFuel() {
        return remainingFuel;
    }
}
