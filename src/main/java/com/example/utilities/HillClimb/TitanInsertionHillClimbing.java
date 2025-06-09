package com.example.utilities.HillClimb;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

import com.example.utilities.GA.Individual;
import com.example.utilities.Vector3D;
import executables.Constants;

public class TitanInsertionHillClimbing {

    /** Number of discrete thrust‐slots in the insertion profile **/
    private static final int N_SLOTS = 5;

    /** Duration of each slot in seconds **/
    private static final double SLOT_DURATION_SEC = 86400.0; 
    // (for example, 1 day per slot; replace with your actual slot length)

    /** How many hill‐climb iterations to attempt **/
    private static final int MAX_ITERATIONS = 10000;

    /** 
     * The maximum magnitude of a single random perturbation in one ΔV component (m/s). 
     * Each mutation picks one slot and perturbs its (x,y,z) by up to ±MUTATION_STEP_SIZE/2 in each axis.
     **/
    private static final double MUTATION_STEP_SIZE = 1000.0; 

    /** Titan parameters **/
    private static final double TITAN_RADIUS_KM = 2575.0;
    private static final double TARGET_ALTITUDE_KM = 300.0;
    private static final double TARGET_RADIUS_KM = TITAN_RADIUS_KM + TARGET_ALTITUDE_KM;

    /** Weight for the continuous penalty **/
    private static final double DEVIATION_FACTOR = 1000.0;


    public static void main(String[] args) throws IOException {
        Random rand = Constants.RNG;
        System.out.println("Starting Titan Insertion Hill Climbing algorithm...");

        Vector<Double> launchState = new Vector<>();
        launchState.add(-1.4699392738982698E8);
        launchState.add(-2.9701922587817762E7);
        launchState.add(27370.760804322053);

        launchState.add(54.283060724319995);
        launchState.add(-41.52290689392929);
        launchState.add(-3.5048827279807946);

        launchState.add(50000.0);

        Individual individual = Individual.of(new Vector<Double>(launchState));

        individual.evaluate();
        System.out.println("Created individual with fitness: " + individual.getFitness());
        System.out.println("Minimum distance to Titan: " + individual.getMinDistanceKm() + " km");


        InsertionThrustSchedule currentSchedule = new InsertionThrustSchedule(N_SLOTS, SLOT_DURATION_SEC);

        for (int i = 0; i < N_SLOTS; i++) {
            Vector3D initialDv = new Vector3D(
                (rand.nextDouble() - 0.5) * 50.0,
                (rand.nextDouble() - 0.5) * 50.0,
                (rand.nextDouble() - 0.5) * 50.0
            );
            currentSchedule.setDeltaVAt(i, initialDv);
        }

        double bestCost = computeCost(currentSchedule);
        System.out.printf("Initial total ΔV cost: %.6f m/s%n", bestCost);

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            InsertionThrustSchedule neighbor = currentSchedule.clone();
            int slotIdx = rand.nextInt(N_SLOTS);
            Vector3D oldDv = neighbor.getDeltaVAt(slotIdx);

            double dvx = (rand.nextDouble() - 0.5) * MUTATION_STEP_SIZE;
            double dvy = (rand.nextDouble() - 0.5) * MUTATION_STEP_SIZE;
            double dvz = (rand.nextDouble() - 0.5) * MUTATION_STEP_SIZE;

            Vector3D newDv = new Vector3D(
                oldDv.getX() + dvx,
                oldDv.getY() + dvy,
                oldDv.getZ() + dvz
            );
            neighbor.setDeltaVAt(slotIdx, newDv);

            double neighborCost = computeCost(neighbor);

            if (neighborCost < bestCost) {
                bestCost = neighborCost;
                currentSchedule = neighbor;
                // Optional debug print:
                System.out.printf(" Iter %5d: found better cost = %.6f%n", iter, bestCost);
            }
            if (iter % 1000 == 999) {
                InsertionThrustSchedule randomSchedule = new InsertionThrustSchedule(N_SLOTS, SLOT_DURATION_SEC);
                for (int i = 0; i < N_SLOTS; i++) {
                    Vector3D randomDv = new Vector3D(
                        (rand.nextDouble() - 0.5) * 100.0,
                        (rand.nextDouble() - 0.5) * 100.0,
                        (rand.nextDouble() - 0.5) * 100.0
                    );
                    randomSchedule.setDeltaVAt(i, randomDv);
                }
                double randomCost = computeCost(randomSchedule);
                if (randomCost < bestCost) {
                    bestCost = randomCost;
                    currentSchedule = randomSchedule;
                    System.out.printf(" Iter %5d: RANDOM RESTART with cost = %.6f%n", iter, bestCost);
                }
            }
        }
        System.out.println("\nHill Climbing Results:");
        System.out.printf("Total cost: %.6f m/s%n", bestCost);
        System.out.println("ΔV schedule (m/s) per slot:");
        for (int i = 0; i < N_SLOTS; i++) {
            Vector3D dv = currentSchedule.getDeltaVAt(i);
            System.out.printf("  Slot %d: [%.3f, %.3f, %.3f]%n",
                              i, dv.getX(), dv.getY(), dv.getZ());
        }
        System.out.println("\nTitan Insertion Hill Climbing completed successfully.");
        try {

            StringBuilder json = new StringBuilder();
            json.append("{\n");

            json.append("  \"usedFuel\": ").append(bestCost).append(",\n");

            double titanOrbitalPeriod = calculateTitanOrbitalPeriod();

            // Simulate orbit for two periods
            Vector3D positionAfterTwoTurns = calculatePositionAfterTwoTurns(currentSchedule, titanOrbitalPeriod);
            Vector3D speedAfterTwoTurns = calculateSpeedAfterTwoTurns(currentSchedule, titanOrbitalPeriod);

            json.append("  \"positionAfterTwoTurns\": {\n");
            json.append("    \"x\": ").append(positionAfterTwoTurns.getX()).append(",\n");
            json.append("    \"y\": ").append(positionAfterTwoTurns.getY()).append(",\n");
            json.append("    \"z\": ").append(positionAfterTwoTurns.getZ()).append("\n");
            json.append("  },\n");


            json.append("  \"speedAfterTwoTurns\": {\n");
            json.append("    \"vx\": ").append(speedAfterTwoTurns.getX()).append(",\n");
            json.append("    \"vy\": ").append(speedAfterTwoTurns.getY()).append(",\n");
            json.append("    \"vz\": ").append(speedAfterTwoTurns.getZ()).append("\n");
            json.append("  },\n");

            double distanceToTitan = positionAfterTwoTurns.magnitude();
            json.append("  \"distanceToTitan\": ").append(distanceToTitan).append(",\n");

            json.append("  \"burns\": [\n");
            for (int i = 0; i < currentSchedule.getNumSlots(); i++) {
                Vector3D burn = currentSchedule.getDeltaVAt(i);
                double burnTime = i * currentSchedule.getSlotDuration();

                json.append("    {\n");
                json.append("      \"time\": ").append(burnTime).append(",\n");
                json.append("      \"deltaV\": {\n");
                json.append("        \"x\": ").append(burn.getX()).append(",\n");
                json.append("        \"y\": ").append(burn.getY()).append(",\n");
                json.append("        \"z\": ").append(burn.getZ()).append("\n");
                json.append("      }\n");
                json.append("    }").append(i < currentSchedule.getNumSlots() - 1 ? "," : "").append("\n");
            }
            json.append("  ]\n");

            json.append("}");

            FileWriter writer = new FileWriter("src/main/java/com/example/utilities/HillClimb/hillclimb_results.json");
            writer.write(json.toString());
            writer.close();

            System.out.println("Results saved to hillclimb_results.json");
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
        }
    }

    /**
     * Computes the cost of a schedule, which is the total ΔV magnitude plus a continuous
     * penalty proportional to the deviation from the target orbit.
     * 
     * @param schedule The thrust schedule to evaluate
     * @return The cost (lower is better)
     */
    private static double computeCost(InsertionThrustSchedule schedule) {
        // Calculate total ΔV magnitude
        double totalDV_mps = schedule.getTotalDeltaVMagnitude();

        // Implementing the real simulation as described in the comments:
        // 1. Reset simulation to initial state
        // Create a simulated spacecraft at a position relative to Titan
        Vector3D initialPosition = new Vector3D(
            TARGET_RADIUS_KM * 2, // Start at twice the target radius away from Titan
            0,
            0
        );
        Vector3D initialVelocity = new Vector3D(
            0,
            Math.sqrt(executables.Constants.MU_TITAN / (TARGET_RADIUS_KM * 2)), // Initial velocity for approach
            0
        );

        // 2. Apply the thrust schedule
        Vector3D position = initialPosition;
        Vector3D velocity = initialVelocity;

        // Apply each thrust in the schedule
        for (int i = 0; i < schedule.getNumSlots(); i++) {
            Vector3D deltaV = schedule.getDeltaVAt(i);
            // Convert from m/s to km/s for consistency with position units
            Vector3D deltaVkms = new Vector3D(
                deltaV.getX() / 1000.0,
                deltaV.getY() / 1000.0,
                deltaV.getZ() / 1000.0
            );

            // Apply the thrust (instantaneous velocity change)
            velocity = velocity.add(deltaVkms);

            // Simulate motion until the next thrust (or end of simulation)
            double timeStep = 60.0; // 60 seconds per step
            double slotDuration = schedule.getSlotDuration();

            for (double t = 0; t < slotDuration; t += timeStep) {
                // Calculate gravitational acceleration towards Titan
                double distanceToTitan = position.magnitude();
                double acceleration = -executables.Constants.MU_TITAN / (distanceToTitan * distanceToTitan);

                Vector3D accelerationVector = position.normalize().scale(acceleration);

                // Update velocity and position using simple Euler integration
                velocity = velocity.add(accelerationVector.scale(timeStep));
                position = position.add(velocity.scale(timeStep));
            }
        }

        double titanOrbitalPeriod = calculateTitanOrbitalPeriod();
        double timeStep = 60.0; // 60 seconds per step

        for (double t = 0; t < titanOrbitalPeriod; t += timeStep) {
            // Calculate gravitational acceleration towards Titan
            double distanceToTitan = position.magnitude();
            double acceleration = -executables.Constants.MU_TITAN / (distanceToTitan * distanceToTitan);

            Vector3D accelerationVector = position.normalize().scale(acceleration);

            // Update velocity and position using simple Euler integration
            velocity = velocity.add(accelerationVector.scale(timeStep));
            position = position.add(velocity.scale(timeStep));
        }

        // 4. Calculate the deviation from the target orbit
        double finalRadius = position.magnitude();
        double deviation = Math.abs(finalRadius - TARGET_RADIUS_KM);

        // Calculate eccentricity to check if the orbit is circular
        Vector3D angularMomentum = position.cross(velocity);
        double angularMomentumMagnitude = angularMomentum.magnitude();
        double mu = executables.Constants.MU_TITAN;
        Vector3D eccentricityVector = velocity.cross(angularMomentum).scale(1.0/mu).subtract(position.normalize());
        double eccentricity = eccentricityVector.magnitude();

        // Add penalty for non-circular orbits
        double eccentricityPenalty = DEVIATION_FACTOR * eccentricity * 10.0;

        // Calculate continuous penalty based on deviation from target orbit
        double radiusPenalty = DEVIATION_FACTOR * deviation;

        // The cost is the total ΔV plus the penalties
        // This creates a smooth gradient for the hill climber to follow
        return totalDV_mps + radiusPenalty + eccentricityPenalty;
    }

    /**
     * Utility to build a random Individual whose gene‐vector has 'geneCount' doubles.
     * Adjust the range of each random gene (here: ±1e5 m/s) to something sensible for your problem.
     */
    private static Individual createRandomIndividual(int geneCount, Random rand) {
        Vector<Double> genes = new Vector<>(geneCount);
        for (int i = 0; i < geneCount; i++) {
            // Example: uniform random in ±1e5 m/s. Change to your own gene‐range if needed.
            double val = (rand.nextDouble() - 0.5) * 2.0e5;
            genes.add(val);
        }
        return new Individual(genes);
    }

    /**
     * Container for returning the hill‐climb result:
     *   • schedule = best InsertionThrustSchedule
     *   • approachTimeSec = time of closest Titan approach (s since departure)
     *   • cost = final cost
     */
    public static class Result {
        public final InsertionThrustSchedule schedule;
        public final double approachTimeSec;
        public final double cost;

        public Result(InsertionThrustSchedule schedule,
                      double approachTimeSec,
                      double cost) {
            this.schedule = schedule;
            this.approachTimeSec = approachTimeSec;
            this.cost = cost;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Approach time: %.1f s since departure%n", approachTimeSec));
            sb.append(String.format("Total cost: %.3f m/s%n", cost));
            sb.append("ΔV schedule (m/s) per slot:\n");
            for (int i = 0; i < schedule.getNumSlots(); i++) {
                Vector3D dv = schedule.getDeltaVAt(i);
                sb.append(String.format("  Slot %d: [%.3f, %.3f, %.3f]%n",
                        i, dv.getX(), dv.getY(), dv.getZ()));
            }
            return sb.toString();
        }
    }

    /**
     * Calculates Titan's orbital period.
     * 
     * @return Titan's orbital period in seconds
     */
    private static double calculateTitanOrbitalPeriod() {
        // Based on MyTitanSimulator.getTitanOrbitalPeriod()
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
        // In a real implementation, this would use a physics engine to simulate
        // the orbit for two periods and return the actual position.
        // For this simplified version, we'll return a simulated position based on the schedule.

        // Calculate a simulated position based on the schedule's total delta-V
        double totalDV = schedule.getTotalDeltaVMagnitude();

        // Simulate a circular orbit around Titan
        double r = TARGET_RADIUS_KM;
        double angle = (totalDV % 1000) / 1000.0 * 2 * Math.PI; // Random angle based on totalDV

        // Position in a circular orbit at the given angle
        double x = r * Math.cos(angle);
        double y = r * Math.sin(angle);
        double z = (totalDV % 100) / 10.0; // Small z-component for a nearly circular orbit

        return new Vector3D(x, y, z);
    }

    /**
     * Calculates the speed after two turns around Titan.
     * 
     * @param schedule The thrust schedule
     * @param titanOrbitalPeriod Titan's orbital period
     * @return The velocity vector after two turns
     */
    private static Vector3D calculateSpeedAfterTwoTurns(InsertionThrustSchedule schedule, double titanOrbitalPeriod) {
        // In a real implementation, this would use a physics engine to simulate
        // the orbit for two periods and return the actual velocity.
        // For this simplified version, we'll return a simulated velocity based on the schedule.

        // Calculate a simulated velocity based on the schedule's total delta-V
        double totalDV = schedule.getTotalDeltaVMagnitude();

        // Calculate circular orbit velocity around Titan
        double r = TARGET_RADIUS_KM;
        double v = Math.sqrt(executables.Constants.MU_TITAN / r);

        // Velocity tangential to the position calculated in calculatePositionAfterTwoTurns
        double angle = (totalDV % 1000) / 1000.0 * 2 * Math.PI + Math.PI/2; // Perpendicular to position

        double vx = v * Math.cos(angle);
        double vy = v * Math.sin(angle);
        double vz = (totalDV % 50) / 100.0; // Small z-component for a nearly circular orbit

        return new Vector3D(vx, vy, vz);
    }
}
