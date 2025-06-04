package com.example.lander;

import java.util.function.BiFunction;

import com.example.utilities.Vector3D;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetHeightGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetSurfaceGrid;
import com.example.utilities.titanAtmosphere.TerrainGenerator.PlanetWindGrid;
import com.example.utilities.titanAtmosphere.TitanEnvironment;

import executables.solvers.ODESolver;
import executables.solvers.RKF45Solver;

public class LandingModule {
    // State variables
    private double x;         // horizontal pos (m)
    private double y;         // vertical pos above ground (m)
    private double theta;     // pitch angle (rad), 0 = upright
    private double xdot;      // horizontal velocity (m/s)
    private double ydot;      // vertical velocity (m/s)
    private double thetadot;  // angular velocity (rad/s)

    // Physical constants
    private final double m;        // lander mass (kg)
    public  final double uMax;     // max main-thrust accel (m/s^2) = 10 * gTitan
    public  final double vMax;     // max angular accel (rad/s^2)
    private final double gTitan = 1.352; // Titan gravity (m/s^2)

    // Environment models
    private final PlanetWindGrid    windGrid;
    private final PlanetSurfaceGrid surfaceGrid;
    private final PlanetHeightGrid  heightGrid;
    private final TitanEnvironment  environment;         // provides getAltitude(...) and getWind(...)
    private final double dragCoefficient;               // aerodynamic drag constant (e.g. 0.001)
    private final double maxAtmosAltitude;              // m (e.g. 70000)

    // ODE solver for integrating one step at a time
    private final ODESolver solver = new RKF45Solver();
    private double[] currentState = new double[6];
    private double currentTime = 0.0;

    // Last commanded thrust, so feedback controllers can read it
    private double lastThrust = 0.0;

    /**
     * @param mass                  lander mass (kg)
     * @param windGrid              PlanetWindGrid instance
     * @param surfaceGrid           PlanetSurfaceGrid instance
     * @param heightGrid            PlanetHeightGrid instance
     * @param environment           TitanEnvironment (provides wind & altitude)
     * @param dragCoefficient       aerodynamic drag constant k
     * @param maxAtmosphereAltitude maximum altitude (m) where drag applies
     */
    public LandingModule(double mass, PlanetWindGrid windGrid, PlanetSurfaceGrid surfaceGrid, PlanetHeightGrid heightGrid, TitanEnvironment environment, double dragCoefficient, double maxAtmosphereAltitude){
        this.m = mass;
        this.uMax = 10.0 * gTitan;
        this.vMax = 1.0;
        this.windGrid = windGrid;
        this.surfaceGrid = surfaceGrid;
        this.heightGrid = heightGrid;
        this.environment = environment;
        this.dragCoefficient = dragCoefficient;
        this.maxAtmosAltitude  = maxAtmosphereAltitude;
    }

    /**
     * Initialize the 2D state (all in meters/seconds/radians).
     * @param x        horizontal offset from landing pad (m)
     * @param y        altitude above pad (m)
     * @param theta    pitch angle (rad)
     * @param xdot     horizontal velocity (m/s)
     * @param ydot     vertical velocity (m/s)
     * @param thetadot angular velocity (rad/s)
     */
    public void setState(double x, double y, double theta, double xdot, double ydot, double thetadot){
        this.x = x;
        this.y = y;
        this.theta = theta;
        this.xdot = xdot;
        this.ydot = ydot;
        this.thetadot = thetadot;
        this.currentTime  = 0.0;
        this.currentState = new double[]{ x, y, theta, xdot, ydot, thetadot };
    }

    /** Return a fresh copy of the 6‐state: [x, y, theta, xdot, ydot, thetadot]. */
    public double[] getState() {
        return new double[]{ x, y, theta, xdot, ydot, thetadot };
    }

    /** Last commanded main-thrust accel u (m/s^2). */
    public double getLastThrust() {
        return lastThrust;
    }

    /**
     * Advance the state by dt seconds, holding controls u and v constant over dt.
     * Uses RKF45 solver for one step.
     *
     * @param dt time increment (s)
     * @param u  main‐thrust acceleration (m/s^2), will be clamped to [0, uMax]
     * @param v  angular acceleration (rad/s^2), will be clamped to [−vMax, +vMax]
     */
    public void stepRKF45(double dt, double u, double v) {
        // Bind controls into the ODE function f(t, s).
        BiFunction<Double, double[], double[]> f = (t, s) -> dynamics(s, u, v);

        // Solve one RKF45 step from (currentTime, currentState)
        // initialStepSize = dt, steps = 1, no stop condition
        double[][] result = solver.solve(f, currentTime, currentState, dt, 1, null);

        // result length should be 2: row0 = initial, row1 = next
        if (result.length > 1) {
            double[] nextRow = result[1];
            // nextRow[0] = newTime, nextRow[1..6] = new state
            currentTime   = nextRow[0];
            this.x        = nextRow[1];
            this.y        = nextRow[2];
            this.theta    = nextRow[3];
            this.xdot     = nextRow[4];
            this.ydot     = nextRow[5];
            this.thetadot = nextRow[6];
            this.currentState = new double[]{
                x, y, theta, xdot, ydot, thetadot
            };
        }
    }

    /**
     * Compute the time‐derivative of the 6‐state given controls (u, v).
     * Input state s = [ x, y, theta, xdot, ydot, thetadot ].
     * Returns [ xdot, ydot, thetadot, xddot, yddot, thetaddot ].
     */
    private double[] dynamics(double[] s, double uCommand, double vCommand) {
        // Unpack state
        double sx      = s[0];
        double sy      = s[1];
        double stheta  = s[2];
        double svx     = s[3];
        double svy     = s[4];
        double sw      = s[5];

        // 1) Clamp controls
        double u = Math.max(0.0, Math.min(uCommand, uMax));
        double v = Math.max(-vMax, Math.min(vCommand, vMax));
        lastThrust = u;

        // 2) Compute global position (3D) from 2D (sx, sy)
        Vector3D globalPos = computeGlobalPositionFrom2D(sx, sy);

        // 3) Determine altitude above terrain
        double terrainAlt = heightGrid.getAltitude(globalPos);
        double altitude   = globalPos.getY() - terrainAlt;

        // 4) Get wind vector at globalPos
        Vector3D wind3D = environment.getWind(globalPos);

        // 5) Compute relative 2D velocity
        //    We ignore wind3D.z (north) because motion is purely in local X–Y plane.
        double wx = wind3D.getX();
        double wy = wind3D.getY();
        double relVx = svx - wx;
        double relVy = svy - wy;

        // 6) Compute drag force only if altitude ≤ maxAtmosAltitude
        double dragX = 0.0, dragY = 0.0;
        if (altitude <= maxAtmosAltitude) {
            double speed = Math.sqrt(relVx*relVx + relVy*relVy);
            if (speed > 1e-8) {
                // drag direction = normalize(relV)
                double dirX = relVx / speed;
                double dirY = relVy / speed;
                double forceMag = -dragCoefficient * speed * speed;
                dragX = dirX * forceMag;
                dragY = dirY * forceMag;
            }
        }

        // 7) Convert drag force to acceleration: aDrag = Fdrag / m
        double aDragX = dragX / m;
        double aDragY = dragY / m;

        // 8) Thrust components in 2D
        double ax_thrust = u * Math.sin(stheta);
        double ay_thrust = u * Math.cos(stheta);

        // 9) Net accelerations
        double xddot = ax_thrust + aDragX;
        double yddot = ay_thrust - gTitan + aDragY;
        double thetaddot = v;

        // 10) Return derivative
        return new double[]{ svx, svy, sw, xddot, yddot, thetaddot };
    }

    /**
     * Compute the module's 3D global position from its 2D (x, y) state.
     * Here we assume:
     *   - padCenterGlobal is (0, 0, 0) in global coords,
     *   - eastHat = (1, 0, 0),
     *   - upHat   = (0, 1, 0).
     *
     * In a full simulation, replace with actual Titan pad location & basis vectors.
     */
    private Vector3D computeGlobalPositionFrom2D(double sx, double sy) {
        return new Vector3D(sx, sy, 0.0);
    }

    /**
     * Check landing status:
     *   0  = still airborne,
     *  +1  = safe landing,
     *  -1  = crash/unsafe.
     *
     * Uses tolerances:
     *   δx  = 0.1 m
     *   δθ  = 0.02 rad
     *   εx  = 0.1 m/s
     *   εy  = 0.1 m/s
     *   εθ  = 0.01 rad/s
     */
    public int checkLanding() {
        double δx = 0.1;
        double δθ = 0.02;
        double εx = 0.1;
        double εy = 0.1;
        double εθ = 0.01;

        Vector3D globalPos = computeGlobalPositionFrom2D(this.x, this.y);
        double terrainAlt  = heightGrid.getAltitude(globalPos);

        if (y <= terrainAlt) {
            boolean okX  = Math.abs(x) <= δx;
            boolean okT  = Math.abs(normalizeAngle(theta)) <= δθ;
            boolean okVx = Math.abs(xdot) <= εx;
            boolean okVy = Math.abs(ydot) <= εy;
            boolean okW  = Math.abs(thetadot) <= εθ;
            return (okX && okT && okVx && okVy && okW) ? +1 : -1;
        }
        return 0;
    }

    /** Normalize any angle to [−π, +π]. */
    private double normalizeAngle(double ang) {
        double a = (ang + Math.PI) % (2.0 * Math.PI);
        if (a < 0) a += 2.0 * Math.PI;
        return a - Math.PI;
    }
}
