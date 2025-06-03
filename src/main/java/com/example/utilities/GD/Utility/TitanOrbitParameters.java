package com.example.utilities.GD.Utility;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Ship.StateVector;
import com.example.utilities.Vector3D;
import executables.Constants;

/**
 * TitanOrbitParameters defines the desired orbital parameters for a spacecraft
 * orbiting Titan. These parameters are used as targets for trajectory optimization.
 */
public class TitanOrbitParameters {

    // Using constants from the Constants class
    public static final double DEFAULT_ALTITUDE = Constants.TITAN_DEFAULT_ALTITUDE;
    public static final double DEFAULT_ECCENTRICITY = Constants.TITAN_DEFAULT_ECCENTRICITY;
    public static final double DEFAULT_INCLINATION = Constants.TITAN_DEFAULT_INCLINATION;
    public static final double DEFAULT_ARGUMENT_OF_PERIAPSIS = Constants.TITAN_DEFAULT_ARGUMENT_OF_PERIAPSIS;
    public static final double DEFAULT_LONGITUDE_OF_ASCENDING_NODE = Constants.TITAN_DEFAULT_LONGITUDE_OF_ASCENDING_NODE;

    // Titan physical parameters from Constants
    public static final double TITAN_RADIUS = Constants.TITAN_RADIUS_KM;
    public static final double TITAN_MASS = Constants.TITAN_MASS_KG;

    // Orbital period calculation constants
    private static final double G = Constants.G;                 // Gravitational constant in km^3 kg^-1 s^-2

    private final double altitude;                 // km above Titan's surface
    private final double eccentricity;             // 0 = circular, 0-1 = elliptical
    private final double inclination;              // degrees
    private final double argumentOfPeriapsis;      // degrees
    private final double longitudeOfAscendingNode; // degrees

    /**
     * Constructs a new TitanOrbitParameters with the specified orbital parameters.
     *
     * @param altitude                 Altitude above Titan's surface in km
     * @param eccentricity             Orbital eccentricity (0 = circular, 0-1 = elliptical)
     * @param inclination              Orbital inclination in degrees
     * @param argumentOfPeriapsis      Argument of periapsis in degrees
     * @param longitudeOfAscendingNode Longitude of ascending node in degrees
     */
    public TitanOrbitParameters(double altitude, double eccentricity, double inclination,
                               double argumentOfPeriapsis, double longitudeOfAscendingNode) {
        this.altitude = altitude;
        this.eccentricity = eccentricity;
        this.inclination = inclination;
        this.argumentOfPeriapsis = argumentOfPeriapsis;
        this.longitudeOfAscendingNode = longitudeOfAscendingNode;
    }

    /**
     * Constructs a new TitanOrbitParameters with default values.
     */
    public TitanOrbitParameters() {
        this(DEFAULT_ALTITUDE, DEFAULT_ECCENTRICITY, DEFAULT_INCLINATION,
             DEFAULT_ARGUMENT_OF_PERIAPSIS, DEFAULT_LONGITUDE_OF_ASCENDING_NODE);
    }

    /**
     * Calculates the semi-major axis of the orbit.
     *
     * @return The semi-major axis in km
     */
    public double calculateSemiMajorAxis() {
        return TITAN_RADIUS + altitude;
    }

    /**
     * Calculates the orbital period.
     *
     * @return The orbital period in seconds
     */
    public double calculateOrbitalPeriod() {
        double semiMajorAxis = calculateSemiMajorAxis();
        double mu = G * TITAN_MASS;
        return 2 * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3) / mu);
    }

    /**
     * Calculates the orbital velocity at periapsis (closest approach).
     *
     * @return The orbital velocity at periapsis in km/s
     */
    public double calculatePeriapsisVelocity() {
        double semiMajorAxis = calculateSemiMajorAxis();
        double mu = G * TITAN_MASS;
        double periapsis = semiMajorAxis * (1 - eccentricity);
        return Math.sqrt(mu * (2 / periapsis - 1 / semiMajorAxis));
    }

    /**
     * Calculates the orbital velocity at apoapsis (farthest point).
     *
     * @return The orbital velocity at apoapsis in km/s
     */
    public double calculateApoapsisVelocity() {
        double semiMajorAxis = calculateSemiMajorAxis();
        double mu = G * TITAN_MASS;
        double apoapsis = semiMajorAxis * (1 + eccentricity);
        return Math.sqrt(mu * (2 / apoapsis - 1 / semiMajorAxis));
    }

    /**
     * Calculates the delta-V required for orbit insertion from a flyby trajectory.
     *
     * @param approachVelocity The approach velocity relative to Titan in km/s
     * @return The delta-V required in km/s
     */
    public double calculateOrbitInsertionDeltaV(double approachVelocity) {
        double orbitalVelocity = calculatePeriapsisVelocity();
        return Math.abs(approachVelocity - orbitalVelocity);
    }

    // Getters

    public double getAltitude() {
        return altitude;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public double getInclination() {
        return inclination;
    }

    public double getArgumentOfPeriapsis() {
        return argumentOfPeriapsis;
    }

    public double getLongitudeOfAscendingNode() {
        return longitudeOfAscendingNode;
    }

    @Override
    public String toString() {
        return String.format(
            "Titan Orbit Parameters:\n" +
            "  Altitude: %.1f km\n" +
            "  Eccentricity: %.3f\n" +
            "  Inclination: %.1f°\n" +
            "  Argument of Periapsis: %.1f°\n" +
            "  Longitude of Ascending Node: %.1f°\n" +
            "  Orbital Period: %.2f hours\n" +
            "  Periapsis Velocity: %.3f km/s\n" +
            "  Apoapsis Velocity: %.3f km/s",
            altitude, eccentricity, inclination, argumentOfPeriapsis, longitudeOfAscendingNode,
            calculateOrbitalPeriod() / 3600, calculatePeriapsisVelocity(), calculateApoapsisVelocity()
        );
    }

    /**
     * Calculates orbital parameters from position and velocity vectors relative to Titan.
     * 
     * @param position Position vector relative to Titan in km
     * @param velocity Velocity vector relative to Titan in km/s
     * @return A TitanOrbitParameters object representing the orbit
     */
    public static TitanOrbitParameters calculateFromStateVectors(Vector3D position, Vector3D velocity) {
        // Gravitational parameter for Titan
        double mu = G * TITAN_MASS;

        // Calculate angular momentum vector
        Vector3D h = position.cross(velocity);

        // Calculate eccentricity vector
        Vector3D eVec = velocity.cross(h).scale(1.0/mu).subtract(position.normalize());
        double eccentricity = eVec.magnitude();

        // Calculate node vector (vector pointing towards ascending node)
        Vector3D zAxis = new Vector3D(0, 0, 1);
        Vector3D nodeVector = zAxis.cross(h);

        // Calculate inclination
        double inclination = Math.toDegrees(Math.acos(h.z / h.magnitude()));

        // Calculate longitude of ascending node
        double longitudeOfAscendingNode = 0.0;
        if (nodeVector.magnitude() > 1e-10) {
            longitudeOfAscendingNode = Math.toDegrees(Math.atan2(nodeVector.y, nodeVector.x));
            if (longitudeOfAscendingNode < 0) {
                longitudeOfAscendingNode += 360.0;
            }
        }

        // Calculate argument of periapsis
        double argumentOfPeriapsis = 0.0;
        if (nodeVector.magnitude() > 1e-10 && eccentricity > 1e-10) {
            double cosArgP = nodeVector.normalize().dot(eVec.normalize());
            double sinArgP = nodeVector.normalize().cross(eVec.normalize()).dot(h.normalize());
            argumentOfPeriapsis = Math.toDegrees(Math.atan2(sinArgP, cosArgP));
            if (argumentOfPeriapsis < 0) {
                argumentOfPeriapsis += 360.0;
            }
        }

        // Calculate semi-major axis
        double r = position.magnitude();
        double v2 = velocity.dot(velocity);
        double semiMajorAxis = 1.0 / (2.0/r - v2/mu);

        // Calculate altitude from semi-major axis
        double altitude = semiMajorAxis * (1 - eccentricity) - TITAN_RADIUS;

        return new TitanOrbitParameters(
            altitude, 
            eccentricity, 
            inclination, 
            argumentOfPeriapsis, 
            longitudeOfAscendingNode
        );
    }

    /**
     * Computes the distance between this orbit and another orbit.
     * Returns a normalized distance metric where smaller values indicate closer orbits.
     * The distance calculation is symmetric, meaning distanceToOrbit(a, b) = distanceToOrbit(b, a).
     * 
     * @param other The other orbit to compare with
     * @return A distance metric (smaller is closer)
     */
    public double distanceToOrbit(TitanOrbitParameters other) {
        // Calculate distance metrics for each orbital parameter
        double altitudeError = Math.abs(this.altitude - other.altitude);
        double eccentricityError = Math.abs(this.eccentricity - other.eccentricity);
        double inclinationError = Math.abs(this.inclination - other.inclination);
        double argPeriapsisError = Math.min(
            Math.abs(this.argumentOfPeriapsis - other.argumentOfPeriapsis),
            360 - Math.abs(this.argumentOfPeriapsis - other.argumentOfPeriapsis)
        );
        double lonAscNodeError = Math.min(
            Math.abs(this.longitudeOfAscendingNode - other.longitudeOfAscendingNode),
            360 - Math.abs(this.longitudeOfAscendingNode - other.longitudeOfAscendingNode)
        );

        // Use fixed normalization factors to ensure symmetry
        double normalizedAltitudeError = altitudeError / 1000.0; // Normalize by 1000 km
        double normalizedEccentricityError = eccentricityError / 0.1; // Normalize by 0.1
        double normalizedInclinationError = inclinationError / 90.0; // Normalize by 90 degrees
        double normalizedArgPeriapsisError = argPeriapsisError / 180.0; // Normalize by 180 degrees
        double normalizedLonAscNodeError = lonAscNodeError / 180.0; // Normalize by 180 degrees

        // Combine errors with weights
        return 0.3 * normalizedAltitudeError + 
               0.2 * normalizedEccentricityError + 
               0.2 * normalizedInclinationError +
               0.15 * normalizedArgPeriapsisError +
               0.15 * normalizedLonAscNodeError;
    }

    /**
     * Determines if the spacecraft is in a stable orbit around Titan based on
     * the provided state vector.
     * 
     * @param stateVector The current state vector of the spacecraft
     * @param titan The celestial body representing Titan
     * @return true if the orbit is stable, false otherwise
     */
    public static boolean isStableOrbit(StateVector stateVector, CelestialBody titan) {
        // Position and velocity relative to Titan
        Vector3D relPosition = stateVector.getPosition().subtract(titan.getPosition());
        Vector3D relVelocity = stateVector.getVelocity().subtract(titan.getVelocity());

        // Calculate orbital energy
        double mu = G * titan.getMass();
        double r = relPosition.magnitude();
        double v2 = relVelocity.dot(relVelocity);
        double specificEnergy = v2/2 - mu/r;

        // Calculate semi-major axis
        double semiMajorAxis = -mu / (2 * specificEnergy);

        // Calculate eccentricity
        Vector3D h = relPosition.cross(relVelocity);
        Vector3D eVec = relVelocity.cross(h).scale(1.0/mu).subtract(relPosition.normalize());
        double eccentricity = eVec.magnitude();

        // Stability criteria:
        // 1. Orbit must be elliptical (e < 1)
        // 2. Periapsis must be above Titan's surface
        // 3. Apoapsis must be within Titan's sphere of influence

        // Check if orbit is elliptical
        if (eccentricity >= 1.0) {
            return false;
        }

        // Check if periapsis is above Titan's surface
        double periapsis = semiMajorAxis * (1 - eccentricity);
        if (periapsis < TITAN_RADIUS) {
            return false;
        }

        // Approximate Titan's sphere of influence (in km)
        // Using the formula: r_SOI = a * (m/M)^(2/5) where a is Saturn-Titan distance
        double saturnTitanDistance = 1.22E6; // km
        double titanSaturnMassRatio = TITAN_MASS / 5.6834E26; // Saturn's mass
        double titanSOI = saturnTitanDistance * Math.pow(titanSaturnMassRatio, 0.4);

        // Check if apoapsis is within Titan's sphere of influence
        double apoapsis = semiMajorAxis * (1 + eccentricity);
        if (apoapsis > titanSOI) {
            return false;
        }

        return true;
    }
}
