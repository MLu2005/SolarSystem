package com.example.solarSystem.GA;

import executables.solvers.Constants;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

class Generation {
    private final Individual[] individuals;
    private static final Random RNG = Constants.RNG;
    private Generation(int size){ individuals = new Individual[size]; }

    public static Generation randomPopulation(int size) {
        Generation g = new Generation(size);

        for (int i = 0; i < size; i++) {
            g.individuals[i] = new Individual();
            g.individuals[i].evaluate();
        }

        g.sort();
        return g;
    }

    public Individual best(int n) { return individuals[n]; }

    public Generation evolve(int mutationRatePercent, int eliteCount) {
        Generation next = new Generation(individuals.length);
        System.arraycopy(individuals, 0, next.individuals, 0, eliteCount);

        for (int i = eliteCount; i < individuals.length; i++) {
            Individual p1 = selectParent();
            Individual p2 = selectParent();
            next.individuals[i] = Individual.crossover(p1, p2);
        }

        for (int i = eliteCount; i < individuals.length; i++) {
            if (RNG.nextInt(100) < mutationRatePercent) {
                next.individuals[i] = next.individuals[i].mutate();
            }
        }
        for (int i = eliteCount; i < individuals.length; i++) {
            next.individuals[i].evaluate();
        }
        next.sort();
        return next;
    }

    private Individual selectParent() {
        Individual best = null;
        for (int k = 0; k < 5; k++) {
            Individual cand = individuals[RNG.nextInt(individuals.length)];
            if (best == null || cand.getFitness() > best.getFitness()) best = cand;
        }
        return best;
    }
    private void sort() {
        Arrays.sort(individuals, Comparator.comparingDouble(Individual::getFitness).reversed());
    }
}