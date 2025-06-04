package com.example.lander;

public class FeedbackController implements Controller {
    private static final double G_TITAN = 1.352e-3;
    public static final double U_MAX = 10 * G_TITAN;
    public static final double V_MAX = 1.0;

    private final double KpHorizontal = 0.5;
    private final double KdHorizontal = 0.2;
    private final double MAX_TILT = Math.toRadians(45.0);

    private final double KpVertical = 0.8;
    private final double KdVertical = 0.5;

    private final double KpTilt = 20.0;
    private final double KdTilt = 10.0;

    @Override
    public double getU(double time, double[] stateVector) {
        double verticalPosition = stateVector[1];
        double verticalVelocity = stateVector[3];

        double verticalError = 0.0 - verticalPosition;
        double verticalVelocityError = 0.0 - verticalVelocity;

        double pdVertical = KpVertical * verticalError + KdVertical * verticalVelocityError;
        double desiredVerticalAcceleration = G_TITAN + pdVertical;

        if (desiredVerticalAcceleration < 0) {
            desiredVerticalAcceleration = 0;
        }
        if (desiredVerticalAcceleration > U_MAX) {
            desiredVerticalAcceleration = U_MAX;
        }
        return desiredVerticalAcceleration;
    }

    @Override
    public double getV(double time, double[] stateVector) {
        double horizontalPosition = stateVector[0];
        double horizontalVelocity = stateVector[2];
        double currentTilt = stateVector[4];
        double currentTiltRate = stateVector[5];

        double desiredTilt = - (KpHorizontal * horizontalPosition + KdHorizontal * horizontalVelocity);
        if (desiredTilt > MAX_TILT) {
            desiredTilt = MAX_TILT;
        }
        if (desiredTilt < -MAX_TILT) {
            desiredTilt = -MAX_TILT;
        }

        double tiltError = desiredTilt - currentTilt;
        double tiltRateError = 0.0 - currentTiltRate;
        double torqueCommand = KpTilt * tiltError + KdTilt * tiltRateError;

        if (torqueCommand > V_MAX) {
            torqueCommand = V_MAX;
        }
        if (torqueCommand < -V_MAX) {
            torqueCommand = -V_MAX;
        }
        return torqueCommand;
    }
}
