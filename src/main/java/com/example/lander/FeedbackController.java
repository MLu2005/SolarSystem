package com.example.lander;

public class FeedbackController implements Controller {
    public static final double U_MAX = 10 * 1.352e-3;
    public static final double V_MAX = 1.0;

    private final double KpHorizontal = 0.5;
    private final double KdHorizontal = 0.2;
    private final double MAX_TILT = Math.toRadians(45.0);

    private final double KpVertical = 0.6;
    private final double KdVertical = 0.3;

    private final double KpTilt = 20.0;
    private final double KdTilt = 10.0;

    @Override
    public double getU(double time, double[] state) {
        double vY = state[3];
        double velError = 0 - vY;
        if (Math.abs(velError) < 1e-4) velError = 0;
        double uCorr = KpVertical * velError;
        return clamp(uCorr, -U_MAX, U_MAX);
    }

    @Override
    public double getV(double time, double[] state) {
        double y        = state[1];      // km
        double thetaDot = state[5];      // rad/s

        // === Landing‐brake override ===
        // If we’re under 0.01 km (10 m) altitude, just kill any rotation
        if (y <= 0.02) {
            // apply full torque opposite to the current spin
            return -Math.signum(thetaDot) * V_MAX;
        }

        // === Otherwise, your normal tilt controller ===
        double x          = state[0];
        double vX         = state[2];
        double theta      = state[4];

        // desired tilt to brake horizontal error
        double desiredTilt = - (KpHorizontal * x + KdHorizontal * vX);
        desiredTilt = Math.max(-MAX_TILT, Math.min(MAX_TILT, desiredTilt));

        // PD for tilt angle
        double tiltError     = desiredTilt - theta;
        double tiltRateError = 0.0 - thetaDot;
        double torque = KpTilt * tiltError + KdTilt * tiltRateError;

        // clamp
        if (torque >  V_MAX) torque =  V_MAX;
        if (torque < -V_MAX) torque = -V_MAX;
        return torque;
    }

    private double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }
}