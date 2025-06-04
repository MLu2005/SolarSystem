package com.example.lander;

import java.util.function.BiFunction;

import com.example.utilities.Vector3D;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetWindGrid;
import com.example.utilities.titanAtmosphere.TitanEnvironment;

import executables.solvers.RK4Solver;

public class LanderSimulator {
    private static final double DRAG_COEFFICIENT = 0.001;
    private static final double MAX_ATMOS_HEIGHT = 70.0;

    private static TitanEnvironment buildEnvironment(double windSpeedX) {
        com.example.solar_system.CelestialBody titan = null;
        for (com.example.solar_system.CelestialBody body :
                com.example.utilities.physics_utilities.SolarSystemFactory.loadFromTable()) {
            if (body.getName().equals("Titan")) {
                titan = body;
                break;
            }
        }
        double cellSize = 1.0;
        PlanetHeightGrid heightGrid = new PlanetHeightGrid(new com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid(titan, cellSize));
        heightGrid.generateFlatTerrain(0.0);
        PlanetWindGrid windGrid = new PlanetWindGrid(new com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid(titan, cellSize));
        windGrid.generateConstantWind(new Vector3D(windSpeedX, 0, 0));
        return new TitanEnvironment(heightGrid, windGrid);
    }

    public static double[][] simulateOpenLoop(double[] initialState, double timeStep, int maximumSteps, double windSpeed, double landerMassKilograms) {
        TitanEnvironment environment = buildEnvironment(windSpeed);
        Controller openLoopController = new OpenLoopController();
        LanderODE odeFunction = new LanderODE(openLoopController, environment, DRAG_COEFFICIENT, MAX_ATMOS_HEIGHT, landerMassKilograms);
        RK4Solver solver = new RK4Solver();
        BiFunction<Double, double[], Boolean> stopIfLanded = (time, state) -> state[1] <= 0.0;
        return solver.solve(odeFunction, 0.0, initialState, timeStep, maximumSteps, stopIfLanded);
    }

    public static double[][] simulateFeedback(double[] initialState, double timeStep, int maximumSteps, double windSpeed, double landerMassKilograms) {
        TitanEnvironment environment = buildEnvironment(windSpeed);
        Controller feedbackController = new FeedbackController();
        LanderODE odeFunction = new LanderODE(feedbackController, environment, DRAG_COEFFICIENT, MAX_ATMOS_HEIGHT, landerMassKilograms);
        RK4Solver solver = new RK4Solver();
        BiFunction<Double, double[], Boolean> stopIfLanded = (time, state) -> state[1] <= 0.0;
        return solver.solve(odeFunction, 0.0, initialState, timeStep, maximumSteps, stopIfLanded);
    }

    public static void main(String[] args) {
        double[] initialState = new double[] {
            0.0,     // horizontal position (km)
            1500.0,  // vertical position (km)
            1.48698278114141, // horizontal velocity (km/s)
            0.0,     // vertical velocity (km/s)
            0.0,     // tilt angle (rad)
            0.0      // tilt rate (rad/s)
        };

        double timeStep = 1.0;
        int maximumSteps = 10000;
        double windSpeed = 0.0001;
        double landerMassKilograms = 10000.0;
        System.out.println("=== Initial Conditions ===");
        System.out.printf("horizontalPosition = %.6f km%n", initialState[0]);
        System.out.printf("verticalPosition   = %.6f km%n", initialState[1]);
        System.out.printf("horizontalVelocity = %.6f km/s%n", initialState[2]);
        System.out.printf("verticalVelocity   = %.6f km/s%n", initialState[3]);
        System.out.printf("tiltAngle          = %.6f rad%n", initialState[4]);
        System.out.printf("tiltRate           = %.6f rad/s%n", initialState[5]);
        System.out.printf("landerMass         = %.1f kg%n", landerMassKilograms);
        System.out.printf("timeStep           = %.3f s%n", timeStep);
        System.out.printf("maximumSteps       = %d%n", maximumSteps);
        System.out.printf("windSpeed          = %.4f km/s%n%n", windSpeed);

        double[][] openLoopTrajectory = simulateOpenLoop(initialState, timeStep, maximumSteps, windSpeed, landerMassKilograms);

        System.out.println("=== Open-Loop Trajectory ===");
        System.out.println("time\tposX\tposY\tvelX\tvelY\ttilt\ttiltRate");
        for (double[] row : openLoopTrajectory) {
            double time      = row[0];
            double posX      = row[1];
            double posY      = row[2];
            double velX      = row[3];
            double velY      = row[4];
            double tiltAngle = row[5];
            double tiltRate  = row[6];
            System.out.printf(
                "%6.1f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f%n",
                time, posX, posY, velX, velY, tiltAngle, tiltRate
            );
        }

        double[][] feedbackTrajectory = simulateFeedback(initialState, timeStep, maximumSteps, windSpeed, landerMassKilograms);

        System.out.println("\n=== Feedback Trajectory ===");
        System.out.println("time\tposX\tposY\tvelX\tvelY\ttilt\ttiltRate");
        for (double[] row : feedbackTrajectory) {
            double time      = row[0];
            double posX      = row[1];
            double posY      = row[2];
            double velX      = row[3];
            double velY      = row[4];
            double tiltAngle = row[5];
            double tiltRate  = row[6];
            System.out.printf(
                "%6.1f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f%n",
                time, posX, posY, velX, velY, tiltAngle, tiltRate
            );
        }
    }
}
