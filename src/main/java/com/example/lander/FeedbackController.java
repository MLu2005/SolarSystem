package com.example.lander;

public class FeedbackController {
    // VERY small vertical‐loop gains
    private static final double kpY = 0.0001;
    private static final double kdY = 0.00005;

    // Horizontal gains
    private static final double kpX = 0.0005;
    private static final double kdX = 0.0002;

    // Attitude gains can remain the same
    private static final double kpTh = 10.0;
    private static final double kdTh = 5.0;

    /**
     * Compute vertical thrust u (m/s^2) from state:
     *   u ≈ gTitan + [ -kpY*(y) - kdY*(ydot) ].
     * Clamp to [0, uMax].
     */
    public static double computeU(double[] state, LandingModule lm) {
        double y    = state[1];
        double ydot = state[4];
        double eY   = y;     // target y=0
        double eYd  = ydot;  // target ydot=0
        double u_ff = 1.352;            // hover feed‐forward
        double u_fb = -kpY * eY - kdY * eYd;
        double uCmd = u_ff + u_fb;
        return clamp(uCmd, 0.0, lm.uMax);
    }

    /**
     * Compute desired pitch angle θ_des to produce horizontal acceleration aX:
     *   aX_des = -kpX*(x) - kdX*(xdot).
     *   θ_des = asin(clamp(aX_des / uCurrent, -1, +1)).
     */
    public static double computeDesiredTheta(double[] state, double uCurrent) {
        double x    = state[0];
        double xdot = state[3];
        double aXdes = -kpX * x - kdX * xdot;
        if (uCurrent <= 1e-6) {
            return 0.0; // no thrust → keep upright
        }
        double ratio = clamp(aXdes / uCurrent, -1.0, 1.0);
        return Math.asin(ratio);
    }

    /**
     * Compute angular accel v (rad/s^2) to drive θ → θ_des:
     *   eTh  = normalize(theta - θ_des), eThd = thetadot.
     *   vCmd = -kpTh*eTh - kdTh*eThd, clamped to [−vMax, +vMax].
     */
    public static double computeV(double[] state, double uCurrent, LandingModule lm) {
        double theta    = state[2];
        double thetadot = state[5];
        double thetaDes = computeDesiredTheta(state, uCurrent);
        double eTh   = normalizeAngle(theta - thetaDes);
        double eThd  = thetadot; // target 0
        double vCmd  = -kpTh * eTh - kdTh * eThd;
        return clamp(vCmd, -lm.vMax, lm.vMax);
    }

    private static double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    /** Normalize angle to [−π, +π]. */
    private static double normalizeAngle(double ang) {
        double a = (ang + Math.PI) % (2.0 * Math.PI);
        if (a < 0) a += 2.0 * Math.PI;
        return a - Math.PI;
    }
}
