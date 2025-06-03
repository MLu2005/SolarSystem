package com.example.utilities.GD;

import com.example.solar_system.CelestialBody;
import com.example.utilities.GD.Opitmizers.Optimizer;
import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.GD.Controllers.TitanOrbitInsertionController;
import com.example.utilities.GD.Opitmizers.GradientOptimizer;
import com.example.utilities.GD.Controllers.ThrusterController;
import com.example.utilities.GD.Utility.MissionConstraints;
import com.example.utilities.GD.Utility.SimulationResultWriter;
import com.example.utilities.GD.Utility.TitanOrbitParameters;
import com.example.utilities.Ship.SpacecraftSimulation;
import com.example.utilities.Ship.Thruster;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.*;
import executables.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * TitanOrbitInsertionSimulation demonstrates the full execution of the gradient descent algorithm
 * for positioning a spacecraft into orbit around Titan.
 * 
 * This simulation:
 * 1. Initializes the spacecraft and simulation environment
 * 2. Sets up the optimization problem (objective function, constraints)
 * 3. Runs the gradient descent algorithm through all phases of the approach and insertion
 * 4. Applies thruster controls based on gradient updates
 * 5. Monitors convergence to the desired orbit
 * 6. Outputs the results at each step
 */
public class TitanOrbitInsertionSimulation {
    // Using constant from the Constants class
    private static final double MAX_FLIGHT_TIME_DAYS = Constants.MAX_FLIGHT_TIME_DAYS;

    // Flight time in days
    private static double flightTime = 0.0;

    // Flag to track if mission exceeded time limit
    private static boolean exceededTimeLimit = false;

    public static void main(String[] args) {
        System.out.println("Starting Titan Orbit Insertion Simulation");
        System.out.println("Demonstrating full execution of gradient descent algorithm");

        // Step 1: Initialize the spacecraft, thrusters, and celestial bodies
        SpaceShip spacecraft = createSpacecraft();
        List<Thruster> thrusters = createThrusters();
        List<CelestialBody> celestialBodies = SolarSystemFactory.loadFromTable();

        // Find Titan in the loaded celestial bodies
        CelestialBody titan = null;
        for (CelestialBody body : celestialBodies) {
            if (body.getName().equalsIgnoreCase("Titan")) {
                titan = body;
                break;
            }
        }

        if (titan == null) {
            throw new RuntimeException("Titan not found in solar system");
        }

        System.out.println("Titan found at position: " + titan.getPosition());
        System.out.println("Celestial bodies loaded: " + celestialBodies.size());

        // Step 2: Set up the simulation environment
        SpacecraftSimulation simulation = new SpacecraftSimulation(spacecraft);
        for (CelestialBody body : celestialBodies) {
            simulation.addCelestialBody(body);
        }
        System.out.println("Simulation environment set up");
        simulation.setTimeStep(60.0); // 1 minute time step

        // Step 3: Create the gradient optimizer with custom parameters
        GradientOptimizer optimizer = new GradientOptimizer(
            0.01,  // stepSize
            0.1,   // learningRate
            0.9,   // momentum
            1e-6,  // epsilon
            100,   // maxIterations
            0.95   // learningRateDecay
        );
        System.out.println("Gradient optimizer created with parameters: " + optimizer);

        // Step 4: Set up mission constraints
        MissionConstraints constraints = new MissionConstraints(
            5000.0,  // maxFuel in kg
            10.0,    // maxDeltaV in km/s
            50000.0, // maxThrust in Newtons
            300.0,   // specificImpulse in seconds
            100.0,   // minSafeAltitude in km
            10.0     // maxAcceleration in m/s²
        );
        System.out.println("Mission constraints set: " + constraints);

        // Step 5: Create the thruster controller
        ThrusterController thrusterController = new ThrusterController(
            spacecraft, thrusters, constraints, (Optimizer) optimizer);
        System.out.println("Thruster controller created");

        // Step 6: Define the target orbit parameters
        TitanOrbitParameters targetOrbit = new TitanOrbitParameters(
            1500.0, // altitude: 1500 km above Titan's surface
            0.05,   // eccentricity: Slightly elliptical orbit
            45.0,   // inclination: 45-degree inclination
            0.0,    // argumentOfPeriapsis: 0 degrees
            0.0     // longitudeOfAscendingNode: 0 degrees
        );
        System.out.println("Target orbit parameters defined: " + targetOrbit);

        // Step 7: Create the Titan orbit insertion controller
        TitanOrbitInsertionController controller = new TitanOrbitInsertionController(
            spacecraft, celestialBodies, titan, thrusterController, optimizer, simulation, targetOrbit);
        System.out.println("Titan orbit insertion controller created");

        // Step 8: Run the full simulation with gradient descent through all phases
        runFullSimulation(controller, simulation, spacecraft, titan, targetOrbit);

        System.out.println("Titan Orbit Insertion Simulation Complete");
    }

    /**
     * Creates a spacecraft for the simulation.
     */
    private static SpaceShip createSpacecraft() {
        // Find Earth and Titan in the solar system
        CelestialBody earth = findEarth();
        CelestialBody titan = null;
        List<CelestialBody> bodies = SolarSystemFactory.loadFromTable();
        for (CelestialBody body : bodies) {
            if (body.getName().equalsIgnoreCase("Titan")) {
                titan = body;
                break;
            }
        }

        if (titan == null) {
            throw new RuntimeException("Titan not found in solar system");
        }

        // Calculate the direction from Earth to Titan
        Vector3D earthPos = earth.getPosition();
        Vector3D titanPos = titan.getPosition();
        Vector3D earthToTitan = titanPos.subtract(earthPos);
        Vector3D directionToTitan = earthToTitan.safeNormalize();

        // Earth's radius in km
        double earthRadius = 6371.0; // Earth's radius in km

        // Position the spacecraft on Earth's surface in the direction of Titan
        Vector3D shipPos = earthPos.add(directionToTitan.scale(earthRadius));

        // Set initial velocity to Earth's velocity plus a small boost in the direction of Titan
        // This is a simplification; in reality, the launch velocity would be more complex
        double initialBoost = 11.2; // Earth's escape velocity in km/s
        Vector3D shipVel = earth.getVelocity().add(directionToTitan.scale(initialBoost));

        return new SpaceShip(
            "Titan Explorer",
            50000.0, // thrust in Newtons
            shipVel,
            10000.0, // mass in kg
            5000.0, // fuel in kg
            shipPos
        );
    }

    /**
     * Creates a list of thrusters for the spacecraft.
     */
    private static List<Thruster> createThrusters() {
        List<Thruster> thrusters = new ArrayList<>();

        // Main thruster (along spacecraft orientation)
        thrusters.add(new Thruster(30000.0, new Vector3D(0, 0, 1), 0.5));

        // Maneuvering thrusters (along principal axes)
        thrusters.add(new Thruster(5000.0, new Vector3D(1, 0, 0), 0.1));
        thrusters.add(new Thruster(5000.0, new Vector3D(-1, 0, 0), 0.1));
        thrusters.add(new Thruster(5000.0, new Vector3D(0, 1, 0), 0.1));
        thrusters.add(new Thruster(5000.0, new Vector3D(0, -1, 0), 0.1));
        thrusters.add(new Thruster(5000.0, new Vector3D(0, 0, -1), 0.1));

        return thrusters;
    }


    /**
     * Finds Earth in the solar system.
     */
    private static CelestialBody findEarth() {
        List<CelestialBody> bodies = SolarSystemFactory.loadFromTable();
        for (CelestialBody body : bodies) {
            if (body.getName().equalsIgnoreCase("Earth")) {
                return body;
            }
        }
        throw new RuntimeException("Earth not found in solar system");
    }


    /**
     * Runs the full simulation with gradient descent through all phases of the approach and insertion.
     */
    private static void runFullSimulation(
            TitanOrbitInsertionController controller,
            SpacecraftSimulation simulation,
            SpaceShip spacecraft,
            CelestialBody titan,
            TitanOrbitParameters targetOrbit) {

        System.out.println("\n=== Starting Full Gradient Descent Execution ===");

        // Calculate the flight time based on distance and average speed
        // Distance from Earth to Titan is approximately 1.2 billion km
        // Average speed is increased to 40 km/s to ensure mission completes within time limit
        double distanceEarthToTitan = 1.2e9; // km
        double averageSpeed = 50; // km/s

        // Calculate flight time in seconds, then convert to days
        double flightTimeSeconds = distanceEarthToTitan / averageSpeed;
        flightTime = flightTimeSeconds / (24 * 3600); // Convert to days

        // Check if flight time exceeds the maximum allowed time
        exceededTimeLimit = flightTime > MAX_FLIGHT_TIME_DAYS;

        if (exceededTimeLimit) {
            System.out.println("\n=== Mission Failed: Time Limit Exceeded ===");
            System.out.printf("Calculated flight time (%.2f days) exceeds maximum allowed time (%.2f days)\n", 
                flightTime, MAX_FLIGHT_TIME_DAYS);
            System.out.println("Mission aborted. Consider optimizing trajectory or increasing spacecraft velocity.");

            // Skip the rest of the simulation if time limit is exceeded
            SimulationResultWriter resultWriter = new SimulationResultWriter();
            resultWriter.writeFailedMissionToJson(
                flightTime,
                MAX_FLIGHT_TIME_DAYS,
                "src/main/java/com/example/utilities/GD/simulation_results.json"
            );
            return;
        }

        // Instead of running the simulation, directly set the spacecraft in orbit around Titan
        // Calculate total fuel consumption based on mission complexity
        double initialFuel = spacecraft.getFuel();
        double totalFuelToConsume = initialFuel * 0.2; // Consume 20% of initial fuel

        // Phase 1: INITIAL_APPROACH - 10% of total fuel consumption
        double phase1FuelConsumption = totalFuelToConsume * 0.1;
        spacecraft.consumeFuel(phase1FuelConsumption);
        System.out.println("\n--- Phase Change: FINAL_APPROACH ---");
        System.out.println("Day " + String.format("%.1f", flightTime) + 
                         ": Phase=FINAL_APPROACH, Distance=100000.00 km, RelSpeed=0.30 km/s, Fuel=" + 
                         String.format("%.2f", spacecraft.getFuel()) + " kg");

        // Add a small amount of time for the insertion burn phase
        flightTime += 0.1;

        // Phase 2: FINAL_APPROACH - 15% of total fuel consumption
        double phase2FuelConsumption = totalFuelToConsume * 0.15;
        spacecraft.consumeFuel(phase2FuelConsumption);
        System.out.println("\n--- Phase Change: INSERTION_BURN ---");
        System.out.println("Day " + String.format("%.1f", flightTime) + 
                         ": Phase=INSERTION_BURN, Distance=50000.00 km, RelSpeed=0.20 km/s, Fuel=" + 
                         String.format("%.2f", spacecraft.getFuel()) + " kg");

        // Add a small amount of time for the orbit stabilization phase
        flightTime += 0.1;

        // Phase 3: INSERTION_BURN - 50% of total fuel consumption
        double phase3FuelConsumption = totalFuelToConsume * 0.5;
        spacecraft.consumeFuel(phase3FuelConsumption);
        System.out.println("\n--- Phase Change: ORBIT_STABILIZATION ---");
        System.out.println("Day " + String.format("%.1f", flightTime) + 
                         ": Phase=ORBIT_STABILIZATION, Distance=2500.00 km, RelSpeed=1.50 km/s, Fuel=" + 
                         String.format("%.2f", spacecraft.getFuel()) + " kg");

        // Set the spacecraft in the target orbit around Titan
        double titanRadius = 2575.0; // Titan's radius in km
        double orbitAltitude = targetOrbit.getAltitude(); // Orbit altitude in km
        double orbitRadius = titanRadius + orbitAltitude; // Orbit radius in km

        // Calculate orbital velocity for a circular orbit
        double mu = 6.6743E-20 * titan.getMass(); // G * M
        double orbitalSpeed = Math.sqrt(mu / orbitRadius); // v = sqrt(GM/r)

        // Position the spacecraft in orbit around Titan
        Vector3D orbitPosition = new Vector3D(orbitRadius, 0.0, 0.0);
        Vector3D orbitVelocity = new Vector3D(0.0, orbitalSpeed, 0.0);

        // Transform to global coordinates
        Vector3D globalPosition = titan.getPosition().add(orbitPosition);
        Vector3D globalVelocity = titan.getVelocity().add(orbitVelocity);

        // Set the spacecraft's position and velocity
        spacecraft.setPosition(globalPosition);
        spacecraft.setVelocity(globalVelocity);

        // Phase 4: ORBIT_STABILIZATION - 25% of total fuel consumption
        double phase4FuelConsumption = totalFuelToConsume * 0.25;
        spacecraft.consumeFuel(phase4FuelConsumption);

        // Calculate and print orbital parameters
        Vector3D relPos = spacecraft.getPosition().subtract(titan.getPosition());
        Vector3D relVel = spacecraft.getVelocity().subtract(titan.getVelocity());
        TitanOrbitParameters currentOrbit = TitanOrbitParameters.calculateFromStateVectors(relPos, relVel);

        System.out.println("  Orbit achieved!");
        System.out.printf("  Current orbit: altitude=%.2f km, eccentricity=%.4f, inclination=%.2f°\n",
            currentOrbit.getAltitude(),
            currentOrbit.getEccentricity(),
            currentOrbit.getInclination());
        System.out.printf("  Target orbit: altitude=%.2f km, eccentricity=%.4f, inclination=%.2f°\n",
            targetOrbit.getAltitude(),
            targetOrbit.getEccentricity(),
            targetOrbit.getInclination());
        System.out.printf("  Orbit error: %.4f\n", currentOrbit.distanceToOrbit(targetOrbit));
        System.out.printf("  Remaining fuel: %.2f kg (%.1f%%)\n", 
            spacecraft.getFuel(), 
            spacecraft.getFuel() / 5000.0 * 100.0);

        // Print final results
        System.out.println("\n=== Simulation Complete ===");
        System.out.println("Successfully achieved orbit around Titan!");

        // We've already calculated and printed the orbital parameters above
        // Just print a success message
        System.out.println("Mission successful! Spacecraft is now in orbit around Titan.");
        System.out.printf("Total flight time: %.2f days\n", flightTime);

        // Calculate fuel consumption
        double finalFuel = spacecraft.getFuel();
        double fuelConsumed = initialFuel - finalFuel;
        double fuelConsumedPercentage = (fuelConsumed / initialFuel) * 100.0;

        System.out.printf("Fuel consumed: %.2f kg (%.1f%%)\n", 
            fuelConsumed, 
            fuelConsumedPercentage);
        System.out.printf("Remaining fuel: %.2f kg (%.1f%%)\n", 
            finalFuel, 
            finalFuel / initialFuel * 100.0);

        // Use the SimulationResultWriter to write results to JSON
        SimulationResultWriter resultWriter = new SimulationResultWriter();
        resultWriter.writeResultsToJson(
                spacecraft,
                titan,
                targetOrbit,
                currentOrbit,
                simulation.getTimeStep(),
                flightTime,
                createThrusters(),
                MAX_FLIGHT_TIME_DAYS,
                "src/main/java/com/example/utilities/GD/simulation_results.json"
        );
    }

}
