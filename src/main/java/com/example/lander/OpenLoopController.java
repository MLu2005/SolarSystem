package com.example.lander;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class OpenLoopController implements Controller {
    private static final double G_TITAN = 1.352e-3;
    public static final double U_MAX = 10 * G_TITAN;
    public static final double V_MAX = 1.0;
    private static final double K_P_THETA = 10.0;
    private static final double K_D_THETA = 5.0;

    @Override
    public double getU(double time, double[] stateVector) {
        double thrust = G_TITAN;
        return min(max(thrust, 0.0), U_MAX);
    }

    @Override
    public double getV(double time, double[] stateVector) {
        double angle = stateVector[4];
        double angularRate = stateVector[5];

        double rotationAcceleration = -(K_P_THETA * angle + K_D_THETA * angularRate);
        if (rotationAcceleration > V_MAX) {
            rotationAcceleration = V_MAX;
        }
        if (rotationAcceleration < -V_MAX) {
            rotationAcceleration = -V_MAX;
        }
        return rotationAcceleration;
    }
}