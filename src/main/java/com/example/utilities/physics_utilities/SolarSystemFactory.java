package com.example.utilities.physics_utilities;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to load a predefined solar system with real celestial bodies and their
 * approximate positions and velocities at a given time.
 */
public class SolarSystemFactory {

    private SolarSystemFactory() {
        // Prevent instantiation
    }

    /**
     * Loads a list of celestial bodies representing a simplified solar system snapshot.
     * @return a list of initialized CelestialBody objects
     */
    public static List<CelestialBody> loadFromTable() {
        List<CelestialBody> system = new ArrayList<>();

        system.add(new CelestialBody(
                "Sun",
                1.99E30, // mass in kg
                new Vector3D(0.0, 0.0, 0.0), // position in km
                new Vector3D(0.0, 0.0, 0.0)  // velocity in km/s
        ));

        system.add(new CelestialBody(
                "Mercury",
                3.30E23, // mass in kg
                new Vector3D(-5.67E7, -3.23E7, 2.58E6),  // position in km
                new Vector3D(13.9, -40.3, -4.57)          // velocity in km/s
        ));

        system.add(new CelestialBody(
                "Venus",
                4.87E24, // mass in kg
                new Vector3D(-1.04E8, -3.19E7, 5.55E6),  // position in km
                new Vector3D(9.89, -33.7, -1.03)         // velocity in km/s
        ));

        system.add(new CelestialBody(
                "Earth",
                5.97E24, // mass in kg
                new Vector3D(-1.47E8, -2.97E7, 2.75E4),  // position in km
                new Vector3D(5.31, -29.3, 6.69E-4)       // velocity in km/s
        ));

        system.add(new CelestialBody(
                "Moon",
                7.35E22, // mass in kg
                new Vector3D(-1.47E8, -2.95E7, 5.29E4),  // position in km
                new Vector3D(4.53, -28.6, 6.73E-2)       // velocity in km/s
        ));
        system.add(new CelestialBody(
                "Mars",
                6.42E23, // mass in kg
                new Vector3D(-2.15E8, 1.27E8, 7.94E6),  // position in km
                new Vector3D(-11.5, -18.7, -0.111)       // velocity in km/s
        ));



        system.add(new CelestialBody(
                "Jupiter",
                1.90E27, // mass in kg
                new Vector3D(5.54E7, 7.62E8, -4.40E6),  // position in km
                new Vector3D(-13.2, 12.9, 0.0522)       // velocity in km/s
        ));

        system.add(new CelestialBody(
                "Saturn",
                5.68E26, // mass in kg
                new Vector3D(1.42E9, -1.91E8, -5.33E7), // position in km
                new Vector3D(0.748, 9.55, -0.196)       // velocity in km/s
        ));

        system.add(new CelestialBody(
                "Uranus",
                8.68E25, // mass in kg
                new Vector3D(1.62E9, 2.43E9, -1.19E7),  // position in km
                new Vector3D(-5.72, 3.45, 0.087)         // velocity in km/s
        ));

        system.add(new CelestialBody(
                "Neptune",
                1.02E26, // mass in kg
                new Vector3D(4.47E9, -5.31E7, -1.02E8), // position in km
                new Vector3D(0.0287, 5.47, -0.113)      // velocity in km/s
        ));


        system.add(new CelestialBody(
                "Titan",
                1.35E23, // mass in kg
                new Vector3D(1.42E9, -1.92E8, -5.28E7), // position in km
                new Vector3D(5.95, 7.68, 0.254)         // velocity in km/s
        ));
         // * Uses a combination between GA best trajectory found and NASA values.
        system.add(new CelestialBody("Noah's ark", 2.1E5,  // mass ~210,000 kg
                new Vector3D(-1.4740509568994185E8, -2.9725582352311186E7, 27287.406431384097),
                new Vector3D(53.87608593069663, -23.73795647814695, 20.476394276366552)));
        // 7.66 km/s relative to Earth (LEO)

        return system;
    }
}

