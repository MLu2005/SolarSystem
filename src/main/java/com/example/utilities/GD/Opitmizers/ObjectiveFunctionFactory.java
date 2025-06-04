package com.example.utilities.GD.Opitmizers;

import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.Vector3D;
import com.example.solar_system.CelestialBody;
import com.example.utilities.physics_utilities.PhysicsEngine;
import com.example.utilities.Ship.Thruster;

import java.util.List;
import java.util.function.Function;

/**
 * ObjectiveFunctionFactory is responsible for creating objective functions for different optimization scenarios.
 * This class follows the Single Responsibility Principle by focusing solely on objective function creation.
 */
public class ObjectiveFunctionFactory {
    
    /**
     * Creates an objective function for minimizing distance to a target celestial body.
     * 
     * @param spaceShip The spacecraft to control
     * @param thrusters List of thrusters on the spacecraft
     * @param target Target celestial body
     * @param simulationTime Simulation time in seconds
     * @param timeStep Time step for simulation in seconds
     * @return Function that takes thruster settings and returns the minimum distance to target
     */
    public Function<double[], Double> createDistanceObjectiveFunction(
            SpaceShip spaceShip, 
            List<Thruster> thrusters,
            CelestialBody target, 
            double simulationTime, 
            double timeStep) {

        return (double[] thrusterSettings) -> {
            // Clone the spacecraft to avoid modifying the original
            SpaceShip shipClone = new SpaceShip(
                spaceShip.getName(),
                spaceShip.getThrust(),
                spaceShip.getVelocity(),
                spaceShip.getMass(),
                spaceShip.getFuel(),
                spaceShip.getPosition(),
                spaceShip.getOrientation()
            );

            // Set up physics engine
            PhysicsEngine engine = new PhysicsEngine();
            engine.addBody(shipClone);
            engine.addBody(target);

            // Apply thruster settings
            for (int i = 0; i < thrusters.size() && i < thrusterSettings.length; i++) {
                thrusters.get(i).activate(thrusterSettings[i]);
            }

            double minDistance = Double.MAX_VALUE;
            double currentTime = 0.0;

            // Run simulation
            while (currentTime < simulationTime) {
                // Apply thruster forces
                Vector3D totalThrustForce = Vector3D.zero();
                for (Thruster thruster : thrusters) {
                    totalThrustForce = totalThrustForce.add(thruster.getThrustForce());
                }

                // Update spacecraft acceleration based on thruster forces
                Vector3D acceleration = totalThrustForce.scale(1.0 / shipClone.getMass());
                shipClone.setAcceleration(shipClone.getAcceleration().add(acceleration));

                // Step the simulation
                engine.step(timeStep);

                // Calculate distance to target
                double distance = shipClone.getPosition().subtract(target.getPosition()).magnitude();
                minDistance = Math.min(minDistance, distance);

                currentTime += timeStep;
            }

            return minDistance;
        };
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

        return (double[] thrusterSettings) -> {
            // Clone the spacecraft to avoid modifying the original
            SpaceShip shipClone = new SpaceShip(
                spaceShip.getName(),
                spaceShip.getThrust(),
                spaceShip.getVelocity(),
                spaceShip.getMass(),
                spaceShip.getFuel(),
                spaceShip.getPosition(),
                spaceShip.getOrientation()
            );

            // Set up physics engine
            PhysicsEngine engine = new PhysicsEngine();
            engine.addBody(shipClone);
            engine.addBody(target);

            // Apply thruster settings
            for (int i = 0; i < thrusters.size() && i < thrusterSettings.length; i++) {
                thrusters.get(i).activate(thrusterSettings[i]);
            }

            double minDistance = Double.MAX_VALUE;
            double currentTime = 0.0;
            double totalFuelConsumed = 0.0;

            // Run simulation
            while (currentTime < simulationTime) {
                // Calculate fuel consumption for this time step
                double fuelConsumedThisStep = 0.0;
                for (Thruster thruster : thrusters) {
                    fuelConsumedThisStep += thruster.getCurrentFuelConsumption() * timeStep;
                }
                totalFuelConsumed += fuelConsumedThisStep;

                // Consume fuel from the spacecraft
                shipClone.consumeFuel(fuelConsumedThisStep);

                // Apply thruster forces
                Vector3D totalThrustForce = Vector3D.zero();
                for (Thruster thruster : thrusters) {
                    totalThrustForce = totalThrustForce.add(thruster.getThrustForce());
                }

                // Update spacecraft acceleration based on thruster forces
                Vector3D acceleration = totalThrustForce.scale(1.0 / shipClone.getMass());
                shipClone.setAcceleration(shipClone.getAcceleration().add(acceleration));

                // Step the simulation
                engine.step(timeStep);

                // Calculate distance to target
                double distance = shipClone.getPosition().subtract(target.getPosition()).magnitude();
                minDistance = Math.min(minDistance, distance);

                currentTime += timeStep;

                // Stop simulation if we run out of fuel
                if (!shipClone.getFuelTracker().hasFuel()) {
                    break;
                }
            }

            // Normalize fuel consumption relative to initial fuel
            double normalizedFuelConsumption = totalFuelConsumed / spaceShip.getFuel();

            // Combine distance and fuel consumption with weights
            // distanceWeight = 1.0 - fuelWeight
            return (1.0 - fuelWeight) * minDistance + fuelWeight * normalizedFuelConsumption * 1000000; // Scale factor to make fuel consumption comparable to distance
        };
    }
}