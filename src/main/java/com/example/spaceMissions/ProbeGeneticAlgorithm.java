package com.example.spaceMissions;

import executables.solvers.RK4Solver;
import com.example.solarSystem.*;
import com.example.solarSystem.StateUtils;

import java.util.*;
import java.util.function.BiFunction;

public class ProbeGeneticAlgorithm {

    private static final int populationSize = 100;
    private static final int generations = 2000;
    private static final double mutationRate = 0.6;

    static final double maxSpeed = 60;
    static final double earthRadius = 6370;
    static final double maxTime = 3 * 365 * 86400; // 3 lata
    static final double stepSize = 3600.0; // 1h

    static Random random = new Random();

    public static Individual runGA(List<CelestialBody> bodies) {
        List<Individual> population = initializePopulation();

        for (int generation = 0; generation < generations; generation++) {
            evaluateFitness(population, bodies);
            population.sort(Comparator.comparingDouble(ind -> ind.fitness));
            Individual best = population.get(0);

            if (best.fitness < 5e7) {
                System.out.println("\u2705 Found close approach to Titan!");
                break;
            }

            double[] bestGenes = best.genes;
            System.out.printf("Gen %4d | \uD83D\uDE80 v = (%.2f, %.2f, %.2f) | \uD83D\uDCCF Distance = %.2e km\n",
                    generation, bestGenes[3], bestGenes[4], bestGenes[5], best.fitness);

            List<Individual> newPopulation = new ArrayList<>();
            for (int i = 0; i < 5; i++) newPopulation.add(population.get(i));

            while (newPopulation.size() < populationSize) {
                Individual parent1 = getParent(population);
                Individual parent2 = getParent(population);
                Individual child = crossover(parent1, parent2);
                mutate(child);
                newPopulation.add(child);
            }

            population = newPopulation;
        }

        evaluateFitness(population, bodies);
        population.sort(Comparator.comparingDouble(ind -> ind.fitness));
        return population.get(0);
    }

    public static List<Individual> initializePopulation() {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            double[] genes = new double[6];
            for (int j = 0; j < 3; j++) {
                genes[j] = (random.nextDouble() - 0.5) * 2 * earthRadius;
            }
            for (int j = 3; j < 6; j++) {
                genes[j] = (random.nextDouble() - 0.5) * 2 * maxSpeed;
            }
            population.add(new Individual(genes));
        }
        return population;
    }

    public static double simulate(Individual ind, List<CelestialBody> originalBodies) {
        List<CelestialBody> bodies = new ArrayList<>();

        for (CelestialBody b : originalBodies) {
            CelestialBody copy = new CelestialBody(
                    b.getName(),
                    b.getMass(),
                    new Vector3D(b.getPosition().getX(), b.getPosition().getY(), b.getPosition().getZ()),
                    new Vector3D(b.getVelocity().getX(), b.getVelocity().getY(), b.getVelocity().getZ())
            );
            bodies.add(copy);
        }


        CelestialBody earth = bodies.stream().filter(b -> b.getName().equals("Earth")).findFirst().get();
        CelestialBody rocket = bodies.stream().filter(b -> b.getName().equals("Rocket")).findFirst().get();
        CelestialBody titan = bodies.stream().filter(b -> b.getName().equals("Titan")).findFirst().get();

        Vector3D offset = new Vector3D(ind.genes[0], ind.genes[1], ind.genes[2]).normalize().scale(earthRadius);
        Vector3D launchPos = earth.getPosition().add(offset);
        Vector3D launchVel = earth.getVelocity().add(new Vector3D(ind.genes[3], ind.genes[4], ind.genes[5]));

        rocket.setPosition(launchPos);
        rocket.setVelocity(launchVel);

        BiFunction<Double, double[], double[]> ode = SolarSystemODE.generateODE(bodies);
        double[] y0 = StateUtils.extractStateVector(bodies);
        int steps = (int) (maxTime / stepSize);

        RK4Solver solver = new RK4Solver();
        double[][] solution = solver.solve(ode, 0.0, y0, stepSize, steps, null);
        StateUtils.applyStateVector(solution[solution.length - 1], bodies);

        return rocket.getPosition().distanceTo(titan.getPosition());
    }

    public static void evaluateFitness(List<Individual> population, List<CelestialBody> bodies) {
        for (Individual ind : population) {
            ind.fitness = simulate(ind, bodies);
        }
    }

    public static Individual getParent(List<Individual> pop) {
        Individual best = pop.get(random.nextInt(20));
        for (int i = 0; i < 2; i++) {
            Individual contender = pop.get(random.nextInt(20));
            if (contender.fitness < best.fitness) best = contender;
        }
        return best;
    }

    public static Individual crossover(Individual p1, Individual p2) {
        double[] newGenes = new double[6];
        for (int i = 0; i < 6; i++) {
            newGenes[i] = random.nextBoolean() ? p1.genes[i] : p2.genes[i];
        }
        return new Individual(newGenes);
    }

    public static void mutate(Individual ind) {
        for (int i = 0; i < 6; i++) {
            if (random.nextDouble() < mutationRate) {
                double range = (i < 3) ? earthRadius : maxSpeed;
                ind.genes[i] += (random.nextDouble() - 0.5) * 0.1 * range;
            }
        }
    }

    public static void main(String[] args) {
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV("src/main/java/com/example/solarSystem/IC.csv");
        if (bodies == null || bodies.isEmpty()) {
            System.err.println("\u274C Failed to load solar system bodies.");
            return;
        }

        Individual best = runGA(bodies);
        System.out.printf("\n\u2728 Best trajectory:\nOffset = (%.2f, %.2f, %.2f) km\nVelocity = (%.2f, %.2f, %.2f) km/s\nDistance to Titan = %.2f km\n",
                best.genes[0], best.genes[1], best.genes[2],
                best.genes[3], best.genes[4], best.genes[5],
                best.fitness);
    }
}
