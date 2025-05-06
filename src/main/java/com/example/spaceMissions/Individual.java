package com.example.spaceMissions;

public class Individual {
    public double[] genes;   // [x, y, z, vx, vy, vz]
    public double fitness;

    public Individual(double[] genes) {
        this.genes = genes;
        this.fitness = Double.MAX_VALUE;
    }
}
