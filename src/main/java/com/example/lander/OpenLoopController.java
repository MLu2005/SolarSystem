package com.example.lander;

public class OpenLoopController {
    private static final int MAX_STEPS = 10000;
    private static double[] timeStamps = new double[MAX_STEPS];
    private static double[] uProfile   = new double[MAX_STEPS];
    private static double[] vProfile   = new double[MAX_STEPS];
    private static int N;

    /**
     * Precompute piecewise-constant thrust/torque profile.
     *
     * @param x0     initial horizontal offset (m)
     * @param y0     initial altitude            (m)
     * @param xdot0  initial horizontal speed   (m/s)
     * @param ydot0  initial vertical speed     (m/s)
     */
    public static void initialize(double x0, double y0,
                                  double xdot0, double ydot0)
    {
        N = 0;
        double gTitan = 1.352;
        double uMax = 10.0 * gTitan;
        double vMax = 1.0;

        // PHASE 1: Kill horizontal velocity by tilting & thrusting
        double thetaKill = Math.asin(clamp(-xdot0 / uMax, -1.0, 1.0));
        double tTurn     = Math.abs(thetaKill) / vMax;
        double dt        = 0.01;

        // 1a) Rotate from theta=0 to thetaKill (no thrust)
        for (double t = 0.0; t < tTurn && N < MAX_STEPS; t += dt) {
            timeStamps[N] = t;
            uProfile[N]   = 0.0;
            vProfile[N]   = Math.signum(thetaKill) * vMax;
            N++;
        }

        // 1b) Thrust at angle = thetaKill to decelerate horizontally
        double aXkill = uMax * Math.sin(thetaKill);
        double tDecelX = Math.abs(xdot0 / aXkill);
        double tStart1b = timeStamps[N-1] + dt;
        for (double t = tStart1b; t < tStart1b + tDecelX && N < MAX_STEPS; t += dt) {
            timeStamps[N] = t;
            uProfile[N]   = uMax;
            vProfile[N]   = 0.0; // hold angle constant
            N++;
        }

        // PHASE 2: Rotate back to vertical (θ=0)
        double tTurnBack  = Math.abs(thetaKill) / vMax;
        double tStart2    = timeStamps[N-1] + dt;
        for (double t = tStart2; t < tStart2 + tTurnBack && N < MAX_STEPS; t += dt) {
            timeStamps[N] = t;
            uProfile[N]   = 0.0;
            vProfile[N]   = -Math.signum(thetaKill) * vMax;
            N++;
        }

        // PHASE 3: Pure vertical descent from y≈y0 to y=0
        // Solve ÿ = u - gTitan with initial y=y0, ẏ≈0, final y=0,ẏ=0
        double y1    = y0;
        double T_des = 1.5 * Math.sqrt(2.0 * y1 / gTitan);
        double aY    = -2.0 * y1 / (T_des * T_des);
        double uVert = clamp(aY + gTitan, 0.0, uMax);
        double tStart3 = timeStamps[N-1] + dt;
        for (double t = tStart3; t < tStart3 + T_des && N < MAX_STEPS; t += dt) {
            timeStamps[N] = t;
            uProfile[N]   = uVert;
            vProfile[N]   = 0.0;
            N++;
        }
    }

    /** Return u(t). */
    public static double lookupU(double t) {
        if (N == 0) return 0.0;
        if (t <= timeStamps[0]) return uProfile[0];
        for (int i = 0; i < N - 1; i++) {
            if (t >= timeStamps[i] && t < timeStamps[i+1]) {
                return uProfile[i];
            }
        }
        return 0.0;
    }

    /** Return v(t). */
    public static double lookupV(double t) {
        if (N == 0) return 0.0;
        if (t <= timeStamps[0]) return vProfile[0];
        for (int i = 0; i < N - 1; i++) {
            if (t >= timeStamps[i] && t < timeStamps[i+1]) {
                return vProfile[i];
            }
        }
        return 0.0;
    }

    private static double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }
}