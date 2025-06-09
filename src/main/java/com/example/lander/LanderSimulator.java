package com.example.lander;

import java.util.Arrays;
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
        PlanetHeightGrid heightGrid = new PlanetHeightGrid(
            new com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid(titan, cellSize)
        );
        heightGrid.generateFlatTerrain(0.0);
        PlanetWindGrid windGrid = new PlanetWindGrid(
            new com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid(titan, cellSize)
        );
        windGrid.generateConstantWind(new Vector3D(windSpeedX, 0, 0));
        return new TitanEnvironment(heightGrid, windGrid);
    }

    public static double[][] simulateOpenLoop(
        double[] initialState,
        double timeStep,
        int maximumSteps,
        double windSpeed,
        double landerMassKilograms
    ) {
        TitanEnvironment environment = buildEnvironment(windSpeed);
        Controller openLoopController = new OpenLoopController();
        LanderODE odeFunction = new LanderODE(
            openLoopController,
            environment,
            DRAG_COEFFICIENT,
            MAX_ATMOS_HEIGHT,
            landerMassKilograms
        );
        RK4Solver solver = new RK4Solver();
        BiFunction<Double,double[],Boolean> stopIfLanded =
            (time, state) -> state[1] <= 0.0;
        return solver.solve(odeFunction, 0.0, initialState, timeStep, maximumSteps, stopIfLanded);
    }

    public static double[][] simulateFeedback(
        double[] initialState,
        double timeStep,
        int maximumSteps,
        double windSpeed,
        double landerMassKilograms
    ) {
        TitanEnvironment environment = buildEnvironment(windSpeed);
        Controller feedbackController = new FeedbackController();
        LanderODE odeFunction = new LanderODE(
            feedbackController,
            environment,
            DRAG_COEFFICIENT,
            MAX_ATMOS_HEIGHT,
            landerMassKilograms
        );
        RK4Solver solver = new RK4Solver();
        BiFunction<Double,double[],Boolean> stopIfLanded =
            (time, state) -> state[1] <= 0.0;
        return solver.solve(odeFunction, 0.0, initialState, timeStep, maximumSteps, stopIfLanded);
    }

    public static double[][] simulateCombined(
        double[] initialState,
        double timeStep,
        int maximumSteps,
        double windSpeed,
        double landerMassKilograms
    ) {
        TitanEnvironment environment = buildEnvironment(windSpeed);

        Controller openLoopController = new OpenLoopController();
        Controller feedbackController = new FeedbackController();
        // HERE is the only new line:
        Controller combinedController = 
            new CombinedController(openLoopController, feedbackController);

        LanderODE odeFunction = new LanderODE(
            combinedController,    // <– pass the combiner here
            environment,
            DRAG_COEFFICIENT,
            MAX_ATMOS_HEIGHT,
            landerMassKilograms
        );

        RK4Solver solver = new RK4Solver();
        BiFunction<Double,double[],Boolean> stopIfLanded =
            (time, state) -> state[1] <= 0.0;

        return solver.solve(
            odeFunction, 
            0.0, 
            initialState, 
            timeStep, 
            maximumSteps, 
            stopIfLanded
        );
    }

    public static void main(String[] args) {
        double titanRadius       = 2575.0;
        double distanceToTitan   = 2875.004939539644;
        double altitude          = distanceToTitan - titanRadius;
        double[] initialState    = {
            -2715.3163563925214,
             altitude,
             0.5807482731466309,
            -1.6690138988461283,
             0.0,
             0.0
        };

        double timeStep    = 1.0;
        int    maximumSteps= 2000000;
        double windSpeed   = 0.0001;
        double landerMass  = 50000.0;

        double[][] combinedTraj = simulateCombined(
            Arrays.copyOf(initialState, initialState.length),
            timeStep, maximumSteps, windSpeed, landerMass
        );

        System.out.println("\n=== Combined Trajectory ===");
        System.out.println("time\tposX\tposY\tvelX\tvelY\ttilt\ttiltRate");
        for (double[] row : combinedTraj) {
            System.out.printf(
                "%6.1f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f%n",
                row[0], row[1], row[2], row[3], row[4], row[5], row[6]
            );
        }
    }
}
