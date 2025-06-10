package com.example.lander;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithmOptimizer {
    private static final int POPULATION_SIZE = 500;
    private static final int MAX_GENERATIONS = 200;
    private static final double MUTATION_RATE = 0.15;
    private static final double CROSSOVER_RATE = 0.85;
    private static final double MIN_VERTICAL = 1.0;
    private static final double MAX_VERTICAL = 40.0;
    private static final double MIN_HORIZONTAL = 5.0;
    private static final double MAX_HORIZONTAL = 60.0;
    private static final double[] INITIAL_STATE = {-2715.3163563925214, 300.0, 0.5807482731466309, -1.6690138988461283, 0.0, 0.0};
    private static final double TIME_STEP = 1.0;
    private static final int MAX_STEPS = 2000000;
    private static final double WIND_SPEED = 0.0001;
    private static final double LANDER_MASS = 50000.0;

    static class Individual {
        double verticalBrake;
        double horizontalBrake;
        double fitness = Double.MAX_VALUE;
        double[] landingState;

        Individual(double verticalBrake, double horizontalBrake) {
            this.verticalBrake = verticalBrake;
            this.horizontalBrake = horizontalBrake;
        }
    }

    public static void main(String[] args) {
        List<Individual> population = initializePopulation();
        Individual bestOverall = null;
        
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            evaluatePopulation(population);
            Collections.sort(population, Comparator.comparingDouble(ind -> ind.fitness));
            
            Individual bestInGen = population.get(0);
            if (bestOverall == null || bestInGen.fitness < bestOverall.fitness) {
                bestOverall = new Individual(bestInGen.verticalBrake, bestInGen.horizontalBrake);
                bestOverall.fitness = bestInGen.fitness;
                bestOverall.landingState = bestInGen.landingState;
            }
            
            printGenerationStats(generation, bestInGen);
            population = evolvePopulation(population);
        }
        
        printFinalResults(bestOverall);
    }

    private static void printGenerationStats(int generation, Individual best) {
        System.out.printf("\nGen %d: Fitness = %.6f%n", generation, best.fitness);
        System.out.printf("Vertical Brake: %.2f km, Horizontal Brake: %.2f km%n", 
                          best.verticalBrake, best.horizontalBrake);
        
        if (best.landingState != null) {
            System.out.println("Landing State:");
            System.out.printf("  X Position: %.4f km, Y Position: %.4f km%n", best.landingState[1], best.landingState[2]);
            System.out.printf("  X Velocity: %.4f km/s, Y Velocity: %.4f km/s%n", best.landingState[3], best.landingState[4]);
            System.out.printf("  Tilt: %.4f rad, Tilt Rate: %.4f rad/s%n", best.landingState[5], best.landingState[6]);
        }
    }

    private static void printFinalResults(Individual best) {
        System.out.println("\n=== OPTIMAL PARAMETERS ===");
        System.out.printf("Vertical Braking Altitude: %.2f km%n", best.verticalBrake);
        System.out.printf("Horizontal Braking Altitude: %.2f km%n", best.horizontalBrake);
        System.out.printf("Fitness Score: %.6f%n", best.fitness);
        
        System.out.println("\nLanding Performance:");
        System.out.printf("Position Error: %.4f km (X), %.4f km (Y)%n", 
                          Math.abs(best.landingState[1]), Math.abs(best.landingState[2]));
        System.out.printf("Velocity Error: %.6f km/s (X), %.6f km/s (Y)%n", 
                          Math.abs(best.landingState[3]), Math.abs(best.landingState[4]));
        System.out.printf("Attitude Error: %.4f rad (Tilt), %.4f rad/s (Tilt Rate)%n", 
                          Math.abs(best.landingState[5]), Math.abs(best.landingState[6]));
    }

    private static List<Individual> initializePopulation() {
        List<Individual> population = new ArrayList<>();
        Random rand = new Random();
        population.add(new Individual(3.5, 33.5));
        population.add(new Individual(10.0, 25.0));
        population.add(new Individual(5.0, 40.0));
        population.add(new Individual(15.0, 30.0));
        for (int i = population.size(); i < POPULATION_SIZE; i++) {
            double vertical = MIN_VERTICAL + rand.nextDouble() * (MAX_VERTICAL - MIN_VERTICAL);
            double horizontal = MIN_HORIZONTAL + rand.nextDouble() * (MAX_HORIZONTAL - MIN_HORIZONTAL);
            population.add(new Individual(vertical, horizontal));
        }
        return population;
    }

    private static void evaluatePopulation(List<Individual> population) {
        for (Individual ind : population) {
            if (ind.fitness == Double.MAX_VALUE) {
                evaluateIndividual(ind);
            }
        }
    }

    private static void evaluateIndividual(Individual ind) {
        Controller openLoop = new OpenLoopController(ind.verticalBrake, ind.horizontalBrake);
        Controller feedback = new FeedbackController();
        Controller combined = new CombinedController(openLoop, feedback);
        
        double[][] trajectory = LanderSimulator.simulateCombined(
            INITIAL_STATE.clone(), TIME_STEP, MAX_STEPS, WIND_SPEED, LANDER_MASS, combined
        );
        
        double[] finalState = trajectory[trajectory.length - 1];
        ind.landingState = finalState;
        
        double posX = finalState[1];
        double posY = finalState[2];
        double velX = finalState[3];
        double velY = finalState[4];
        double tilt = finalState[5];
        double tiltRate = finalState[6];
        
        if (posY > 0) {
            ind.fitness = 1e9 + (1000 * posY);
            return;
        }
        if (Math.abs(velY) > 0.005) {
            ind.fitness = 5e8 + (100000 * Math.abs(velY));
            return;
        }
        
        double positionError = Math.abs(posX) * 1000;
        double altitudeError = Math.abs(posY) * 1000;
        double velocityErrorX = Math.abs(velX) * 1000;
        double velocityErrorY = Math.abs(velY) * 1000;
        double tiltError = Math.abs(tilt);
        double tiltRateError = Math.abs(tiltRate);
        
        ind.fitness = 1.0 * positionError +
                      2.0 * altitudeError +
                      10.0 * velocityErrorX +
                      20.0 * velocityErrorY +
                      5.0 * Math.toDegrees(tiltError) +
                      2.0 * Math.toDegrees(tiltRateError);
    }

    private static List<Individual> evolvePopulation(List<Individual> population) {
        List<Individual> newPopulation = new ArrayList<>();
        Random rand = new Random();
        int eliteCount = (int) (POPULATION_SIZE * 0.1);
        newPopulation.addAll(population.subList(0, eliteCount));
        while (newPopulation.size() < POPULATION_SIZE) {
            Individual parent1 = selectParent(population, rand);
            Individual parent2 = selectParent(population, rand);
            
            if (rand.nextDouble() < CROSSOVER_RATE) {
                Individual[] children = crossover(parent1, parent2, rand);
                mutate(children[0], rand);
                mutate(children[1], rand);
                newPopulation.add(children[0]);
                if (newPopulation.size() < POPULATION_SIZE) {
                    newPopulation.add(children[1]);
                }
            } else {
                newPopulation.add(new Individual(parent1.verticalBrake, parent1.horizontalBrake));
            }
        }
        return newPopulation;
    }

    private static Individual selectParent(List<Individual> population, Random rand) {
        int tournamentSize = 5;
        Individual best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Individual candidate = population.get(rand.nextInt(population.size()));
            if (best == null || candidate.fitness < best.fitness) {
                best = candidate;
            }
        }
        return best;
    }

    private static Individual[] crossover(Individual p1, Individual p2, Random rand) {
        double alpha = 0.25;
        double vMin = Math.min(p1.verticalBrake, p2.verticalBrake);
        double vMax = Math.max(p1.verticalBrake, p2.verticalBrake);
        double vRange = vMax - vMin;
        double newV = vMin - alpha * vRange + rand.nextDouble() * (vRange * (1 + 2 * alpha));
        
        double hMin = Math.min(p1.horizontalBrake, p2.horizontalBrake);
        double hMax = Math.max(p1.horizontalBrake, p2.horizontalBrake);
        double hRange = hMax - hMin;
        double newH = hMin - alpha * hRange + rand.nextDouble() * (hRange * (1 + 2 * alpha));
        
        return new Individual[] {
            new Individual(clamp(newV, MIN_VERTICAL, MAX_VERTICAL), clamp(newH, MIN_HORIZONTAL, MAX_HORIZONTAL)),
            new Individual(clamp(newV, MIN_VERTICAL, MAX_VERTICAL), clamp(newH, MIN_HORIZONTAL, MAX_HORIZONTAL))
        };
    }

    private static void mutate(Individual ind, Random rand) {
        if (rand.nextDouble() < MUTATION_RATE) {
            ind.verticalBrake += rand.nextGaussian() * 0.1 * (MAX_VERTICAL - MIN_VERTICAL);
            ind.verticalBrake = clamp(ind.verticalBrake, MIN_VERTICAL, MAX_VERTICAL);
        }
        if (rand.nextDouble() < MUTATION_RATE) {
            ind.horizontalBrake += rand.nextGaussian() * 0.1 * (MAX_HORIZONTAL - MIN_HORIZONTAL);
            ind.horizontalBrake = clamp(ind.horizontalBrake, MIN_HORIZONTAL, MAX_HORIZONTAL);
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}