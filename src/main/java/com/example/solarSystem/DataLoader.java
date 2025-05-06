package com.example.solarSystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * DataLoader reads celestial body data from a CSV file.
 * It returns a list of CelestialBody objects containing names, mass, position, and velocity.
 */
public class DataLoader {

    /**
     * Loads celestial bodies from a semicolon-delimited CSV file.
     * Expects the columns: name, x, y, z, vx, vy, vz, mass.
     */
    public static List<CelestialBody> loadBodiesFromCSV(String filePath) {
        List<CelestialBody> bodies = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // skip headers

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines

                String[] tokens = line.split(";");
                if (tokens.length < 8) {

                    continue;
                }

                try {
                    String name = tokens[0].trim();

                    double x = parse(tokens[1]);
                    double y = parse(tokens[2]);
                    double z = parse(tokens[3]);

                    double vx = parse(tokens[4]);
                    double vy = parse(tokens[5]);
                    double vz = parse(tokens[6]);

                    double mass = parse(tokens[7]);

                    Vector3D position = new Vector3D(x, y, z);
                    Vector3D velocity = new Vector3D(vx, vy, vz);
                    CelestialBody body = new CelestialBody(name, mass, position, velocity);
                    bodies.add(body);

                } catch (NumberFormatException e) {
                    System.err.println("Number parsing error in line: " + line);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to load CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return bodies;
    }

    private static double parse(String s) {
        return Double.parseDouble(s.replace(",", ".").replaceAll("[^\\dEe+\\-\\.]", ""));
    }
}
