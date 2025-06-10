package com.example.utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.example.utilities.GA.Individual;
import com.example.utilities.HillClimb.InsertionThrustSchedule;

/**
 * A utility class for writing simulation results to files.
 * This class handles file writing operations for both GA and HillClimb packages.
 */
public class SimulationFileWriter {

    /**
     * Writes the results of a genetic algorithm simulation to a JSON file.
     * 
     * @param bestIndividuals The list of best individuals to include in the output
     * @param outputPath The path where the JSON file will be written
     */
    public static void writeGAResults(List<Individual> bestIndividuals, String outputPath) {

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"best_individuals\": [\n");

        for (int i = 0; i < bestIndividuals.size(); i++) {
            Individual ind = bestIndividuals.get(i);
            List<Double> genes = ind.genes();

            jsonBuilder.append("    {\n");
            jsonBuilder.append("      \"rank\": ").append(i).append(",\n");
            jsonBuilder.append("      \"fitness\": ").append(ind.getFitness()).append(",\n");
            jsonBuilder.append("      \"min_distance_to_titan_km\": ").append(ind.getMinDistanceKm()).append(",\n");

            jsonBuilder.append("      \"launch_position\": {\n");
            jsonBuilder.append("        \"x\": ").append(genes.get(0)).append(",\n");
            jsonBuilder.append("        \"y\": ").append(genes.get(1)).append(",\n");
            jsonBuilder.append("        \"z\": ").append(genes.get(2)).append("\n");
            jsonBuilder.append("      },\n");

            jsonBuilder.append("      \"launch_velocity\": {\n");
            jsonBuilder.append("        \"vx\": ").append(genes.get(3)).append(",\n");
            jsonBuilder.append("        \"vy\": ").append(genes.get(4)).append(",\n");
            jsonBuilder.append("        \"vz\": ").append(genes.get(5)).append("\n");
            jsonBuilder.append("      },\n");

            jsonBuilder.append("      \"launch_mass\": ").append(genes.get(6)).append(",\n");

            double dvRel = com.example.utilities.GA.GeneticTitan.computeDvRel(genes.get(3), genes.get(4), genes.get(5));
            jsonBuilder.append("      \"delta_v_relative_to_earth\": ").append(dvRel).append("\n");

            jsonBuilder.append("    }");
            if (i < bestIndividuals.size() - 1) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }

        jsonBuilder.append("  ]\n");
        jsonBuilder.append("}\n");

        writeToFile(jsonBuilder.toString(), outputPath);
    }

    /**
     * Writes the results of a hill climbing simulation to a JSON file.
     * 
     * @param schedule The thrust schedule from the hill climbing algorithm
     * @param bestCost The best cost found by the algorithm
     * @param outputPath The path where the JSON file will be written
     */
    public static void writeHillClimbResults(
            InsertionThrustSchedule schedule,
            double bestCost,
            String outputPath
    ) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // Total ΔV‐sum cost (unchanged)
        json.append("  \"totalCost\": ").append(bestCost).append(",\n");

        // Total fuel used
        json.append("  \"totalFuelUsed\": ")
                .append(schedule.getTotalFuelUsed())
                .append(",\n");

        double titanOrbitalPeriod = calculateTitanOrbitalPeriod();

        Vector3D positionAfterTwoTurns = calculatePositionAfterTwoTurns(schedule, titanOrbitalPeriod);
        Vector3D speedAfterTwoTurns    = calculateSpeedAfterTwoTurns(schedule, titanOrbitalPeriod);

        json.append("  \"positionAfterTwoTurns\": {\n")
                .append("    \"x\": ").append(positionAfterTwoTurns.getX()).append(",\n")
                .append("    \"y\": ").append(positionAfterTwoTurns.getY()).append(",\n")
                .append("    \"z\": ").append(positionAfterTwoTurns.getZ()).append("\n")
                .append("  },\n");

        json.append("  \"speedAfterTwoTurns\": {\n")
                .append("    \"vx\": ").append(speedAfterTwoTurns.getX()).append(",\n")
                .append("    \"vy\": ").append(speedAfterTwoTurns.getY()).append(",\n")
                .append("    \"vz\": ").append(speedAfterTwoTurns.getZ()).append("\n")
                .append("  },\n");

        double distanceToTitan = positionAfterTwoTurns.magnitude();
        json.append("  \"distanceToTitan\": ").append(distanceToTitan).append(",\n");

        json.append("  \"burns\": [\n");
        int nSlots = schedule.getNumSlots();
        for (int i = 0; i < nSlots; i++) {
            Vector3D burn    = schedule.getDeltaVAt(i);
            double  burnTime = i * schedule.getSlotDuration();

            json.append("    {\n")
                    .append("      \"time\": ").append(burnTime).append(",\n")
                    .append("      \"deltaV\": {\n")
                    .append("        \"x\": ").append(burn.getX()).append(",\n")
                    .append("        \"y\": ").append(burn.getY()).append(",\n")
                    .append("        \"z\": ").append(burn.getZ()).append("\n")
                    .append("      },\n")
                    .append("      \"fuelUsed\": ").append(schedule.getFuelUsedAt(i)).append(",\n")
                    .append("      \"thrust\": ").append(schedule.getThrustAt(i)).append("\n")
                    .append("    }")
                    .append(i < nSlots - 1 ? "," : "")
                    .append("\n");
        }
        json.append("  ]\n");
        json.append("}");

        writeToFile(json.toString(), outputPath);
    }


    /**
     * Helper method to write a string to a file.
     * 
     * @param content The content to write
     * @param filePath The path where the file will be written
     */
    private static void writeToFile(String content, String filePath) {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(content);
            System.out.println("Results written to " + filePath);
        } catch (IOException e) {
            System.out.println("Error writing results to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Computes the period of a circular orbit around Titan at the target altitude.
     * Uses the standard Newton–Kepler formulation for a circular orbit
     * 
     * @return Titan's orbital period in seconds
     */
    private static double calculateTitanOrbitalPeriod() {

        double TITAN_RADIUS_KM = 2575.0;
        double TARGET_ALTITUDE_KM = 300.0;
        double r = TITAN_RADIUS_KM + TARGET_ALTITUDE_KM;
        double mu = executables.Constants.MU_TITAN;
        return 2.0 * Math.PI * Math.sqrt(r * r * r / mu);
    }

    /**
     * Calculates the position after two turns around Titan.
     * 
     * @param schedule The thrust schedule
     * @param titanOrbitalPeriod Titan's orbital period
     * @return The position vector after two turns
     */
    private static Vector3D calculatePositionAfterTwoTurns(InsertionThrustSchedule schedule, double titanOrbitalPeriod) {
        // Implementation copied from TitanInsertionHillClimbing.calculatePositionAfterTwoTurns
        double TITAN_RADIUS_KM = 2575.0;
        double TARGET_ALTITUDE_KM = 300.0;
        double TARGET_RADIUS_KM = TITAN_RADIUS_KM + TARGET_ALTITUDE_KM;

        double r = TARGET_RADIUS_KM;
        double v = Math.sqrt(executables.Constants.MU_TITAN / r);

        Vector3D initialPosition = new Vector3D(r, 0, 0);
        Vector3D initialVelocity = new Vector3D(0, v, 0);

        double[] y0 = new double[6];
        y0[0] = initialPosition.getX();
        y0[1] = initialPosition.getY();
        y0[2] = initialPosition.getZ();
        y0[3] = initialVelocity.getX();
        y0[4] = initialVelocity.getY();
        y0[5] = initialVelocity.getZ();

        for (int i = 0; i < schedule.getNumSlots(); i++) {
            Vector3D deltaV = schedule.getDeltaVAt(i);
            // Convert from m/s to km/s (deltaV is in m/s)
            y0[3] += deltaV.getX() / 1000.0;
            y0[4] += deltaV.getY() / 1000.0;
            y0[5] += deltaV.getZ() / 1000.0;
        }

        java.util.function.BiFunction<Double, double[], double[]> f = (t, state) -> {
            double[] dydt = new double[6];

            dydt[0] = state[3];
            dydt[1] = state[4];
            dydt[2] = state[5];

            Vector3D pos = new Vector3D(state[0], state[1], state[2]);
            double distSq = pos.magnitudeSquared();
            double dist = Math.sqrt(distSq);

            // Acceleration = -mu * r / |r|^3
            double factor = -executables.Constants.MU_TITAN / (dist * distSq);

            dydt[3] = factor * state[0];
            dydt[4] = factor * state[1];
            dydt[5] = factor * state[2];

            return dydt;
        };

        executables.solvers.RKF45Solver solver = new executables.solvers.RKF45Solver();
        double simulationTime = 2.0 * titanOrbitalPeriod;
        int steps = (int)(simulationTime / executables.Constants.INITIAL_STEP_SIZE) + 1;
        steps = Math.min(steps, executables.Constants.MAX_STEPS);

        double[][] result = solver.solve(f, 0, y0, executables.Constants.INITIAL_STEP_SIZE, steps, null);

        double[] finalState = result[result.length - 1];

        return new Vector3D(finalState[0], finalState[1], finalState[2]);
    }

    /**
     * Calculates the speed after two turns around Titan.
     * 
     * @param schedule The thrust schedule
     * @param titanOrbitalPeriod Titan's orbital period
     * @return The velocity vector after two turns
     */
    private static Vector3D calculateSpeedAfterTwoTurns(InsertionThrustSchedule schedule, double titanOrbitalPeriod) {
        // Implementation copied from TitanInsertionHillClimbing.calculateSpeedAfterTwoTurns
        double TITAN_RADIUS_KM = 2575.0;
        double TARGET_ALTITUDE_KM = 300.0;
        double TARGET_RADIUS_KM = TITAN_RADIUS_KM + TARGET_ALTITUDE_KM;

        // Initial state: circular orbit at target radius
        double r = TARGET_RADIUS_KM;
        double v = Math.sqrt(executables.Constants.MU_TITAN / r);

        // Initial position and velocity (starting in the x-y plane)
        Vector3D initialPosition = new Vector3D(r, 0, 0);
        Vector3D initialVelocity = new Vector3D(0, v, 0);

        double[] y0 = new double[6];
        y0[0] = initialPosition.getX();
        y0[1] = initialPosition.getY();
        y0[2] = initialPosition.getZ();
        y0[3] = initialVelocity.getX();
        y0[4] = initialVelocity.getY();
        y0[5] = initialVelocity.getZ();

        for (int i = 0; i < schedule.getNumSlots(); i++) {
            Vector3D deltaV = schedule.getDeltaVAt(i);
            // Convert from m/s to km/s (deltaV is in m/s)
            y0[3] += deltaV.getX() / 1000.0;
            y0[4] += deltaV.getY() / 1000.0;
            y0[5] += deltaV.getZ() / 1000.0;
        }

        java.util.function.BiFunction<Double, double[], double[]> f = (t, state) -> {
            double[] dydt = new double[6];

            dydt[0] = state[3];
            dydt[1] = state[4];
            dydt[2] = state[5];

            Vector3D pos = new Vector3D(state[0], state[1], state[2]);
            double distSq = pos.magnitudeSquared();
            double dist = Math.sqrt(distSq);

            // Acceleration = -mu * r / |r|^3
            double factor = -executables.Constants.MU_TITAN / (dist * distSq);

            dydt[3] = factor * state[0];
            dydt[4] = factor * state[1];
            dydt[5] = factor * state[2];

            return dydt;
        };

        executables.solvers.RKF45Solver solver = new executables.solvers.RKF45Solver();

        double simulationTime = 2.0 * titanOrbitalPeriod;
        int steps = (int)(simulationTime / executables.Constants.INITIAL_STEP_SIZE) + 1;
        steps = Math.min(steps, executables.Constants.MAX_STEPS);

        double[][] result = solver.solve(f, 0, y0, executables.Constants.INITIAL_STEP_SIZE, steps, null);

        double[] finalState = result[result.length - 1];

        // final velocity
        return new Vector3D(finalState[3], finalState[4], finalState[5]);
    }
}
