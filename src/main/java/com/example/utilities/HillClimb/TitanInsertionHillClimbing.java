package com.example.utilities.HillClimb;

import java.util.*;
import java.io.IOException;
import java.util.function.BiFunction;

import com.example.utilities.GA.Individual;
import com.example.utilities.SimulationFileWriter;
import com.example.utilities.Vector3D;
import com.example.Constants;
import com.example.utilities.solvers.RKF45Solver;

public class TitanInsertionHillClimbing {

    /** Number of discrete thrust‐slots in the insertion profile **/
    private static final int N_SLOTS = 5;

    /** Duration of each slot in seconds, one day here **/
    private static final double SLOT_DURATION_SEC = 86400.0; 


    /** How many hill‐climb iterations to attempt **/
    private static final int MAX_ITERATIONS = 250000;

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
            // Use the SimulationFileWriter to write the results
            SimulationFileWriter.writeHillClimbResults(
                currentSchedule,
                bestCost,
                "src/main/java/com/example/utilities/HillClimb/hillclimb_results.json"
            );

            System.out.println("Results saved to hillclimb_results.json");
        } catch (Exception e) {
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
        double totalDV_mps = schedule.getTotalDeltaVMagnitude();
        double totalFuel = schedule.getTotalFuelUsed();

        Vector3D initialPosition = new Vector3D(
            TARGET_RADIUS_KM * 2, // Start at twice the target radius away from Titan
            0,
            0
        );
        Vector3D initialVelocity = new Vector3D(
            0,
            Math.sqrt(Constants.MU_TITAN / (TARGET_RADIUS_KM * 2)),
            0
        );

        Vector3D position = initialPosition;
        Vector3D velocity = initialVelocity;

        RKF45Solver solver = new RKF45Solver();

        BiFunction<Double, double[], double[]> gravity = (t, y) -> {
            // y[0..2]=position km, y[3..5]=velocity km/s
            Vector3D r    = new Vector3D(y[0], y[1], y[2]);
            double  rMag  = r.magnitude();
            double  aMag  = -Constants.MU_TITAN / (rMag * rMag);
            Vector3D aVec = r.normalize().scale(aMag);
            return new double[]{
                    y[3],        // dx/dt = vx
                    y[4],        // dy/dt = vy
                    y[5],        // dz/dt = vz
                    aVec.getX(), // dvx/dt = ax
                    aVec.getY(), // dvy/dt = ay
                    aVec.getZ()  // dvz/dt = az
            };
        };

        for (int i = 0; i < schedule.getNumSlots(); i++) {
            Vector3D deltaV    = schedule.getDeltaVAt(i);
            Vector3D deltaVkms = new Vector3D(
                    deltaV.getX() / 1000.0,
                    deltaV.getY() / 1000.0,
                    deltaV.getZ() / 1000.0
            );
            velocity = velocity.add(deltaVkms);


            double duration = schedule.getSlotDuration();
            double initialStep  = 60.0;
            int maxSteps = (int) Math.ceil(duration / initialStep) + 1;

            // pack state [x,y,z,vx,vy,vz]
            double[] y0 = new double[]{
                    position.getX(), position.getY(), position.getZ(),
                    velocity.getX(), velocity.getY(), velocity.getZ()
            };

            double[][] traj = solver.solve(
                    gravity,
                    0.0,
                    y0,
                    initialStep,
                    maxSteps,
                    null
            );

            // get final row
            double[] yEnd = traj[traj.length - 1];
            position = new Vector3D(yEnd[0], yEnd[1], yEnd[2]);
            velocity = new Vector3D(yEnd[3], yEnd[4], yEnd[5]);
        }
        double titanOrbitalPeriod = calculateTitanOrbitalPeriod();
        double timeStep = 60.0;

        for (double t = 0; t < titanOrbitalPeriod; t += timeStep) {
            double distanceToTitan = position.magnitude();
            double acceleration = -Constants.MU_TITAN / (distanceToTitan * distanceToTitan);

            Vector3D accelerationVector = position.normalize().scale(acceleration);

            velocity = velocity.add(accelerationVector.scale(timeStep));
            position = position.add(velocity.scale(timeStep));
        }

        double finalRadius = position.magnitude();
        double deviation = Math.abs(finalRadius - TARGET_RADIUS_KM);


        Vector3D angularMomentum = position.cross(velocity);
        double mu = Constants.MU_TITAN;
        Vector3D eccentricityVector = velocity.cross(angularMomentum).scale(1.0/mu).subtract(position.normalize());
        double eccentricity = eccentricityVector.magnitude();

        double eccentricityPenalty = DEVIATION_FACTOR * eccentricity * 10.0;

        double radiusPenalty = DEVIATION_FACTOR * deviation;

        // The cost is the total DV plus the penalties
        // This creates a smooth gradient for the hill climber to follow
        return 0.8*totalDV_mps + radiusPenalty + eccentricityPenalty + totalFuel;
    }


    /**
     * Container for returning the hill‐climb result:
     *   *schedule = best InsertionThrustSchedule
     *   *approachTimeSec= time of closest Titan approach (s since departure)
     *   *cost= final cost
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
     * Computes the period of a circular orbit around Titan at the target altitude.
     * Uses the standard Newton–Kepler formulation for a circular orbit
     * 
     * @return Titan's orbital period in seconds
     */
    private static double calculateTitanOrbitalPeriod() {
        // Based on MyTitanSimulator.getTitanOrbitalPeriod()
        double r = TITAN_RADIUS_KM + TARGET_ALTITUDE_KM;
        double mu = Constants.MU_TITAN;
        return 2.0 * Math.PI * Math.sqrt(r * r * r / mu);
    }

}
