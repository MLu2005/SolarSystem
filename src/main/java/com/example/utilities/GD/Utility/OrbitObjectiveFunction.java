package com.example.utilities.GD.Utility;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Ship.StateVector;
import com.example.utilities.Vector3D;
import executables.Constants;

/**
 * OrbitObjectiveFunction calculates how well a spacecraft's trajectory matches desired orbital parameters
 * around Titan. This is used for trajectory optimization to achieve specific orbits.
 */
public class OrbitObjectiveFunction {

    // Desired orbital parameters around Titan
    private final double desiredAltitude;     // km above Titan's surface
    private final double desiredEccentricity; // 0 = circular, 0-1 = elliptical
    private final double desiredInclination;  // degrees

    // Using constants from the Constants class
    private static final double TITAN_RADIUS = Constants.TITAN_RADIUS_KM;
    private static final double TITAN_MASS = Constants.TITAN_MASS_KG;
    private static final double G = Constants.G; // Gravitational constant in km^3 kg^-1 s^-2

    /**
     * Constructs a new OrbitObjectiveFunction with the specified desired orbital parameters.
     *
     * @param desiredAltitude     Desired altitude above Titan's surface in km
     * @param desiredEccentricity Desired orbital eccentricity (0 = circular, 0-1 = elliptical)
     * @param desiredInclination  Desired orbital inclination in degrees
     */
    public OrbitObjectiveFunction(double desiredAltitude, double desiredEccentricity, double desiredInclination) {
        this.desiredAltitude = desiredAltitude;
        this.desiredEccentricity = desiredEccentricity;
        this.desiredInclination = desiredInclination;
    }

    /**
     * Calculates the fitness of a state vector based on how well it matches the desired orbit.
     * Higher fitness values indicate better matches to the desired orbit.
     *
     * @param stateVector The state vector to evaluate
     * @param titan       The celestial body representing Titan
     * @return A fitness value (higher is better)
     */
    public double calculateFitness(StateVector stateVector, CelestialBody titan) {
        // Calculate orbital elements from state vector relative to Titan
        OrbitalElements elements = calculateOrbitalElements(stateVector, titan);

        // Calculate distance metrics for each orbital parameter
        double altitudeError = Math.abs(elements.getSemiMajorAxis() - TITAN_RADIUS - desiredAltitude);
        double eccentricityError = Math.abs(elements.getEccentricity() - desiredEccentricity);
        double inclinationError = Math.abs(elements.getInclination() - desiredInclination);

        // Normalize errors (lower is better)
        double normalizedAltitudeError = altitudeError / (desiredAltitude + 1.0);
        double normalizedEccentricityError = eccentricityError / (desiredEccentricity + 0.1);
        double normalizedInclinationError = inclinationError / (desiredInclination + 1.0);

        // Combine errors with weights
        double totalError = 0.5 * normalizedAltitudeError + 
                           0.3 * normalizedEccentricityError + 
                           0.2 * normalizedInclinationError;

        // Convert to fitness (higher is better)
        return 1.0 / (totalError + 0.001);
    }

    /**
     * Calculates orbital elements from a state vector relative to a central body.
     *
     * @param stateVector The state vector
     * @param centralBody The central body (Titan)
     * @return The orbital elements
     */
    private OrbitalElements calculateOrbitalElements(StateVector stateVector, CelestialBody centralBody) {
        // Position and velocity relative to central body
        Vector3D relPosition = stateVector.getPosition().subtract(centralBody.getPosition());
        Vector3D relVelocity = stateVector.getVelocity().subtract(centralBody.getVelocity());

        // Calculate orbital elements
        double mu = G * centralBody.getMass(); // Gravitational parameter

        // Angular momentum vector
        Vector3D h = relPosition.cross(relVelocity);

        // Eccentricity vector
        Vector3D eVec = relVelocity.cross(h).scale(1.0/mu).subtract(relPosition.normalize());
        double eccentricity = eVec.magnitude();

        // Semi-major axis
        double r = relPosition.magnitude();
        double v2 = relVelocity.dot(relVelocity);
        double semiMajorAxis = 1.0 / (2.0/r - v2/mu);

        // Inclination
        double inclination = Math.toDegrees(Math.acos(h.z / h.magnitude()));

        return new OrbitalElements(semiMajorAxis, eccentricity, inclination);
    }

    /**
     * Inner class to represent orbital elements.
     */
    public static class OrbitalElements {
        private final double semiMajorAxis;  // km
        private final double eccentricity;   // dimensionless
        private final double inclination;    // degrees

        public OrbitalElements(double semiMajorAxis, double eccentricity, double inclination) {
            this.semiMajorAxis = semiMajorAxis;
            this.eccentricity = eccentricity;
            this.inclination = inclination;
        }

        public double getSemiMajorAxis() {
            return semiMajorAxis;
        }

        public double getEccentricity() {
            return eccentricity;
        }

        public double getInclination() {
            return inclination;
        }
    }

    /**
     * Returns the desired altitude above Titan's surface.
     *
     * @return Desired altitude in km
     */
    public double getDesiredAltitude() {
        return desiredAltitude;
    }

    /**
     * Returns the desired orbital eccentricity.
     *
     * @return Desired eccentricity
     */
    public double getDesiredEccentricity() {
        return desiredEccentricity;
    }

    /**
     * Returns the desired orbital inclination.
     *
     * @return Desired inclination in degrees
     */
    public double getDesiredInclination() {
        return desiredInclination;
    }
}
