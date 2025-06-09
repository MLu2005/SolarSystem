package com.example.utilities.physics_utilities;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to load a predefined solar system with real celestial bodies and their
 * positions and velocities at a given time.
 *
 * -> https://ssd.jpl.nasa.gov/horizons/app.html#/
 *
 * -> ALL OBJECTS SET TO START FROM APRIL 1ST 2025!
 * ROCKET FOLLOW GA!
 * Units:
 *   • position  – kilometers (km)
 *   • velocity  – kilometers per second (km/s)
 *   • mass      – kilograms (kg)
 */
public final class SolarSystemFactory {
    private SolarSystemFactory() { /* prevent instantiation */ }

    public static List<CelestialBody> loadFromTable() {
        List<CelestialBody> system = new ArrayList<>();

        CelestialBody sun = new CelestialBody("Sun", 1.989e30,
                new Vector3D(0, 0, 0),
                new Vector3D(0, 0, 0));
        system.add(sun);

        system.add(new CelestialBody("Mercury", 3.3011e23,
                new Vector3D(-5.671193615988735E+07, -3.227251209672350E+07, 2.583296735726040E+06),
                new Vector3D( 1.388030865975940E+01, -4.032390841463938E+01, -4.567160923750894E+00)));

        system.add(new CelestialBody("Venus", 4.8675e24,
                new Vector3D(-1.039326720916525E+08, -3.187005740943382E+07, 5.551106734133117E+06),
                new Vector3D( 9.885723304837379E+00, -3.369401290966694E+01, -1.032687053337243E+00)));

        CelestialBody earth = new CelestialBody("Earth", 5.97237e24,
                new Vector3D(-1.474114613044819E+08, -2.972578730668059E+07, 2.745063093019836E+04),
                new Vector3D( 5.306839723370035E+00, -2.934993232297309E+01, 6.693785809943620E-04));
        system.add(earth);

        CelestialBody moon = new CelestialBody("Moon", 7.342e22,
                new Vector3D(-1.471660130896692E+08, -2.946233624390671E+07,  5.289107585630007E+04),
                new Vector3D(4.533176913775855E+00, -2.858677469962307E+01, 6.725183765165710E-02));
        system.add(moon);

        system.add(new CelestialBody("Mars", 6.4171e23,
                new Vector3D(-2.145953111232504E+08, 1.266512112612688E+08, 7.939425621251538E+06),
                new Vector3D(-1.148153686680200E+01, -1.874941366896797E+01, -1.111751414588387E-01)));

        system.add(new CelestialBody("Jupiter", 1.8982e27,
                new Vector3D(5.543705768794881E+07,  7.620296282600125E+08, -4.400748237416446E+06),
                new Vector3D(-1.318182239145089E+01, 1.572192901176178E+00 ,  2.885060369130182E-01)));

        CelestialBody saturn = new CelestialBody("Saturn", 5.6834e26,
                new Vector3D(1.422232874477568E+09, -1.907185789783441E+08, -5.331045504162484E+07),
                new Vector3D(0.7466654196823925, 9.554030161946484,   -0.1960083815552225));
        system.add(saturn);

        CelestialBody titan = new CelestialBody("Titan", 1.3452e23,
                new Vector3D(1.421787721861711E+09, -1.917156604354737E+08,-5.275190739154144E+07),
                new Vector3D(5.951711470718787E+00, 7.676884294391810E+00,  2.538506864185868E-01)
        );
        system.add(titan);

        system.add(new CelestialBody("Uranus", 8.681e25,
                new Vector3D(1.615976888879250E+09 , 2.434176310227056E+09, -1.189478209689093E+07),
                new Vector3D(-5.723661504149057E+00, 3.449051233303488E+00, 8.671510810638172E-02)));

        system.add(new CelestialBody("Neptune", 1.02413e26,
                new Vector3D(4.469540357827111E+09, -5.309854693989044E+07, -1.019116933726746E+08),
                new Vector3D(2.794039452517386E-02, 5.466848606800432E+00, -1.128524365224877E-01)));

        // Noah's Ark (hypothetical spacecraft)
        system.add(new CelestialBody("Noah's Ark", 50000.0,
                new Vector3D(-1.474051e+08, -2.972643e+07, 2.724182e+04),
                new Vector3D(57.199429, -31.663384, -13.655187)));

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
            case "titan":    return 2_575.0;
            default:         return 0.0;
        }
    }
}
