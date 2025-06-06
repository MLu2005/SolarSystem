package com.example.utilities.physics_utilities;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to load a predefined solar system with real celestial bodies and their
 * approximate positions and velocities at a given time.
 *
 * Units:
 *   • position  – kilometres (km)
 *   • velocity  – kilometres per second (km s⁻¹)
 *   • mass      – kilograms (kg)
 */
public final class SolarSystemFactory {
    public SolarSystemFactory() { /* prevent instantiation */ }
    /**
     * Loads a list of celestial bodies representing a simplified Solar-System snapshot
     * whose state vectors are an exact copy of the rows in IC.csv.
     *
     * @return a list of initialised CelestialBody objects
     */
    public static List<CelestialBody> loadFromTable() {
        List<CelestialBody> system = new ArrayList<>();

        system.add(new CelestialBody("Sun", 1.9900000000E30,
                new Vector3D(0, 0, 0),
                new Vector3D(0, 0, 0)));

        system.add(new CelestialBody("Mercury", 3.3000000000E23,
                new Vector3D(-5.6700000000E7, -3.2300000000E7, 2.5800000000E6),
                new Vector3D(1.3900000000E1, -4.0300000000E1, -4.5700000000E0)));

        system.add(new CelestialBody("Venus", 4.8700000000E24,
                new Vector3D(-1.0400000000E8, -3.1900000000E7, 5.5500000000E6),
                new Vector3D(9.8900000000E0, -3.3700000000E1, -1.0300000000E0)));

        system.add(new CelestialBody("Earth", 5.9700000000E24,
                new Vector3D(-1.4700000000E8, -2.9700000000E7, 2.7500000000E4),
                new Vector3D(5.3100000000E0, -2.9300000000E1, 6.6900000000E-04)));

        system.add(new CelestialBody("Moon", 7.3500000000E22,
                new Vector3D(-1.4700000000E8, -2.9500000000E7, 5.2900000000E4),
                new Vector3D(4.5300000000E0, -2.8600000000E1, 6.7300000000E-02)));

        system.add(new CelestialBody("Mars", 6.4200000000E23,
                new Vector3D(-2.1500000000E8, 1.2700000000E8, 7.9400000000E6),
                new Vector3D(-1.1500000000E1, -1.8700000000E1, -1.1100000000E-01)));

        system.add(new CelestialBody("Jupiter", 1.9000000000E27,
                new Vector3D(5.5400000000E7, 7.6200000000E8, -4.4000000000E6),
                new Vector3D(-1.3200000000E1, 1.2900000000E1, 5.2200000000E-02)));

        system.add(new CelestialBody("Saturn", 5.6800000000E26,
                new Vector3D(1.4200000000E9, -1.9100000000E8, -5.3300000000E7),
                new Vector3D(7.4800000000E-01, 9.5500000000E0, -1.9600000000E-01)));

        system.add(new CelestialBody("Titan", 1.3500000000E23,
                new Vector3D(1.4200000000E9, -1.9200000000E8, -5.2800000000E7),
                new Vector3D(5.9500000000E0, 7.6800000000E0, 2.5400000000E-01)));

        system.add(new CelestialBody("Uranus", 8.6800000000E25,
                new Vector3D(1.6200000000E9, 2.4300000000E9, -1.1900000000E7),
                new Vector3D(-5.7200000000E0, 3.4500000000E0, 8.7000000000E-02)));

        system.add(new CelestialBody("Neptune", 1.0200000000E26,
                new Vector3D(4.4700000000E9, -5.3100000000E7, -1.0200000000E8),
                new Vector3D(2.8700000000E-02, 5.4700000000E0, -1.1300000000E-01)));

        system.add(new CelestialBody("Noah's ark", 50000.0,
                new Vector3D(-1.4699366647890487E8, -2.9700655866209034E7, 27285.544752075686),
                new Vector3D(56.199263487003066, -31.70830008678653, -13.951757766821851)));

        return system;
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
