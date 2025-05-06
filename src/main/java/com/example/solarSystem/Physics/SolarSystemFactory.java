package com.example.solarSystem.Physics;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Vector3D;

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
     *
     * @return a list of initialized CelestialBody objects
     */
    public static List<CelestialBody> loadFromTable() {
        List<CelestialBody> system = new ArrayList<>();

        system.add(new CelestialBody("Sun", 1.9885E30, new Vector3D(0, 0, 0), new Vector3D(0, 0, 0)));
        system.add(new CelestialBody("Mercury", 3.302E23,
                new Vector3D(-5.671193616E7, -3.22725121E7, 2583296.74),
                new Vector3D(13.88, -40.32, -4.57)));
        system.add(new CelestialBody("Venus", 4.8685E24,
                new Vector3D(-1.0393267209E8, -3.187005741E7, 5551106.73),
                new Vector3D(9.89, -33.69, -1.03)));
        system.add(new CelestialBody("Earth", 5.97219E24,
                new Vector3D(-1.474114613E8, -2.972578731E7, 27450.63),
                new Vector3D(5.31, -29.35, 0.0)));
        system.add(new CelestialBody("Moon", 7.349E22,
                new Vector3D(-1.4716601309E8, -2.946233624E7, 52891.08),
                new Vector3D(4.53, -28.59, 0.07)));
        system.add(new CelestialBody("Mars", 6.4171E23,
                new Vector3D(-2.1526640622E8, -3.044089497E7, 4542807.19),
                new Vector3D(2.65, -22.96, -0.41)));
        system.add(new CelestialBody("Jupiter", 1.8982E27,
                new Vector3D(6.0857935174E8, 4.672016269E7, -1.483329181E7),
                new Vector3D(-0.6, 12.13, 0.1)));
        system.add(new CelestialBody("Saturn", 5.6834E26,
                new Vector3D(1.31431287745E9, 2.054189449E7, -3.774477837E7),
                new Vector3D(-0.36, 9.47, 0.0)));
        system.add(new CelestialBody("Uranus", 8.681E25,
                new Vector3D(2.14338351673E9, 2.3914058252E8, -2.455374152E7),
                new Vector3D(-0.75, 6.41, -0.02)));
        system.add(new CelestialBody("Neptune", 1.0241E26,
                new Vector3D(4.45444278098E9, -3.9150318139E8, -8.633703583E7),
                new Vector3D(0.46, 5.41, -0.12)));
        system.add(new CelestialBody("Pluto", 1.303E22,
                new Vector3D(2.06759017176E9, -4.34460371416E9, -3.644156388E7),
                new Vector3D(4.74, 0.61, -1.33)));
        system.add(new CelestialBody("Titan", 1.3452E23,
                new Vector3D(1.34766797638E9, 2.253674319E7, -3.867191073E7),
                new Vector3D(-0.45, 9.41, 0.17)));

        return system;
    }
}
