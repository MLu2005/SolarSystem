package com.example.utilities.physics_utilities;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to load a predefined solar system with real celestial bodies and their
 * approximate positions and velocities at a given time.
 *
 * Sources: https://nssdc.gsfc.nasa.gov/planetary/factsheet/
 *          https://science.nasa.gov/mars/facts/#h-orbit-and-rotation
 *
 */
public final class SolarSystemFactory {
    private SolarSystemFactory() { /* prevent instantiation */ }

    public static List<CelestialBody> loadFromTable() {
        List<CelestialBody> system = new ArrayList<>();

        // Sun - central reference frame
        CelestialBody sun = new CelestialBody("Sun", 1.9900000000E30,
                new Vector3D(0, 0, 0),
                new Vector3D(0, 0, 0));
        system.add(sun);


        CelestialBody mercury = new CelestialBody("Mercury", 3.3000000000E23,
                new Vector3D(-5.67E7, -3.23E7, 2.58E6),
                new Vector3D(22.3, -39.2, 0));
        system.add(mercury);


        CelestialBody venus = new CelestialBody("Venus", 4.8700000000E24,
                new Vector3D(-1.04E8, -3.19E7, 5.55E6),
                new Vector3D(10.2, -33.3, -1.03));
        system.add(venus);


        CelestialBody earth = new CelestialBody("Earth", 5.9700000000E24,
                new Vector3D(-1.4700000000E8, -2.9700000000E7, 2.7500000000E4),
                new Vector3D(5.88, -29.1, 6.69E-4));
        system.add(earth);

        // * The following part is to perfectly update moon with earth relative velocity and distance.
        double moonDistance = 3.844e5;   // km
        double moonOrbitalSpeed = 1.022; // km/s
        Vector3D moonPos = new Vector3D(
                earth.getPosition().x - moonDistance,
                earth.getPosition().y,
                earth.getPosition().z
        );
        Vector3D moonVel = new Vector3D(
                earth.getVelocity().x,
                earth.getVelocity().y + moonOrbitalSpeed,
                earth.getVelocity().z
        );
        CelestialBody moon = new CelestialBody("Moon", 7.35e22, moonPos, moonVel);
        system.add(moon);

        CelestialBody mars = new CelestialBody(
                        "Mars",
                        6.42E23, // mass in kg
                new Vector3D(-2.15E8, 1.27E8, 7.94E6),  // position in km
                new Vector3D(-11.5, -18.7, -0.111)       // velocity in km/s
        );
        system.add(mars);

        CelestialBody jupiter = new CelestialBody(
                "Jupiter",
                1.89813e27, // mass in kg
                new Vector3D(7.7857e8, 1.4396e8, -1.2481e7), // position in km
                new Vector3D(-2.556, 12.120, 0.087)); // velocity in km/s
        system.add(jupiter);



        CelestialBody saturn = new CelestialBody(
                "Saturn",
                5.6834e26, // Mass in kilograms (NASA value)
                new Vector3D(1.3792534e9, -5.1340122e8, -3.2942753e7), // Position in kilometers (J2000 heliocentric ecliptic)
                new Vector3D(3.689544, 7.476999, -0.212132) // Velocity in km/s
        );
        system.add(saturn);

        CelestialBody titan = getCelestialBody(saturn);
        system.add(titan);

        CelestialBody uranus = new CelestialBody(
                "Uranus",
                8.6810e25,
                new Vector3D(2.5503986e9, -1.5802832e9, -2.5871355e7),
                new Vector3D(3.017182, 5.474592, -0.064976)
        );
        system.add(uranus);

        CelestialBody neptune = new CelestialBody(
                "Neptune",
                1.02413e26,
                new Vector3D(4.4606127e9, -1.9791365e8, -9.4281056e7),
                new Vector3D(0.537145, 5.362865, -0.133276)
        );
        system.add(neptune);


        // Spaceship / Noah's Ark (updated the best trajectory data)
        system.add(new CelestialBody("Noah's ark", 50000.0,
                new Vector3D(-1.4699392692624402E8, -2.970192132688297E7, 27373.82883395478),
                new Vector3D(54.08720813572575, -41.50176739356171, -3.5044954692950294)));

        return system;


    }

    /**
     * Returns a CelestialBody representing Titan with approximate position and velocity
     * relative to the Sun, based on Saturn's position and velocity.
     *
     * @param saturn the CelestialBody representing Saturn.
     * @return the CelestialBody representing Titan.
     */
    @NotNull
    private static CelestialBody getCelestialBody(CelestialBody saturn) {
        double titanDistance = 1.22187e6; // km, mean distance from Saturn
        double titanOrbitalSpeed = 5.57;  // km/s, mean orbital speed

        Vector3D titanPos = new Vector3D(
                saturn.getPosition().x + titanDistance,
                saturn.getPosition().y,
                saturn.getPosition().z
        );

        Vector3D titanVel = new Vector3D(
                saturn.getVelocity().x,
                saturn.getVelocity().y + titanOrbitalSpeed,
                saturn.getVelocity().z
        );

        return new CelestialBody("Titan", 1.3452e23, titanPos, titanVel);
    }

    public static double getRadiusKm(String name) {
        switch (name.toLowerCase()) {
            case "sun":      return 696_342.0;
            case "mercury":  return 2_439.7;
            case "venus":    return 6_051.8;
            case "earth":    return 6_371.0;
            case "moon":     return 1_737.4;
            case "mars":     return 3_389.5;
            case "jupiter":  return 69_911.0;
            case "saturn":   return 58_232.0;
            case "uranus":   return 25_362.0;
            case "neptune":  return 24_622.0;
            case "titan":    return 2_575.0;      // shown for completeness
            default:         return 0.0;          // unknown / artificial bodies
        }
    }

    public CelestialBody[] getAllBodies() {
        List<CelestialBody> bodies = loadFromTable();
        return bodies.toArray(new CelestialBody[0]);
    }
}