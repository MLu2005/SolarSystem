package com.example.utilities.HillClimb;

import java.util.Random;
import java.util.Vector;

import com.example.utilities.GA.Individual;
import com.example.utilities.Vector3D;

public class TitanInsertionHillClimbing {

    // ─── TUNE THESE PARAMETERS ────────────────────────────────────────────────

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

    // ──────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        Random rand = new Random();

        System.out.println("Starting Titan Insertion Hill Climbing algorithm...");

        // Create a random Individual
        Individual individual = Individual.of(new Vector<Double>(
                java.util.Arrays.asList(
                        -1.469936664885829E8,
                        -2.9700657414408103E7,
                        27290.04661092061,
                        56.02682883113462,
                        -31.71008058719922,
                        -13.988410108682189,
                        50000.0
                )
        ));
        // Evaluate the individual to ensure it has valid fitness
        individual.evaluate();
        System.out.println("Created individual with fitness: " + individual.getFitness());
        System.out.println("Minimum distance to Titan: " + individual.getMinDistanceKm() + " km");

        // 1) Create an initial schedule with small non-zero values
        InsertionThrustSchedule currentSchedule = new InsertionThrustSchedule(N_SLOTS, SLOT_DURATION_SEC);

        // Initialize with small non-zero values to help escape local minimum at zero
        for (int i = 0; i < N_SLOTS; i++) {
            Vector3D initialDv = new Vector3D(
                (rand.nextDouble() - 0.5) * 50.0,  // Small random values (-25 to 25 m/s)
                (rand.nextDouble() - 0.5) * 50.0,
                (rand.nextDouble() - 0.5) * 50.0
            );
            currentSchedule.setDeltaVAt(i, initialDv);
        }

        // 2) Compute its "cost" (total ΔV). Lower is better.
        double bestCost = computeCost(currentSchedule);
        System.out.printf("Initial total ΔV cost: %.6f m/s%n", bestCost);

        // 4) Hill‐climb loop: at each iteration, clone + mutate + evaluate
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            // a) Make a deep copy of the current best schedule
            InsertionThrustSchedule neighbor = currentSchedule.clone();

            // b) Randomly pick one slot to perturb by (Δvx, Δvy, Δvz)
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

            // c) Evaluate the mutated schedule
            double neighborCost = computeCost(neighbor);

            // d) If it's better (lower cost), accept it
            if (neighborCost < bestCost) {
                bestCost = neighborCost;
                currentSchedule = neighbor;
                // Optional debug print:
                System.out.printf(" Iter %5d: found better cost = %.6f%n", iter, bestCost);
            }

            // e) Occasionally do a random restart to escape local minima
            if (iter % 1000 == 999) {
                // Create a new random schedule with small non-zero values
                InsertionThrustSchedule randomSchedule = new InsertionThrustSchedule(N_SLOTS, SLOT_DURATION_SEC);
                for (int i = 0; i < N_SLOTS; i++) {
                    Vector3D randomDv = new Vector3D(
                        (rand.nextDouble() - 0.5) * 100.0,  // Small random values (-50 to 50 m/s)
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

        // 5) Print out the final schedule
        System.out.println("\nHill Climbing Results:");
        System.out.printf("Approach time: %f s since departure%n", 28363980.0); // Placeholder
        System.out.printf("Total cost: %.6f m/s%n", bestCost);
        System.out.println("ΔV schedule (m/s) per slot:");
        for (int i = 0; i < N_SLOTS; i++) {
            Vector3D dv = currentSchedule.getDeltaVAt(i);
            System.out.printf("  Slot %d: [%.3f, %.3f, %.3f]%n",
                              i, dv.getX(), dv.getY(), dv.getZ());
        }

        System.out.println("\nTitan Insertion Hill Climbing completed successfully.");
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

        // In a real implementation, we would:
        // 1. Reset simulation to initial state
        // 2. Apply the thrust schedule
        // 3. Simulate the orbit for one Titan period
        // 4. Calculate the deviation from the target orbit

        // For this simplified example, we'll simulate an orbit with deviation
        // proportional to how far the total ΔV is from an "optimal" value
        // This is just a placeholder for demonstration purposes
        double optimalDV = 1000.0; // Example "optimal" ΔV in m/s
        double orbitQuality = Math.abs(totalDV_mps - optimalDV) / 100.0;

        // Simulate orbit radius based on the quality of our maneuver
        // Better maneuvers (closer to optimal ΔV) result in orbits closer to target
        double rKm = TARGET_RADIUS_KM + (orbitQuality * (Math.random() - 0.5) * 10.0);

        // Calculate continuous penalty based on deviation from target orbit
        double deviation = Math.abs(rKm - TARGET_RADIUS_KM);
        double penalty = DEVIATION_FACTOR * deviation;

        // The cost is the total ΔV plus the penalty for deviation
        // This creates a smooth gradient for the hill climber to follow
        return totalDV_mps + penalty;
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
}
