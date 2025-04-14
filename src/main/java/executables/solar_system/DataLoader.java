package executables.solar_system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public static List<CelestialBody> loadBodiesFromCSV(String filePath) {
        List<CelestialBody> bodies = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // pomiń nagłówki

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // pomiń puste linie

                String[] tokens = line.split(";");
                if (tokens.length < 8) {
                    System.err.println("⚠️ Pominięto wiersz zbyt krótki: " + line);
                    continue;
                }

                try {
                    String name = tokens[0].trim();

                    double x = Double.parseDouble(tokens[1].replace(",", "."));
                    double y = Double.parseDouble(tokens[2].replace(",", "."));
                    double z = Double.parseDouble(tokens[3].replace(",", "."));

                    double vx = Double.parseDouble(tokens[4].replace(",", "."));
                    double vy = Double.parseDouble(tokens[5].replace(",", "."));
                    double vz = Double.parseDouble(tokens[6].replace(",", "."));

                    double mass = Double.parseDouble(tokens[7].replace(",", "."));

                    Vector3D position = new Vector3D(x, y, z);
                    Vector3D velocity = new Vector3D(vx, vy, vz);
                    CelestialBody body = new CelestialBody(name, mass, position, velocity);
                    bodies.add(body);
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Błąd parsowania liczby w wierszu: " + line);
                }

            }

        } catch (Exception e) {
            System.err.println("❌ Błąd ładowania CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return bodies;
    }

}
