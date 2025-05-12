package com.example.spaceMissions;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.Physics.SolarSystemFactory;
import com.example.solarSystem.SolarSystemODE;
import com.example.solarSystem.Vector3D;
import executables.Constants;
import executables.solvers.RK4Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * OrbitalInsertion calculates the orbit insertion trajectory using the RK4 method
 * based on the output from the Genetic Algorithm.
 */
public class OrbitalInsertion {

    private CelestialBody targetPlanet;
    private double targetAltitude;
    private static final double minAltitude = 100.0;
    private static final double maxAltitude = 300.0;
    private Vector3D velocity;
    private Vector3D position;
    private double probeMass;

    private List<CelestialBody> bodies;
    private CelestialBody probe;
    private double[][] trajectory;
    private boolean isOrbitAchieved;
    private double minDistanceToTarget;
    private double finalAltitude;

    /**
     * Constructor that takes the output of the GA
     * 
     * @param position Initial position vector
     * @param velocity Initial velocity vector
     * @param probeMass Mass of the probe
     * @param targetAltitude Desired orbital altitude
     */
    public OrbitalInsertion(Vector3D position, Vector3D velocity, double probeMass, double targetAltitude) {

        this.bodies = SolarSystemFactory.loadFromTable();
        this.position = position;
        this.velocity = velocity;
        this.probeMass = probeMass;
        this.targetAltitude = targetAltitude;


        this.targetPlanet = bodies.stream()
                .filter(b -> b.getName().equals("Titan"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Titan not found in celestial bodies"));

        // Create the probe
        this.probe = new CelestialBody("Probe", probeMass, position, velocity);
    }

    /**
     * Calculates the orbit insertion trajectory using RK4
     * 
     * @param simulationDuration Duration of the simulation in seconds
     * @param timeStep Time step for the simulation in seconds
     * @return true if orbit insertion was successful, false otherwise
     */
    public boolean calculateOrbitInsertion(double simulationDuration, double timeStep) {
        int steps = (int) (simulationDuration / timeStep);

        // create a list of all bodies including the probe
        List<CelestialBody> allBodies = new ArrayList<>(bodies);
        allBodies.add(probe);

        // generate the ODE function for the solar system
        BiFunction<Double, double[], double[]> odeFunction = SolarSystemODE.generateODE(allBodies);

        // initialize the state array
        double[] initialState = new double[allBodies.size() * 6];
        for (int i = 0; i < allBodies.size(); i++) {
            CelestialBody body = allBodies.get(i);
            Vector3D pos = body.getPosition();
            Vector3D vel = body.getVelocity();

            int idx = i * 6;
            initialState[idx] = pos.getX();
            initialState[idx + 1] = pos.getY();
            initialState[idx + 2] = pos.getZ();
            initialState[idx + 3] = vel.getX();
            initialState[idx + 4] = vel.getY();
            initialState[idx + 5] = vel.getZ();
        }

        // Create the RK4 solver and solve the system
        RK4Solver rk4Solver = new RK4Solver();
        trajectory = rk4Solver.solve(odeFunction, 0.0, initialState, timeStep, steps, null);

        // Analyze the trajectory to determine if orbit insertion was successful
        analyzeTrajectory(allBodies.size() - 1, allBodies.indexOf(targetPlanet));

        return isOrbitAchieved;
    }

    /**
     * Analyzes the trajectory to determine if orbit insertion was successful
     * 
     * @param probeIndex Index of the probe in the bodies list
     * @param targetIndex Index of the target planet in the bodies list
     */
    private void analyzeTrajectory(int probeIndex, int targetIndex) {
        minDistanceToTarget = Double.MAX_VALUE;
        double targetRadius = Constants.TITAN_RADIUS_KM;

        // Check if the probe is in a stable orbit around Titan
        boolean inOrbit = false;
        int orbitStabilityCounter = 0;
        double previousDistance = 0;
        double distanceVariation = 0;

        for (int i = 1; i < trajectory.length; i++) {
            // Extract probe and target positions
            Vector3D probePos = new Vector3D(
                trajectory[i][probeIndex * 6 + 1],
                trajectory[i][probeIndex * 6 + 2],
                trajectory[i][probeIndex * 6 + 3]
            );

            Vector3D targetPos = new Vector3D(
                trajectory[i][targetIndex * 6 + 1],
                trajectory[i][targetIndex * 6 + 2],
                trajectory[i][targetIndex * 6 + 3]
            );

            // Calculate distance between probe and target
            double distance = probePos.subtract(targetPos).magnitude();

            // Update minimum distance
            if (distance < minDistanceToTarget) {
                minDistanceToTarget = distance;
            }

            // Calculate altitude (distance from surface)
            double altitude = distance - targetRadius;

            // Check if the probe is within the desired altitude range
            if (altitude >= minAltitude && altitude <= maxAltitude) {
                // Check if the orbit is stable (distance doesn't vary too much)
                if (previousDistance > 0) {
                    distanceVariation = Math.abs(distance - previousDistance) / previousDistance;

                    // If variation is small, increment stability counter
                    if (distanceVariation < 0.05) { // 5% variation tolerance
                        orbitStabilityCounter++;
                    } else {
                        orbitStabilityCounter = 0;
                    }

                    // If orbit is stable for a sufficient number of steps, consider it achieved
                    if (orbitStabilityCounter > 100) { // Arbitrary threshold
                        inOrbit = true;
                        finalAltitude = altitude;
                        break;
                    }
                }
                previousDistance = distance;
            } else {
                orbitStabilityCounter = 0;
                previousDistance = 0;
            }
        }

        isOrbitAchieved = inOrbit;
    }

    public double[][] getTrajectory() {
        return trajectory;
    }

    public boolean isOrbitAchieved() {
        return isOrbitAchieved;
    }

    public double getMinDistanceToTarget() {
        return minDistanceToTarget;
    }

    public double getFinalAltitude() {
        return isOrbitAchieved ? finalAltitude : -1;
    }

    public void setTargetPlanet() {
        this.targetPlanet = bodies.stream()
                .filter(b -> b.getName().equals("Titan"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Titan not found in celestial bodies"));
    }

    public void setTargetAltitude(double altitude) {
        if (altitude < minAltitude || altitude > maxAltitude) {
            throw new IllegalArgumentException("Altitude must be between " + minAltitude + 
                                              " and " + maxAltitude + " kilometers");
        }
        this.targetAltitude = altitude;
    }

    /**
     * Creates an OrbitalInsertion object from the output of the Genetic Algorithm
     * 
     * @param genes The genes from the GA Individual (position and velocity)
     * @param targetAltitude The desired orbital altitude
     * @return A new OrbitalInsertion object
     */
    public static OrbitalInsertion fromGeneticAlgorithm(List<Double> genes, double targetAltitude) {
        if (genes.size() < 7) {
            throw new IllegalArgumentException("Genes list must contain at least 7 elements (x,y,z,vx,vy,vz,mass)");
        }

        Vector3D position = new Vector3D(genes.get(0), genes.get(1), genes.get(2));
        Vector3D velocity = new Vector3D(genes.get(3), genes.get(4), genes.get(5));
        double mass = genes.get(6);

        return new OrbitalInsertion(position, velocity, mass, targetAltitude);
    }

    /**
     * Runs the orbital insertion simulation with default parameters
     * 
     * @return true if orbit insertion was successful, false otherwise
     */
    public boolean runSimulation() {
        // Default simulation parameters: 30 days with 1-hour time steps
        return calculateOrbitInsertion(30 * 24 * 3600, 3600);
    }
}
