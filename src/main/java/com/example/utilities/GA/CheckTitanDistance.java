package com.example.utilities.GA;

import java.util.List;

/**
 * Simple standalone program to check if any individual in best_individuals.json
 * came within Titan's sphere of influence.
 */
public class CheckTitanDistance {
    public static void main(String[] args) {
        System.out.println("Checking if any individual came within Titan's sphere of influence...");
        
        try {
            List<GAResultsParser.Individual> all = GAResultsParser.loadAllIndividuals("src/main/resources/best_individuals.json");
            System.out.println("Found " + all.size() + " individuals in best_individuals.json");
            
            for (int i = 0; i < Math.min(5, all.size()); i++) {
                GAResultsParser.Individual ind = all.get(i);
                System.out.printf("#%d: minDistToTitan = %.3e km%n", i, ind.minDistanceToTitan());
            }
            boolean foundViableIntercept = false;
            for (GAResultsParser.Individual ind : all) {
                if (ind.minDistanceToTitan() < 1e7) {
                    foundViableIntercept = true;
                    break;
                }
            }
            if (foundViableIntercept) {
                System.out.println("At least one individual came within 1e7 km of Titan!");
            } else {
                System.out.println("No individual came within 1e7 km of Titan. The GA never produced a viable Titan intercept.");
                System.out.println("Fix your GA or use a different seed.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}