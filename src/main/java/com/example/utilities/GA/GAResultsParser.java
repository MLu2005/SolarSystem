package com.example.utilities.GA;

import com.example.utilities.Vector3D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse the results from Genetic Algorithm optimization.
 * This class reads the best_individuals.json file and extracts key parameters
 * like launch position, velocity, and minimum distance to Titan.
 */
public class GAResultsParser {
    private final String filePath;
    private static List<Individual> individuals;

    /**
     * Constructor that takes the path to the best_individuals.json file.
     *
     * @param filePath Path to the best_individuals.json file
     */
    public GAResultsParser(String filePath) {
        this.filePath = filePath;
        this.individuals = new ArrayList<>();
        loadJsonFile();
    }



    /**
     * Gets all individuals parsed from the GA results file.
     *
     * @return List of all individuals
     */
    public static List<Individual> getIndividuals() {
        return new ArrayList<>(individuals); // Return a copy to prevent modification
    }

    /**
     * Loads the JSON file containing the best individuals from GA.
     */
    private void loadJsonFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            parseIndividuals(content.toString());
        } catch (IOException e) {
            System.err.println("Error loading GA results file: " + e.getMessage());
            throw new RuntimeException("Failed to load GA results", e);
        }
    }

    /**
     * Parses the JSON content to extract individual data.
     *
     * @param jsonContent The JSON content as a string
     */
    private void parseIndividuals(String jsonContent) {
        // Extract the array of individuals
        Pattern individualPattern = Pattern.compile("\\{\\s*\"rank\":\\s*(\\d+),\\s*\"fitness\":\\s*([\\d.]+),\\s*\"min_distance_to_titan_km\":\\s*([\\d.]+),\\s*\"launch_position\":\\s*\\{\\s*\"x\":\\s*([\\d.E-]+),\\s*\"y\":\\s*([\\d.E-]+),\\s*\"z\":\\s*([\\d.E-]+)\\s*\\},\\s*\"launch_velocity\":\\s*\\{\\s*\"vx\":\\s*([\\d.E-]+),\\s*\"vy\":\\s*([\\d.E-]+),\\s*\"vz\":\\s*([\\d.E-]+)\\s*\\},\\s*\"launch_mass\":\\s*([\\d.]+),\\s*\"delta_v_relative_to_earth\":\\s*([\\d.]+)\\s*\\}");

        Matcher matcher = individualPattern.matcher(jsonContent);

        while (matcher.find()) {
            int rank = Integer.parseInt(matcher.group(1));
            double fitness = Double.parseDouble(matcher.group(2));
            double minDistanceToTitan = Double.parseDouble(matcher.group(3));

            double posX = Double.parseDouble(matcher.group(4));
            double posY = Double.parseDouble(matcher.group(5));
            double posZ = Double.parseDouble(matcher.group(6));

            double velX = Double.parseDouble(matcher.group(7));
            double velY = Double.parseDouble(matcher.group(8));
            double velZ = Double.parseDouble(matcher.group(9));

            double launchMass = Double.parseDouble(matcher.group(10));
            double deltaV = Double.parseDouble(matcher.group(11));

            Vector3D position = new Vector3D(posX, posY, posZ);
            Vector3D velocity = new Vector3D(velX, velY, velZ);

            Individual individual = new Individual(
                rank, fitness, minDistanceToTitan, position, velocity, launchMass, deltaV
            );

            individuals.add(individual);
        }

        // Sort individuals by rank to ensure they're in the correct order
        individuals.sort((a, b) -> Integer.compare(a.rank(), b.rank()));
    }

    /**
     * Gets the best individual (rank 0) from the results.
     *
     * @return Individual with rank 0
     */
    public static Individual getBestIndividual() {
        if (individuals.isEmpty()) {
            throw new RuntimeException("No individuals found in GA results");
        }

        // Find the individual with rank 0
        for (Individual individual : individuals) {
            if (individual.rank() == 0) {
                Individual bestIndividual = individual;
                return bestIndividual;
            }
        }

        // If no rank 0 individual is found, return the first one
        return individuals.get(0);
    }

    /**
     * Gets the launch position of the best individual.
     *
     * @return Vector3D representing the launch position
     */
    public static Vector3D getLaunchPosition() {
        return getBestIndividual().launchPosition();
    }

    /**
     * Gets the launch velocity of the best individual.
     *
     * @return Vector3D representing the launch velocity
     */
    public static Vector3D getLaunchVelocity() {
        return getBestIndividual().launchVelocity();
    }

    /**
     * Gets the minimum distance to Titan achieved by the best individual.
     *
     * @return Minimum distance to Titan in kilometers
     */
    public double getMinDistanceToTitan() {
        return getBestIndividual().minDistanceToTitan();
    }

    /**
     * Gets the fitness value of the best individual.
     *
     * @return Fitness value
     */
    public double getFitness() {
        return getBestIndividual().fitness();
    }

    /**
     * Gets the launch mass of the best individual.
     *
     * @return Launch mass in kilograms
     */
    public double getLaunchMass() {
        return getBestIndividual().launchMass();
    }

    /**
     * Gets the delta-v relative to Earth for the best individual.
     *
     * @return Delta-v in km/s
     */
    public double getDeltaVRelativeToEarth() {
        return getBestIndividual().deltaVRelativeToEarth();
    }

    /**
     * Static method to load all individuals from a JSON file.
     *
     * @param filename Path to the JSON file containing individuals
     * @return List of all individuals from the file
     */
    public static List<Individual> loadAllIndividuals(String filename) {
        GAResultsParser parser = new GAResultsParser(filename);
        return parser.getIndividuals();
    }

    /**
     * Static method to load the best individual from a JSON file.
     *
     * @param filename Path to the JSON file containing individuals
     * @return The best individual from the file
     */
    public static Individual loadBestIndividual(String filename) {
        List<Individual> list = loadAllIndividuals(filename); // existing method
        if (list.isEmpty()) {
            throw new IllegalStateException("No individuals found in JSON");
        }
        // Assume list is sorted by fitness ascending or descending—pick the best
        return list.get(0);
    }

    /**
     * Main method to inspect the top individuals and their minimum distances to Titan.
     * This helps verify if any individual came within Titan's sphere of influence.
     */
    public static void main(String[] args) {
        List<Individual> all = GAResultsParser.loadAllIndividuals("src/main/resources/best_individuals.json");
        for (int i = 0; i < Math.min(5, all.size()); i++) {
            Individual ind = all.get(i);
            System.out.printf("#%d: minDistToTitan = %.3e km%n", i, ind.minDistanceToTitan());
        }
    }

    /**
         * Inner class to represent an individual from the GA results.
         */
        public record Individual(int rank, double fitness, double minDistanceToTitan, Vector3D launchPosition,
                                 Vector3D launchVelocity, double launchMass, double deltaVRelativeToEarth) {

        /**
         * Estimates the time of closest approach to Titan.
         * Since the actual time is not stored in the GA results, this method
         * provides a rough estimate based on the simulation length.
         *
         * @return Estimated time of closest approach in seconds
         */
            public double getTimeOfClosestApproach() {
                // Estimate the time of closest approach as a percentage of the simulation length
                // based on how close the spacecraft got to Titan
                if (minDistanceToTitan < 1e5) {
                    // If very close, assume it happened around 70% of the way through the simulation
                    return executables.Constants.SIM_LEN * 0.7;
                } else if (minDistanceToTitan < 1e6) {
                    // If moderately close, assume it happened around 60% of the way through
                    return executables.Constants.SIM_LEN * 0.6;
                } else {
                    // Otherwise, assume it happened around the middle of the simulation
                    return executables.Constants.SIM_LEN * 0.5;
                }
            }

            @Override
            public String toString() {
                return "Individual{" +
                        "rank=" + rank +
                        ", fitness=" + fitness +
                        ", minDistanceToTitan=" + minDistanceToTitan +
                        ", launchPosition=" + launchPosition +
                        ", launchVelocity=" + launchVelocity +
                        ", launchMass=" + launchMass +
                        ", deltaVRelativeToEarth=" + deltaVRelativeToEarth +
                        '}';
            }

        public Vector3D getLaunchPosition() {
                return launchPosition;
        }

    }
}
