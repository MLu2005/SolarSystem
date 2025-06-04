package com.example.lander;

import com.example.solar_system.CelestialBody;
import com.example.spaceMissions.LandingCondition;
import com.example.spaceMissions.SpaceShip;
import com.example.utilities.Vector3D;
import com.example.utilities.titanAtmosphere.AtmosphericForce;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetWindGrid;
import com.example.utilities.titanAtmosphere.TitanEnvironment;

public class LandingTest {
    public static void main(String[] args) {
        // (1) Build Titan’s CelestialBody at origin so grids work:
        CelestialBody titan = new CelestialBody(
            "Titan",
            1.3452e23,
            new Vector3D(0.0, 0.0, 0.0), // initialPosition
            new Vector3D(0.0, 0.0, 0.0)  // initialVelocity
        );
        // If your CelestialBody has a setter for position instead, call:
        // titan.setPosition(new Vector3D(0,0,0));

        // (2) Build surface & height grids (10 km cells)
        PlanetSurfaceGrid surfaceGrid = new PlanetSurfaceGrid(titan, 10_000.0);
        PlanetHeightGrid  heightGrid  = new PlanetHeightGrid(surfaceGrid);
        heightGrid.generateFlatTerrain(0.0); // flat at y=0

        // (3) Build wind grid & set zero wind
        PlanetWindGrid windGrid = new PlanetWindGrid(surfaceGrid);
        windGrid.generateConstantWind(new Vector3D(0.0, 0.0, 0.0));

        // (4) Build TitanEnvironment from those two
        TitanEnvironment env = new TitanEnvironment(heightGrid, windGrid);

        // (5) AtmosphericForce: same dragCoefficient & maxAtmosAltitude as before
        double dragCoeff    = 0.001;
        double maxAtmosAlt  = 70_000.0;
        AtmosphericForce aeroForce = new AtmosphericForce(env, dragCoeff, maxAtmosAlt);

        // (6) Instantiate the 2D LandingModule
        double landerMass = 1500.0; // kg
        LandingModule lander = new LandingModule(
            landerMass,
            windGrid,
            surfaceGrid,
            heightGrid,
            env,
            dragCoeff,
            maxAtmosAlt
        );

        // (7) Build a “dummy” SpaceShip that LandingCondition will inspect.
        //     We assume SpaceShip has at least:
        //       - a constructor like SpaceShip(Vector3D position, Vector3D velocity)
        //       - setPosition(Vector3D), setVelocity(Vector3D)
        //       - getPosition(), getVelocity().
        //     If your SpaceShip needs more parameters, adjust accordingly.
        SpaceShip ship = new SpaceShip(
            "Lander",                // String name or id
            landerMass,              // double mass
            new Vector3D(0.0, 0.0, 0.0), // Vector3D position
            0.0,                     // double fuel or other double parameter
            0.0,                     // double another parameter (e.g., orientation)
            new Vector3D(0.0, 0.0, 0.0)  // Vector3D velocity
        );

        // (8) Instantiate LandingCondition: altitudeTolerance = 1 m, maxLandingSpeed = 2 m/s
        double altitudeTol   = 1.0;  // m above terrain
        double maxLandSpeed  = 2.0;  // m/s
        LandingCondition lc = new LandingCondition(env, maxLandSpeed, altitudeTol);

        // (9) Choose initial 2D state for LandingModule:
        double x0         =  50.0;   // 50 m east of pad in 2D
        double y0         = 500.0;   // 500 m above ground in 2D
        double theta0     =   0.0;   // upright
        double xdot0      =  -5.0;   // m/s toward pad
        double ydot0      =   0.0;   // m/s vertical
        double thetadot0  =   0.0;   // rad/s

        lander.setState(x0, y0, theta0, xdot0, ydot0, thetadot0);

        // (10) If we ever declare “safe to land” via LandingCondition,
        //      we’ll print out those original initial values and stop.
        //      We’ll keep track of them now:
        final double initialX       = x0;
        final double initialY       = y0;
        final double initialTheta   = theta0;
        final double initialXdot    = xdot0;
        final double initialYdot    = ydot0;
        final double initialThetadot= thetadot0;

        // (11) Simulation loop
        double t   = 0.0;
        double dt  = 0.1;     // 0.1 s
        int maxSteps = 200_000;
        boolean useFeedback = true; // close‐loop vs open‐loop

        System.out.println("Starting landing simulation with 3D LandingCondition checks...");
        for (int step = 0; step < maxSteps; step++) {
            // (a) Get current 2D state:
            double[] s2D = lander.getState();
            double sx    = s2D[0];
            double sy    = s2D[1];
            double stheta= s2D[2];
            double svx   = s2D[3];
            double svy   = s2D[4];
            double sw    = s2D[5];

            // (b) Build a 3D position + velocity out of the 2D state.
            //     We assume the “landing plane” is the X–Y plane in global coords,
            //     with no north‐south motion (Z=0). If your Titan pad is tilted,
            //     you would instead use the true padCenter + basis vectors.
            Vector3D pos3D = new Vector3D(sx, sy, 0.0);
            Vector3D vel3D = new Vector3D(svx, svy, 0.0);

            // (c) Update the SpaceShip’s position + velocity:
            ship.setPosition(pos3D);
            ship.setVelocity(vel3D);

            // (d) First check our 2D landing‐tolerance (x within δx, y within terrain, etc.)
            int status2D = lander.checkLanding();
            if (status2D != 0) {
                // 2D says “landed or crashed.” But before accepting, we also check isSafeLanding.
                if (status2D > 0 && lc.isSafeLanding(ship)) {
                    // BOTH 2D criteria AND 3D LandingCondition agree it’s safe:
                    System.out.printf(
                        "✅ 2D & 3D criteria agree: Safe landing at t=%.2f s%n", t
                    );
                    System.out.printf(
                        "   Returning initial values: x0=%.2f m, y0=%.2f m, θ0=%.2f rad, " +
                        "ẋ0=%.2f m/s, ẏ0=%.2f m/s, θ̇0=%.2f rad/s%n",
                        initialX, initialY, initialTheta, initialXdot, initialYdot, initialThetadot
                    );
                } else {
                    // Either 2D crashed, or 2D thinks OK but 3D landing condition says “too fast or too high.”
                    System.out.printf(
                        "❌ Landing failed at t=%.2f s: 2D status=%d, 3D safe?=%b%n",
                        t, status2D, lc.isSafeLanding(ship)
                    );
                }
                break;
            }

            // (e) Otherwise, still flying: compute controls and step forward
            double u, v;
            if (useFeedback) {
                u = FeedbackController.computeU(s2D, lander);
                v = FeedbackController.computeV(s2D, u, lander);
            } else {
                u = OpenLoopController.lookupU(t);
                v = OpenLoopController.lookupV(t);
            }

            lander.stepRKF45(dt, u, v);
            t += dt;

            // (f) Optional logging every 10 seconds of sim time:
            if (step % 100 == 0) {
                System.out.printf("t=%.1f s, x=%.1f m, y=%.1f m, x˙=%.2f m/s, 3D alt=%.1f m%n",
                                  t, sx, sy, svx, /* 3D altitude */ (pos3D.getY() - heightGrid.getAltitude(pos3D))
                );
            }
        }

        if (t >= dt * maxSteps) {
            System.out.println("⚠️  Simulation timed out without landing/crash.");
        }
    }
}