package com.example.utilities.GA;

import com.example.solar_system.CelestialBody;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.Vector3D;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A pretty coarse GA to reach titan, may have to later implement a finer one for more precise operations using rkf45
 */
public class GeneticTitan {

    private static final Vector3D EARTH_VELOCITY;

    // using the same logic al always when retrieving from the table
    static {CelestialBody earth = SolarSystemFactory.loadFromTable()
                .stream()
                .filter(b -> b.getName().equals("Earth"))
                .findFirst()
                .orElseThrow();
        EARTH_VELOCITY = earth.getVelocity();
    }
    // deltav = |vLaunch – vEarth|  (km s⁻¹).
    public static double computeDvRel(double vx, double vy, double vz) {
        Vector3D vLaunch = new Vector3D(vx, vy, vz);
        return vLaunch.subtract(EARTH_VELOCITY).magnitude();
    }

    public static void main(String[] args) {
        final int POP_SIZE  = 800;
        final int GENERATIONS = 600;
        final int ELITES= 18;
        final int MUTATION_RATE =70;
        final double TARGET_KM=2575;

        System.out.println("Starting the GA, debug successful:");

        Generation pop = Generation.randomPopulation(POP_SIZE);
        int gen = 0;

        while (gen < GENERATIONS && pop.best(0).getMinDistanceKm() > TARGET_KM) {
            pop = pop.evolve(MUTATION_RATE, ELITES);
            gen++;

            Individual best = pop.best(0);
            System.out.printf("Gen %03d  fitness %.6f  dTitan %.1f km%n",
                    gen,
                    best.getFitness(),
                    best.getMinDistanceKm());
        }

        Individual winner_winner_chicken_dinner = pop.best(0);
        List<Double> g = winner_winner_chicken_dinner.genes();

        double x  = g.get(0), y  = g.get(1), z  = g.get(2);   // km
        double vx = g.get(3), vy = g.get(4), vz = g.get(5);   // km s⁻¹


        System.out.println("\n=== BEST INDIVIDUAL ===");
        System.out.printf(
                "  Fitness score .............. %10.6f%n" +
                        "  Titan miss distance ........ %10.3f km%n" +
                        "  Launch position  %n" +
                        "    x = % .6e%n" +
                        "    y = % .6e%n" +
                        "    z = % .6e%n" +
                        "  Launch velocity  (km/s)%n" +
                        "    vx = % .6f%n" +
                        "    vy = % .6f%n" +
                        "    vz = % .6f%n", +
                winner_winner_chicken_dinner.getFitness(),
                winner_winner_chicken_dinner.getMinDistanceKm(),
                x, y, z,
                vx, vy, vz
        );
        double dvRel = computeDvRel(vx, vy, vz);
        System.out.printf("%n  Launch velocity relative to Earth ..... %6.2f km/s%n", dvRel);
        System.out.println("(The launch mass is constant as given by the Manual! 50k kg)");
        pop.sort();
        writeToFile(pop, ELITES);
    }


    public static void writeToFile(Generation pop, int top_n) {
        List<Individual> best = new ArrayList<>();
        for (int i = 0; i < top_n; i++) {
            best.add(pop.best(i));
        }

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"best_individuals\": [\n");

        for (int i = 0; i < best.size(); i++) {
            Individual ind = best.get(i);
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

            double dvRel = computeDvRel(genes.get(3), genes.get(4), genes.get(5));
            jsonBuilder.append("      \"delta_v_relative_to_earth\": ").append(dvRel).append("\n");

            jsonBuilder.append("    }");
            if (i < best.size() - 1) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }

        jsonBuilder.append("  ]\n");
        jsonBuilder.append("}\n");

        try (FileWriter file = new FileWriter("src/main/java/com/example/utilities/GA/best_individuals.json")) {
            file.write(jsonBuilder.toString());
            System.out.println("Results written to best_individuals.json");
        } catch (IOException e) {
            System.out.println("Error writing results to JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
