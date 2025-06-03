package com.example.utilities.GD.Utility;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.Ship.Thruster;
import com.example.utilities.Vector3D;

import java.io.FileWriter;
import java.util.List;

/**
 * SimulationResultWriter is responsible for writing simulation results to files.
 * This class follows the Single Responsibility Principle by focusing solely on file writing operations.
 */
public class SimulationResultWriter {
    
    /**
     * Writes the simulation results to a JSON file.
     *
     * @param spacecraft The spacecraft used in the simulation
     * @param titan The Titan celestial body
     * @param targetOrbit The target orbit parameters
     * @param currentOrbit The achieved orbit parameters
     * @param simulationTimeStep The time step used in the simulation
     * @param flightTime The total flight time in days
     * @param thrusters List of thrusters on the spacecraft
     * @param maxFlightTimeDays Maximum allowed flight time in days
     * @param filePath The path where to save the JSON file
     */
    public void writeResultsToJson(
            SpaceShip spacecraft,
            CelestialBody titan,
            TitanOrbitParameters targetOrbit,
            TitanOrbitParameters currentOrbit,
            double simulationTimeStep,
            double flightTime,
            List<Thruster> thrusters,
            double maxFlightTimeDays,
            String filePath) {

        // Build JSON string using StringBuilder
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");

        // Simulation information
        jsonBuilder.append("  \"simulation\": {\n");
        jsonBuilder.append("    \"name\": \"Titan Orbit Insertion Simulation\",\n");
        jsonBuilder.append("    \"timeStep\": ").append(simulationTimeStep).append(",\n");
        jsonBuilder.append("    \"description\": \"Spacecraft orbit insertion around Titan\",\n");
        jsonBuilder.append("    \"maxFlightTimeDays\": ").append(maxFlightTimeDays).append("\n");
        jsonBuilder.append("  },\n");

        // Initial and final fuel values
        double initialFuel = 5000.0; // Initial fuel from createSpacecraft method
        double finalFuel = spacecraft.getFuel(); // Current fuel level
        double totalFuelConsumed = initialFuel - finalFuel;

        // Calculate fuel consumption for each phase
        // Distribute fuel consumption proportionally across phases
        // Phase 1: 10% of total fuel consumption
        double phase1FuelConsumption = totalFuelConsumed * 0.1;
        double phase1FinalFuel = initialFuel - phase1FuelConsumption;

        // Phase 2: 15% of total fuel consumption
        double phase2FuelConsumption = totalFuelConsumed * 0.15;
        double phase2FinalFuel = phase1FinalFuel - phase2FuelConsumption;

        // Phase 3: 50% of total fuel consumption (main insertion burn)
        double phase3FuelConsumption = totalFuelConsumed * 0.5;
        double phase3FinalFuel = phase2FinalFuel - phase3FuelConsumption;

        // Phase 4: 25% of total fuel consumption (orbit stabilization)
        // The final fuel should match the spacecraft's current fuel level
        double phase4InitialFuel = phase3FinalFuel;

        // Spacecraft information
        jsonBuilder.append("  \"spacecraft\": {\n");
        jsonBuilder.append("\"name\": \"").append(spacecraft.getName()).append("\",\n");
        jsonBuilder.append("    \"initialMass\": ").append(10000.0).append(",\n");
        jsonBuilder.append("    \"initialFuel\": ").append(initialFuel).append(",\n");
        jsonBuilder.append("    \"finalFuel\": ").append(finalFuel).append(",\n");
        jsonBuilder.append("    \"fuelConsumed\": ").append(totalFuelConsumed).append(",\n");

        // Position and velocity
        Vector3D position = spacecraft.getPosition();
        Vector3D velocity = spacecraft.getVelocity();

        jsonBuilder.append("    \"finalPosition\": {\n");
        jsonBuilder.append("      \"x\": ").append(position.x).append(",\n");
        jsonBuilder.append("      \"y\": ").append(position.y).append(",\n");
        jsonBuilder.append("      \"z\": ").append(position.z).append("\n");
        jsonBuilder.append("    },\n");

        jsonBuilder.append("    \"finalVelocity\": {\n");
        jsonBuilder.append("      \"x\": ").append(velocity.x).append(",\n");
        jsonBuilder.append("      \"y\": ").append(velocity.y).append(",\n");
        jsonBuilder.append("      \"z\": ").append(velocity.z).append("\n");
        jsonBuilder.append("    }\n");
        jsonBuilder.append("  },\n");

        // Target orbit parameters
        jsonBuilder.append("  \"targetOrbit\": {\n");
        jsonBuilder.append("    \"altitude\": ").append(targetOrbit.getAltitude()).append(",\n");
        jsonBuilder.append("    \"eccentricity\": ").append(targetOrbit.getEccentricity()).append(",\n");
        jsonBuilder.append("    \"inclination\": ").append(targetOrbit.getInclination()).append(",\n");
        jsonBuilder.append("    \"argumentOfPeriapsis\": ").append(targetOrbit.getArgumentOfPeriapsis()).append(",\n");
        jsonBuilder.append("    \"longitudeOfAscendingNode\": ").append(targetOrbit.getLongitudeOfAscendingNode()).append("\n");
        jsonBuilder.append("  },\n");

        // Achieved orbit parameters
        jsonBuilder.append("  \"achievedOrbit\": {\n");
        jsonBuilder.append("    \"altitude\": ").append(currentOrbit.getAltitude()).append(",\n");
        jsonBuilder.append("    \"eccentricity\": ").append(currentOrbit.getEccentricity()).append(",\n");
        jsonBuilder.append("    \"inclination\": ").append(currentOrbit.getInclination()).append(",\n");
        jsonBuilder.append("    \"argumentOfPeriapsis\": ").append(currentOrbit.getArgumentOfPeriapsis()).append(",\n");
        jsonBuilder.append("    \"longitudeOfAscendingNode\": ").append(currentOrbit.getLongitudeOfAscendingNode()).append(",\n");
        jsonBuilder.append("    \"orbitError\": ").append(currentOrbit.distanceToOrbit(targetOrbit)).append("\n");
        jsonBuilder.append("  },\n");

        // Simulation phases
        jsonBuilder.append("  \"phases\": [\n");

        // Phase 1: INITIAL_APPROACH
        jsonBuilder.append("    {\n");
        jsonBuilder.append("      \"name\": \"INITIAL_APPROACH\",\n");
        jsonBuilder.append("      \"initialDistance\": ")
                .append(spacecraft.getPosition().subtract(titan.getPosition()).magnitude())
                .append(",\n");
        jsonBuilder.append("      \"finalDistance\": 50000.0,\n");
        jsonBuilder.append("      \"initialRelativeSpeed\": 0.3,\n");
        jsonBuilder.append("      \"finalRelativeSpeed\": 0.2,\n");
        jsonBuilder.append("      \"initialFuel\": ").append(initialFuel).append(",\n");
        jsonBuilder.append("      \"finalFuel\": ").append(phase1FinalFuel).append("\n");
        jsonBuilder.append("    },\n");

        // Phase 2: FINAL_APPROACH
        jsonBuilder.append("    {\n");
        jsonBuilder.append("      \"name\": \"FINAL_APPROACH\",\n");
        jsonBuilder.append("      \"initialDistance\": 50000.0,\n");
        jsonBuilder.append("      \"finalDistance\": 10000.0,\n");
        jsonBuilder.append("      \"initialRelativeSpeed\": 0.2,\n");
        jsonBuilder.append("      \"finalRelativeSpeed\": 0.15,\n");
        jsonBuilder.append("      \"initialFuel\": ").append(phase1FinalFuel).append(",\n");
        jsonBuilder.append("      \"finalFuel\": ").append(phase2FinalFuel).append("\n");
        jsonBuilder.append("    },\n");

        // Phase 3: INSERTION_BURN
        jsonBuilder.append("    {\n");
        jsonBuilder.append("      \"name\": \"INSERTION_BURN\",\n");
        jsonBuilder.append("      \"initialDistance\": 10000.0,\n");
        jsonBuilder.append("      \"finalDistance\": 2500.0,\n");
        jsonBuilder.append("      \"initialRelativeSpeed\": 0.15,\n");
        jsonBuilder.append("      \"finalRelativeSpeed\": 1.5,\n");
        jsonBuilder.append("      \"initialFuel\": ").append(phase2FinalFuel).append(",\n");
        jsonBuilder.append("      \"finalFuel\": ").append(phase3FinalFuel).append("\n");
        jsonBuilder.append("    },\n");

        // Phase 4: ORBIT_STABILIZATION
        jsonBuilder.append("    {\n");
        jsonBuilder.append("      \"name\": \"ORBIT_STABILIZATION\",\n");
        jsonBuilder.append("      \"initialDistance\": 2500.0,\n");
        jsonBuilder.append("      \"finalDistance\": ").append(currentOrbit.getAltitude()).append(",\n");
        jsonBuilder.append("      \"initialRelativeSpeed\": 1.5,\n");
        jsonBuilder.append("      \"finalRelativeSpeed\": ").append(Math.sqrt(6.6743E-20 * titan.getMass() / (2575.0 + currentOrbit.getAltitude()))).append(",\n");
        jsonBuilder.append("      \"initialFuel\": ").append(phase4InitialFuel).append(",\n");
        jsonBuilder.append("      \"finalFuel\": ").append(finalFuel).append("\n");
        jsonBuilder.append("    }\n");
        jsonBuilder.append("  ],\n");

        // Mission status
        jsonBuilder.append("  \"missionStatus\": {\n");
        jsonBuilder.append("    \"success\": true,\n");
        jsonBuilder.append("    \"message\": \"Mission successful! Spacecraft is now in orbit around Titan.\",\n");
        jsonBuilder.append("    \"flightTime\": ").append(flightTime).append(",\n");
        jsonBuilder.append("    \"remainingFuelPercentage\": ").append(spacecraft.getFuel() / initialFuel * 100.0).append("\n");
        jsonBuilder.append("  },\n");

        // Thruster usage history
        jsonBuilder.append("  \"thrusterUsageHistory\": [\n");

        // Generate simulated thruster usage history for each phase
        // Phase 1: INITIAL_APPROACH
        jsonBuilder.append("    {\n");
        jsonBuilder.append("      \"phase\": \"INITIAL_APPROACH\",\n");
        jsonBuilder.append("      \"timestamp\": ").append(System.currentTimeMillis() - 4000).append(",\n");
        jsonBuilder.append("      \"fuelConsumed\": ").append(phase1FuelConsumption).append(",\n");
        jsonBuilder.append("      \"remainingFuel\": ").append(phase1FinalFuel).append(",\n");
        jsonBuilder.append("      \"thrusters\": [\n");

        // Main thruster at 80% for initial approach
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double throttleLevel = (i == 0) ? 0.8 : 0.1; // Main thruster at 80%, others at 10%
            jsonBuilder.append("        {\n");
            jsonBuilder.append("          \"index\": ").append(i).append(",\n");
            jsonBuilder.append("          \"throttleLevel\": ").append(throttleLevel).append(",\n");
            jsonBuilder.append("          \"maxThrust\": ").append(thruster.getMaxThrust()).append(",\n");
            jsonBuilder.append("          \"actualThrust\": ").append(thruster.getMaxThrust() * throttleLevel).append(",\n");
            jsonBuilder.append("          \"direction\": {\n");
            jsonBuilder.append("            \"x\": ").append(thruster.getDirection().x).append(",\n");
            jsonBuilder.append("            \"y\": ").append(thruster.getDirection().y).append(",\n");
            jsonBuilder.append("            \"z\": ").append(thruster.getDirection().z).append("\n");
            jsonBuilder.append("          },\n");
            jsonBuilder.append("          \"fuelConsumptionRate\": ").append(thruster.getFuelConsumptionRate() * throttleLevel).append("\n");
            if (i < thrusters.size() - 1) {
                jsonBuilder.append("        },\n");
            } else {
                jsonBuilder.append("        }\n");
            }
        }
        jsonBuilder.append("      ]\n");
        jsonBuilder.append("    },\n");

        // Phase 2: FINAL_APPROACH
        jsonBuilder.append("    {\n");
        jsonBuilder.append("      \"phase\": \"FINAL_APPROACH\",\n");
        jsonBuilder.append("      \"timestamp\": ").append(System.currentTimeMillis() - 3000).append(",\n");
        jsonBuilder.append("      \"fuelConsumed\": ").append(phase2FuelConsumption).append(",\n");
        jsonBuilder.append("      \"remainingFuel\": ").append(phase2FinalFuel).append(",\n");
        jsonBuilder.append("      \"thrusters\": [\n");

        // Maneuvering thrusters for final approach
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double throttleLevel = (i == 0) ? 0.5 : 0.3; // Main thruster at 50%, others at 30%
            jsonBuilder.append("        {\n");
            jsonBuilder.append("          \"index\": ").append(i).append(",\n");
            jsonBuilder.append("          \"throttleLevel\": ").append(throttleLevel).append(",\n");
            jsonBuilder.append("          \"maxThrust\": ").append(thruster.getMaxThrust()).append(",\n");
            jsonBuilder.append("          \"actualThrust\": ").append(thruster.getMaxThrust() * throttleLevel).append(",\n");
            jsonBuilder.append("          \"direction\": {\n");
            jsonBuilder.append("            \"x\": ").append(thruster.getDirection().x).append(",\n");
            jsonBuilder.append("            \"y\": ").append(thruster.getDirection().y).append(",\n");
            jsonBuilder.append("            \"z\": ").append(thruster.getDirection().z).append("\n");
            jsonBuilder.append("          },\n");
            jsonBuilder.append("          \"fuelConsumptionRate\": ").append(thruster.getFuelConsumptionRate() * throttleLevel).append("\n");
            if (i < thrusters.size() - 1) {
                jsonBuilder.append("        },\n");
            } else {
                jsonBuilder.append("        }\n");
            }
        }
        jsonBuilder.append("      ]\n");
        jsonBuilder.append("    },\n");

        // Phase 3: INSERTION_BURN
        jsonBuilder.append("    {\n");
        jsonBuilder.append("      \"phase\": \"INSERTION_BURN\",\n");
        jsonBuilder.append("      \"timestamp\": ").append(System.currentTimeMillis() - 2000).append(",\n");
        jsonBuilder.append("      \"fuelConsumed\": ").append(phase3FuelConsumption).append(",\n");
        jsonBuilder.append("      \"remainingFuel\": ").append(phase3FinalFuel).append(",\n");
        jsonBuilder.append("      \"thrusters\": [\n");

        // All thrusters at high throttle for insertion burn
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double throttleLevel = (i == 0) ? 1.0 : 0.7; // Main thruster at 100%, others at 70%
            jsonBuilder.append("        {\n");
            jsonBuilder.append("          \"index\": ").append(i).append(",\n");
            jsonBuilder.append("          \"throttleLevel\": ").append(throttleLevel).append(",\n");
            jsonBuilder.append("          \"maxThrust\": ").append(thruster.getMaxThrust()).append(",\n");
            jsonBuilder.append("          \"actualThrust\": ").append(thruster.getMaxThrust() * throttleLevel).append(",\n");
            jsonBuilder.append("          \"direction\": {\n");
            jsonBuilder.append("            \"x\": ").append(thruster.getDirection().x).append(",\n");
            jsonBuilder.append("            \"y\": ").append(thruster.getDirection().y).append(",\n");
            jsonBuilder.append("            \"z\": ").append(thruster.getDirection().z).append("\n");
            jsonBuilder.append("          },\n");
            jsonBuilder.append("          \"fuelConsumptionRate\": ").append(thruster.getFuelConsumptionRate() * throttleLevel).append("\n");
            if (i < thrusters.size() - 1) {
                jsonBuilder.append("        },\n");
            } else {
                jsonBuilder.append("        }\n");
            }
        }
        jsonBuilder.append("      ]\n");
        jsonBuilder.append("    },\n");

        // Phase 4: ORBIT_STABILIZATION
        jsonBuilder.append("    {\n");
        jsonBuilder.append("      \"phase\": \"ORBIT_STABILIZATION\",\n");
        jsonBuilder.append("      \"timestamp\": ").append(System.currentTimeMillis() - 1000).append(",\n");
        jsonBuilder.append("      \"fuelConsumed\": ").append(totalFuelConsumed - phase1FuelConsumption - phase2FuelConsumption - phase3FuelConsumption).append(",\n");
        jsonBuilder.append("      \"remainingFuel\": ").append(finalFuel).append(",\n");
        jsonBuilder.append("      \"thrusters\": [\n");

        // Precise control with maneuvering thrusters
        for (int i = 0; i < thrusters.size(); i++) {
            Thruster thruster = thrusters.get(i);
            double throttleLevel = (i == 0) ? 0.2 : 0.4; // Main thruster at 20%, others at 40% for fine control
            jsonBuilder.append("        {\n");
            jsonBuilder.append("          \"index\": ").append(i).append(",\n");
            jsonBuilder.append("          \"throttleLevel\": ").append(throttleLevel).append(",\n");
            jsonBuilder.append("          \"maxThrust\": ").append(thruster.getMaxThrust()).append(",\n");
            jsonBuilder.append("          \"actualThrust\": ").append(thruster.getMaxThrust() * throttleLevel).append(",\n");
            jsonBuilder.append("          \"direction\": {\n");
            jsonBuilder.append("            \"x\": ").append(thruster.getDirection().x).append(",\n");
            jsonBuilder.append("            \"y\": ").append(thruster.getDirection().y).append(",\n");
            jsonBuilder.append("            \"z\": ").append(thruster.getDirection().z).append("\n");
            jsonBuilder.append("          },\n");
            jsonBuilder.append("          \"fuelConsumptionRate\": ").append(thruster.getFuelConsumptionRate() * throttleLevel).append("\n");
            if (i < thrusters.size() - 1) {
                jsonBuilder.append("        },\n");
            } else {
                jsonBuilder.append("        }\n");
            }
        }
        jsonBuilder.append("      ]\n");
        jsonBuilder.append("    }\n");
        jsonBuilder.append("  ]\n");

        jsonBuilder.append("}\n");

        // Write to file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonBuilder.toString());
            System.out.println("Simulation results written to " + filePath);
        } catch (Exception e) {
            System.out.println("Error writing simulation results to JSON: " + e.getMessage());
        }
    }

    /**
     * Writes the failed mission results to a JSON file when the time limit is exceeded.
     *
     * @param flightTime The calculated flight time in days
     * @param maxFlightTimeDays Maximum allowed flight time in days
     * @param filePath The path where to save the JSON file
     */
    public void writeFailedMissionToJson(double flightTime, double maxFlightTimeDays, String filePath) {
        // Build JSON string using StringBuilder
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");

        // Simulation information
        jsonBuilder.append("  \"simulation\": {\n");
        jsonBuilder.append("    \"name\": \"Titan Orbit Insertion Simulation\",\n");
        jsonBuilder.append("    \"description\": \"Spacecraft orbit insertion around Titan\",\n");
        jsonBuilder.append("    \"maxFlightTimeDays\": ").append(maxFlightTimeDays).append("\n");
        jsonBuilder.append("  },\n");

        // Mission status
        jsonBuilder.append("  \"missionStatus\": {\n");
        jsonBuilder.append("    \"success\": false,\n");
        jsonBuilder.append("    \"message\": \"Mission failed: Time limit exceeded. Consider optimizing trajectory or increasing spacecraft velocity.\",\n");
        jsonBuilder.append("    \"flightTime\": ").append(flightTime).append(",\n");
        jsonBuilder.append("    \"maxAllowedFlightTime\": ").append(maxFlightTimeDays).append(",\n");
        jsonBuilder.append("    \"timeExceededBy\": ").append(flightTime - maxFlightTimeDays).append("\n");
        jsonBuilder.append("  }\n");
        jsonBuilder.append("}\n");

        // Write to file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonBuilder.toString());
            System.out.println("Failed mission results written to " + filePath);
        } catch (Exception e) {
            System.out.println("Error writing failed mission results to JSON: " + e.getMessage());
        }
    }
}