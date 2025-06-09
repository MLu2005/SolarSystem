package com.example.utilities.Ship;

import com.example.solar_system.CelestialBody;
import com.example.utilities.DataLoader;
import com.example.utilities.Vector3D;
import executables.Constants;
import executables.solvers.RK4Solver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * SpacecraftSimulation maintains the full “state” array of all CelestialBody(ies) plus one SpaceShip.
 * It uses an RK4 integrator to step forward by small ∆t, optionally applying the ship’s continuous or
 * instantaneous thrust.  In this Hill‐Climb scenario, we only ever apply “instantaneous” ΔV via
 * SpaceShip.applyImpulse(), so applyThrust is unused.  We still provide the hook for future expansion.
 */
public class SpacecraftSimulation {

    private final List<CelestialBody> bodyList = new ArrayList<>();
    private final Map<String, CelestialBody> bodyMap = new HashMap<>();

    private SpaceShip spacecraft;

    // The initial state
    private double[] initialStateArray;
    private double initialJulian;

    // The current state (mutable)
    private double[] currentState;
    private double currentJulian;

    /**
     * Constructor.  You pass in the FULL list of CelestialBody objects (including Titan),
     * plus the single SpaceShip (which is also a CelestialBody).  departureJulian is the
     * Julian date at which these bodies are “initialized.”
     *
     * @param bodies           All CelestialBody objects to simulate (e.g. Sun, Earth, Titan, etc.)
     * @param spacecraft       The SpaceShip (position/velocity/mass already set at departureJulian)
     * @param departureJulian  The JD at which initial positions/velocities are valid
     */
    public SpacecraftSimulation(List<CelestialBody> bodies,
                                SpaceShip spacecraft,
                                double departureJulian) {
        // 1) Copy all bodies into our list & map
        for (CelestialBody b : bodies) {
            bodyList.add(b);
            bodyMap.put(b.getName(), b);
        }
        // 2) Insert the spacecraft as well
        this.spacecraft = spacecraft;
        bodyList.add(spacecraft);
        bodyMap.put(spacecraft.getName(), spacecraft);

        // 3) Build the “initialStateArray” = [ (x,y,z,vx,vy,vz) for each body ] in index order
        int n = bodyList.size();
        initialStateArray = new double[n * 6];
        for (int i = 0; i < n; i++) {
            CelestialBody b = bodyList.get(i);
            Vector3D pos = b.getPosition();
            Vector3D vel = b.getVelocity();
            int idx = i * 6;
            initialStateArray[idx + 0] = pos.x;
            initialStateArray[idx + 1] = pos.y;
            initialStateArray[idx + 2] = pos.z;
            initialStateArray[idx + 3] = vel.x;
            initialStateArray[idx + 4] = vel.y;
            initialStateArray[idx + 5] = vel.z;
        }

        // Clone into currentState
        currentState = initialStateArray.clone();
        this.initialJulian = departureJulian;
        this.currentJulian = departureJulian;
    }


    /**
     * Advances the entire N‐body + spacecraft state by dt seconds.  If applyThrust=true,
     * you could compute a continuous‐thrust acceleration inside your derivative function;
     * but for this Hill‐Climb, we only apply “instantaneous” impulses via ship.applyImpulse(...)
     * outside of this step.  Therefore, applyThrust is unused here.
     *
     * @param dtSecs       Time step in seconds
     */
    public void step(double dtSecs) {
        // 1) Build a local copy of the state for RK4 input
        double[] stateForIntegrator = currentState.clone();
        double   t0                 = currentJulian;

        // 2) Define the derivative function f(t, state) -> dstate/dt
        BiFunction<Double, double[], double[]> derivative = (time, state) -> {
            int n = bodyList.size();
            double[] dstatedt = new double[n * 6];

            // Extract each body's position, velocity, and mass
            Vector3D[] posArray  = new Vector3D[n];
            Vector3D[] velArray  = new Vector3D[n];
            double[]   massArray = new double[n];
            for (int i = 0; i < n; i++) {
                int idx = i * 6;
                posArray[i]  = new Vector3D(state[idx + 0],
                        state[idx + 1],
                        state[idx + 2]);
                velArray[i]  = new Vector3D(state[idx + 3],
                        state[idx + 4],
                        state[idx + 5]);
                massArray[i] = bodyList.get(i).getMass();
            }

            // Compute d(state)/dt for each body
            for (int i = 0; i < n; i++) {
                int idx = i * 6;
                // dx/dt = vx, dy/dt = vy, dz/dt = vz
                dstatedt[idx + 0] = velArray[i].x;
                dstatedt[idx + 1] = velArray[i].y;
                dstatedt[idx + 2] = velArray[i].z;

                // dv/dt = sum over j≠i of G·M_j · (r_j - r_i)/|r_j - r_i|^3
                Vector3D accSum = new Vector3D(0.0, 0.0, 0.0);
                for (int j = 0; j < n; j++) {
                    if (i == j) continue;
                    Vector3D r_ij = posArray[j].subtract(posArray[i]);
                    double dist = r_ij.norm();
                    if (dist < 1e-12) continue;  // avoid singularity
                    double mu_j = Constants.G * massArray[j];
                    accSum = accSum.add(
                            r_ij.scale(mu_j / (dist * dist * dist))
                    );
                }
                dstatedt[idx + 3] = accSum.x;
                dstatedt[idx + 4] = accSum.y;
                dstatedt[idx + 5] = accSum.z;
            }

            return dstatedt;
        };

        // 3) Use RK4Solver.solveStep(...) for exactly one RK4 timestep
        RK4Solver solver = new RK4Solver();
        double[] nextState = solver.solveStep(derivative, t0, stateForIntegrator, dtSecs);

        // 4) Unpack nextState back into currentState and update each CelestialBody
        int nBodies = bodyList.size();
        for (int i = 0; i < nBodies; i++) {
            int idx = i * 6;
            // Copy the integrator’s result into currentState[]
            currentState[idx + 0] = nextState[idx + 0];
            currentState[idx + 1] = nextState[idx + 1];
            currentState[idx + 2] = nextState[idx + 2];
            currentState[idx + 3] = nextState[idx + 3];
            currentState[idx + 4] = nextState[idx + 4];
            currentState[idx + 5] = nextState[idx + 5];

            // Update the actual CelestialBody object
            CelestialBody b = bodyList.get(i);
            b.setPosition(new Vector3D(
                    currentState[idx + 0],
                    currentState[idx + 1],
                    currentState[idx + 2]
            ));
            b.setVelocity(new Vector3D(
                    currentState[idx + 3],
                    currentState[idx + 4],
                    currentState[idx + 5]
            ));
        }

        // 5) Advance the Julian date by dtSecs (converted to days)
        currentJulian += dtSecs / 86400.0;
    }


    /** Returns a StateVector (pos, vel, orientation, mass) for the SpaceShip. */
    public com.example.utilities.Ship.StateVector getSpaceShipState() {
        if (spacecraft == null) return null;
        return spacecraft.getStateVector();
    }

    /** Returns a StateVector for any named CelestialBody (e.g. "Titan", "Saturn", etc.). */
    public com.example.utilities.Ship.StateVector getBodyState(String name) {
        CelestialBody body = bodyMap.get(name);
        if (body == null) return null;
        return body.getStateVector();
    }
}
