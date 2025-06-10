package com.example.lander;

public class OpenLoopController implements Controller {
    private static final double TITAN_GRAVITY = 1.352e-3;
    public static final double MAX_THRUST = 10 * TITAN_GRAVITY;
    private final double VERTICAL_BRAKING_ALTITUDE;
    private final double HORIZONTAL_BRAKING_ALTITUDE;
    private static final double POSITION_TOLERANCE = 1e-4;

    public OpenLoopController(double VERTICAL_BRAKING_ALTITUDE, double HORIZONTAL_BRAKING_ALTITUDE) {
        this.VERTICAL_BRAKING_ALTITUDE = VERTICAL_BRAKING_ALTITUDE;
        this.HORIZONTAL_BRAKING_ALTITUDE = HORIZONTAL_BRAKING_ALTITUDE;
    }

    @Override
    public double getU(double time, double[] state) {
        double currentAltitude = state[1];
        double verticalVelocity = state[3];

        if (currentAltitude > this.VERTICAL_BRAKING_ALTITUDE || currentAltitude <= 0.0) {
            return 0.0;
        }

        double plannedDeceleration;
        if (verticalVelocity < 0.0) {
            plannedDeceleration = (verticalVelocity * verticalVelocity) / (2 * currentAltitude);
        } else {
            plannedDeceleration = 0.0;
        }

        double plannedThrust = TITAN_GRAVITY + plannedDeceleration;
        return clampToRange(plannedThrust, 0.0, MAX_THRUST);
    }

    @Override
    public double getV(double time, double[] state) {
        double horizontalPosition = state[0];
        double horizontalVelocity = state[2];
        double currentAltitude = state[1];
        double currentTiltAngle = state[4];
        double currentTiltRate = state[5];

        if (currentAltitude > this.HORIZONTAL_BRAKING_ALTITUDE || Math.abs(horizontalVelocity) < 1e-6) {
            return -currentTiltAngle - 2.0 * currentTiltRate;
        }

        double requiredDeceleration = (horizontalVelocity * horizontalVelocity)
            / (2 * Math.max(Math.abs(horizontalPosition), POSITION_TOLERANCE))
            * Math.signum(-horizontalVelocity);

        double currentThrust = getU(time, state);
        double sineOfDesiredTilt = clampToRange(requiredDeceleration / currentThrust, -1.0, 1.0);
        double desiredTiltAngle = Math.asin(sineOfDesiredTilt);

        double tiltProportionalGain = 10.0;
        double tiltDerivativeGain = 5.0;
        double tiltAngleError = desiredTiltAngle - currentTiltAngle;
        double tiltRateError = -currentTiltRate;
        
        double plannedTorque = tiltProportionalGain * tiltAngleError
                            + tiltDerivativeGain * tiltRateError;

        return clampToRange(plannedTorque, -1.0, 1.0);
    }

    private double clampToRange(double value, double minimum, double maximum) {
        if (value < minimum) {
            return minimum;
        }
        return Math.min(value, maximum);
    }
}