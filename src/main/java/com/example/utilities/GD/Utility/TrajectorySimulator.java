package com.example.utilities.GD.Utility;

import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.Vector3D;
import com.example.solar_system.CelestialBody;
import com.example.utilities.physics_utilities.PhysicsEngine;
import com.example.utilities.Ship.Thruster;
import com.example.utilities.Ship.StateVector;

import java.util.List;

/**
 * TrajectorySimulator is responsible for simulating spacecraft trajectories.
 * This class follows the Single Responsibility Principle by focusing solely on trajectory simulation.
 */
public class TrajectorySimulator {
    
    /**
     * Simulates a spacecraft trajectory with the given thruster settings.
     * 
     * @param spaceShip The spacecraft to simulate
     * @param thrusters List of thrusters on the spacecraft
     * @param thrusterSettings Throttle levels for each thruster
     * @param target Target celestial body
     * @param simulationTime Simulation time in seconds
     * @param timeStep Time step for simulation in seconds
     * @return Minimum distance to target during the simulation
     */
    public double simulateTrajectory(
            SpaceShip spaceShip,
            List<Thruster> thrusters,
            double[] thrusterSettings,
            CelestialBody target,
            double simulationTime,
            double timeStep) {
        
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
    }
    
    /**
     * Simulates a spacecraft trajectory with the given thruster settings, tracking fuel consumption.
     * 
     * @param spaceShip The spacecraft to simulate
     * @param thrusters List of thrusters on the spacecraft
     * @param thrusterSettings Throttle levels for each thruster
     * @param target Target celestial body
     * @param simulationTime Simulation time in seconds
     * @param timeStep Time step for simulation in seconds
     * @return Array containing [minimum distance to target, total fuel consumed]
     */
    public double[] simulateTrajectoryWithFuel(
            SpaceShip spaceShip,
            List<Thruster> thrusters,
            double[] thrusterSettings,
            CelestialBody target,
            double simulationTime,
            double timeStep) {
        
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

        return new double[] { minDistance, totalFuelConsumed };
    }
    
    /**
     * Simulates a spacecraft trajectory over multiple time segments with different thruster settings for each segment.
     * 
     * @param spaceShip The spacecraft to simulate
     * @param thrusters List of thrusters on the spacecraft
     * @param thrusterTrajectory Throttle levels for each thruster for each time segment
     * @param target Target celestial body
     * @param totalSimulationTime Total simulation time in seconds
     * @param timeStep Time step for simulation in seconds
     * @param numSegments Number of time segments
     * @return Final state of the spacecraft after the simulation
     */
    public StateVector simulateSegmentedTrajectory(
            SpaceShip spaceShip,
            List<Thruster> thrusters,
            double[][] thrusterTrajectory,
            CelestialBody target,
            double totalSimulationTime,
            double timeStep,
            int numSegments) {
        
        double segmentDuration = totalSimulationTime / numSegments;
        
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

        // Simulate each segment sequentially
        for (int segment = 0; segment < numSegments; segment++) {
            // Set up physics engine for this segment
            PhysicsEngine engine = new PhysicsEngine();
            engine.addBody(shipClone);
            engine.addBody(target);

            // Apply thruster settings for this segment
            double[] segmentSettings = thrusterTrajectory[segment];
            for (int i = 0; i < thrusters.size() && i < segmentSettings.length; i++) {
                thrusters.get(i).activate(segmentSettings[i]);
            }

            // Simulate this segment
            double currentTime = 0.0;
            while (currentTime < segmentDuration) {
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

                currentTime += timeStep;
            }
        }

        // Return the final state
        return new StateVector(
            shipClone.getPosition(),
            shipClone.getVelocity(),
            shipClone.getOrientation(),
            shipClone.getMass()
        );
    }
}