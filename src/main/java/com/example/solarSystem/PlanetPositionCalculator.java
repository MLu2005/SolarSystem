package com.example.solarSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import executables.solvers.RKF45Solver;

/**
 * PlanetPositionCalculator uses an RKF45 ODE solver to propagate a list of
 * CelestialBody objects from their initial positions and velocities (at J2000)
 * to a specified UTC date/time.
 */
public class PlanetPositionCalculator {

    private static final double GRAVITATIONAL_CONSTANT = 6.67430e-20;
    private static final double J2000_EPOCH_JULIAN_DATE = 2451545.0;
    private static final double SECONDS_PER_DAY = 86400.0;

    private final List<CelestialBody> initialBodies;
    private final double[] bodyMasses;
    private final double[] initialStateVector;
    private final RKF45Solver odeSolver = new RKF45Solver();

    /**
     * Constructs the calculator from a list of CelestialBody objects,
     * assumed to be at the J2000 epoch.
     */
    public PlanetPositionCalculator(List<CelestialBody> bodiesAtJ2000) {
        this.initialBodies = new ArrayList<>(bodiesAtJ2000);
        this.bodyMasses = new double[initialBodies.size()];
        this.initialStateVector = new double[initialBodies.size() * 6];

        // Populate masses and state vector from each body's properties
        for (int bodyIndex = 0; bodyIndex < initialBodies.size(); bodyIndex++) {
            CelestialBody body = initialBodies.get(bodyIndex);
            bodyMasses[bodyIndex] = body.getMass();
            Vector3D position = body.getPosition();
            Vector3D velocity = body.getVelocity();
            int offset = bodyIndex * 6;
            initialStateVector[offset]     = position.getX();
            initialStateVector[offset + 1] = position.getY();
            initialStateVector[offset + 2] = position.getZ();
            initialStateVector[offset + 3] = velocity.getX();
            initialStateVector[offset + 4] = velocity.getY();
            initialStateVector[offset + 5] = velocity.getZ();
        }
    }

    /**
     * Propagates all bodies from the J2000 epoch to the specified UTC date/time.
     * After propagation, each CelestialBody in the original list is updated in-place.
     *
     * @param targetDateTimeUtc the target date/time in UTC
     */
    public void propagateTo(LocalDateTime targetDateTimeUtc) {
        // Start time is zero seconds since J2000 epoch
        double startTimeSeconds = 0.0;
        // Convert the target UTC date/time to a Julian Date
        double targetJulianDate = convertLocalDateTimeToJulianDate(targetDateTimeUtc);
        // Compute the offset in seconds from J2000 epoch
        double targetTimeOffsetSeconds = (targetJulianDate - J2000_EPOCH_JULIAN_DATE) * SECONDS_PER_DAY;

        // Determine integration direction
        boolean integrateForwardInTime = targetTimeOffsetSeconds >= 0;
        // Stop condition: stop when current time passes the target in the correct direction
        BiFunction<Double, double[], Boolean> stopCondition = (currentTimeSeconds, stateVector) ->
            integrateForwardInTime
                ? (currentTimeSeconds >= targetTimeOffsetSeconds)
                : (currentTimeSeconds <= targetTimeOffsetSeconds);

        // Initial step size: one hour in seconds
        double initialStepSizeSeconds = 3600.0;
        // Maximum number of solver steps: rough bound based on 24 steps per day plus margin
        int maximumNumberOfSteps = (int) (Math.ceil(Math.abs(targetTimeOffsetSeconds) / SECONDS_PER_DAY) * 24) + 1000;

        // Perform the ODE integration
        double[][] solutionMatrix = odeSolver.solve(
            (currentTime, stateVector) -> computeStateDerivatives(currentTime, stateVector),
            startTimeSeconds,
            java.util.Arrays.copyOf(initialStateVector, initialStateVector.length),
            initialStepSizeSeconds,
            maximumNumberOfSteps,
            stopCondition
        );

        // Extract the final state (last row of the solution matrix)
        double[] finalStateVector = solutionMatrix[solutionMatrix.length - 1];

        // Update each body's position and velocity from the final state vector
        int numberOfBodies = initialBodies.size();
        for (int bodyIndex = 0; bodyIndex < numberOfBodies; bodyIndex++) {
            int offset = bodyIndex * 6;
            CelestialBody body = initialBodies.get(bodyIndex);
            Vector3D finalPosition = new Vector3D(
                finalStateVector[offset],
                finalStateVector[offset + 1],
                finalStateVector[offset + 2]
            );
            Vector3D finalVelocity = new Vector3D(
                finalStateVector[offset + 3],
                finalStateVector[offset + 4],
                finalStateVector[offset + 5]
            );
            body.setPosition(finalPosition);
            body.setVelocity(finalVelocity);
        }
    }

    /**
     * Computes the derivative of the state vector at a given time.
     * The state vector is [x0,y0,z0,vx0,vy0,vz0, ...] for each body.
     * Returns [vx0,vy0,vz0,ax0,ay0,az0, ...] where acceleration is from gravity.
     */
    private double[] computeStateDerivatives(double currentTimeSeconds, double[] stateVector) {
        int numberOfBodies = bodyMasses.length;
        double[] derivatives = new double[stateVector.length];

        // Position derivatives are the velocities
        for (int bodyIndex = 0; bodyIndex < numberOfBodies; bodyIndex++) {
            int offset = bodyIndex * 6;
            derivatives[offset]     = stateVector[offset + 3];
            derivatives[offset + 1] = stateVector[offset + 4];
            derivatives[offset + 2] = stateVector[offset + 5];
        }

        // Velocity derivatives (accelerations) from gravitational forces
        for (int targetIndex = 0; targetIndex < numberOfBodies; targetIndex++) {
            int baseOffset = targetIndex * 6;
            double posX = stateVector[baseOffset];
            double posY = stateVector[baseOffset + 1];
            double posZ = stateVector[baseOffset + 2];
            double accelerationX = 0.0;
            double accelerationY = 0.0;
            double accelerationZ = 0.0;

            // Sum contributions from all other bodies
            for (int otherIndex = 0; otherIndex < numberOfBodies; otherIndex++) {
                int otherOffset = otherIndex * 6;
                double dx = stateVector[otherOffset]     - posX;
                double dy = stateVector[otherOffset + 1] - posY;
                double dz = stateVector[otherOffset + 2] - posZ;
                double distanceCubed = Math.pow(
                    dx*dx + dy*dy + dz*dz, 1.5
                );
                accelerationX += GRAVITATIONAL_CONSTANT * bodyMasses[otherIndex] * dx / distanceCubed;
                accelerationY += GRAVITATIONAL_CONSTANT * bodyMasses[otherIndex] * dy / distanceCubed;
                accelerationZ += GRAVITATIONAL_CONSTANT * bodyMasses[otherIndex] * dz / distanceCubed;
            }

            derivatives[baseOffset + 3] = accelerationX;
            derivatives[baseOffset + 4] = accelerationY;
            derivatives[baseOffset + 5] = accelerationZ;
        }

        return derivatives;
    }

    /**
     * Converts a UTC LocalDateTime to its Julian Date value.
     */
    private static double convertLocalDateTimeToJulianDate(LocalDateTime dateTimeUtc) {
        int year = dateTimeUtc.getYear();
        int month = dateTimeUtc.getMonthValue();
        int day = dateTimeUtc.getDayOfMonth();
        int adjustment = (14 - month) / 12;
        int adjustedYear = year + 4800 - adjustment;
        int adjustedMonth = month + 12 * adjustment - 3;

        double julianDayNumber = day
            + Math.floor((153 * adjustedMonth + 2) / 5.0)
            + 365 * adjustedYear
            + Math.floor(adjustedYear / 4.0)
            - Math.floor(adjustedYear / 100.0)
            + Math.floor(adjustedYear / 400.0)
            - 32045;

        double fractionalDay = (dateTimeUtc.getHour() - 12) / 24.0
            + dateTimeUtc.getMinute() / 1440.0
            + dateTimeUtc.getSecond() / 86400.0
            + dateTimeUtc.getNano() / 86400e9;

        return julianDayNumber + fractionalDay;
    }

    /**
     * Returns the list of propagated CelestialBody objects.
     */
    public List<CelestialBody> getBodies() {
        return initialBodies;
    }
}
