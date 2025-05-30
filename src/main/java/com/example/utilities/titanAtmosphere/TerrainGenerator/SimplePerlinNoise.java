package com.example.utilities.titanAtmosphere.TerrainGenerator;

import java.util.Random;

/**
 * SimplePerlinNoise generates smooth 2D Perlin noise values in the range [-1, 1].
 *
 * It is used to simulate realistic natural phenomena such as terrain elevation
 * and atmospheric wind variation. The output is continuous, pseudo-random, and spatially smooth.
 *
 * How it works (theory):
 *
 * Divide the plane into a grid of unit squares.
 * For a given point (x, y), determine its containing grid cell (xi, yi).
 * At each corner of the cell, assign a pseudo-random gradient vector G.
 * Compute dot products of each corner's gradient with the vector from corner to (x, y).
 * Interpolate these values using a smoothstep "fade" function:
 *The result is deterministic (based on a seed), spatially smooth, and ideal for terrain/wind modeling.
 * https://www.youtube.com/watch?v=IKB1hWWedMk
 */
public class SimplePerlinNoise {

    /** Permutation table used to select gradient vectors. */
    private final int[] permutation;

    /** Random number generator seeded for reproducibility. */
    private final Random random;

    /**
     * Constructs a Perlin noise generator with a given seed.
     *
     * @param seed the seed used for deterministic noise generation
     */
    public SimplePerlinNoise(int seed) {
        this.random = new Random(seed);
        this.permutation = new int[512];
        int[] p = new int[256];

        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        // Fisher-Yates shuffle to randomize the gradient indices
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }

        // Duplicate array to simplify lookup (avoids overflow)
        for (int i = 0; i < 512; i++) {
            permutation[i] = p[i % 256];
        }
    }

    /**
     * Computes the Perlin noise value at a given 2D coordinate.
     *
     * @param x horizontal position (real number)
     * @param y vertical position (real number)
     * @return noise value in the range [-1, 1], smooth in space
     */
    public double noise(double x, double y) {
        // Integer grid cell coordinates
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;

        // Fractional position within the cell
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        // Fade (ease) functions
        double u = fade(xf);
        double v = fade(yf);

        // Hash coordinates of the four corners
        int aa = permutation[permutation[xi] + yi];
        int ab = permutation[permutation[xi] + yi + 1];
        int ba = permutation[permutation[xi + 1] + yi];
        int bb = permutation[permutation[xi + 1] + yi + 1];

        // Compute dot products at corners
        double x1 = lerp(u, grad(aa, xf, yf), grad(ba, xf - 1, yf));
        double x2 = lerp(u, grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1));

        // Final bilinear interpolation
        return lerp(v, x1, x2);
    }

    /**
     * Fade function: smooths the interpolation factor t.
     *
     * Formula: 6t^5 - 15t^4 + 10t^3
     * Ensures smooth transitions between grid points (C1 continuity).
     *
     * @param t interpolation factor
     * @return smoothed factor
     */
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10); //could be any
    }

    /**
     * Performs linear interpolation between two values a and b using factor t.
     *
     * @param t blend factor [0, 1]
     * @param a start value
     * @param b end value
     * @return interpolated result
     */
    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    /**
     * Computes the gradient value based on a hash and position delta.
     * Simulates dot product between pseudo-random unit vector and distance vector.
     *
     * @param hash hash value used to pick direction
     * @param x    delta x from grid point
     * @param y    delta y from grid point
     * @return dot product of gradient and offset
     */
    private double grad(int hash, double x, double y) {

        int h = hash % 8;

        double u, v;

        if (h < 4) {
            u = x;
            v = y;
        } else {
            u = y;
            v = x;
        }

        if ((h & 1) == 0) {

        } else {
            u = -u;
        }

        if ((h & 2) == 0) {

        } else {
            v = -v;
        }

        return u + v;
    }

}
