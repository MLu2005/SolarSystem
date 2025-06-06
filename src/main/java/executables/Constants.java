package executables;

/**
 * Constants class contains all important constants used throughout the application.
 * This centralized approach helps maintain consistency and makes it easier to update values.
 */
public class Constants {
    // Simulation constants
    public static final double TOLERANCE = 1e-9;
    public static final double SIM_LEN = 365 * 86400; // Simulation length in seconds (1 year)
    public static final double MAX_FLIGHT_TIME_DAYS = 365.0; // Maximum allowed flight time in days

    // Physical constants
    public static final double G = 6.6743E-20; // Gravitational constant in km^3 kg^-1 s^-2
    public static final double SECONDS_PER_DAY = 86400.0;
    public static final double J2000_EPOCH_JULIAN_DATE = 2451545.0;

    // Celestial body parameters
    public static final double EARTH_RADIUS_KM = 6371;
    public static final double R_EARTH = 6378.137; // Earth radius in km
    public static final double TITAN_RADIUS_KM = 2575;
    public static final double R_TITAN = TITAN_RADIUS_KM; // Titan radius in km
    public static final double TITAN_MASS_KG = 1.3452E23;
    public static final double MU_SATURN = G * 5.68E26; // Saturn's gravitational parameter in km³/s²
    public static final double MU_TITAN = G * TITAN_MASS_KG; // Titan's gravitational parameter in km³/s²

    // Spacecraft parameters
    public static final double PROBE_MASS = 50000.0; // kg
    public static final double ORIENTATION_EPS = 1e-12; // Orientation epsilon

    // Physics engine parameters
    public static final double SOFTENING_LENGTH = 100.0; // km
    public static final double INITIAL_STEP_SIZE = 60.0; // seconds
    public static final int MAX_STEPS = 500;


    // Random number generator with fixed seed for reproducibility
    public static final java.util.Random RNG = new java.util.Random(69);
}
