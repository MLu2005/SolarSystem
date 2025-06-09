package com.example.lander;

public class OpenLoopController implements Controller {
    private static final double G_TITAN       = 1.352e-3;    // km/s²
    public  static final double U_MAX         = 10 * G_TITAN;
    private static final double BRAKE_ALT_V   = 2.0;        // km, start vertical brake
    private static final double BRAKE_ALT_H   = 20.0;        // km, start horizontal brake
    private static final double X_TOLERANCE   = 1e-4;        // km = 0.1 m

    /** clamp v into [min, max] */
    private double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    @Override
    public double getU(double t, double[] s) {
        double y  = s[1];
        double vY = s[3];

        // vertical open-loop as before:
        if (y > BRAKE_ALT_V)       return 0.0;
        if (y <= 0)                return 0.0;
        double aPlan = (vY < 0)
                     ? (vY * vY) / (2 * y)
                     : 0.0;
        double uPlan = G_TITAN + aPlan;
        return clamp(uPlan, 0.0, U_MAX);
    }

    @Override
    public double getV(double t, double[] s) {
        double x   = s[0];
        double vX  = s[2];
        double y   = s[1];
        double theta = s[4];
        double thetaDot = s[5];

        // 1) before horizontal-brake altitude, stay upright:
        if (y > BRAKE_ALT_H || Math.abs(vX) < 1e-6) {
            return -theta * 1.0   // simple PD to level out
                 - thetaDot * 2.0;
        }

        // 2) plan the required horizontal deceleration:
        //    vX^2 = 2 a Δx  →  a = vX^2/(2 x)  (sign matches vX)
        double aH = (vX * vX) / (2 * Math.max(Math.abs(x), X_TOLERANCE))
                  * (vX > 0 ? -1 : +1);

        // 3) compute the tilt angle α so that uPlan·sin(α)=aH
        double uPlan = getU(t, s);
        double sinAlpha = clamp(aH / uPlan, -1.0, 1.0);
        double desiredTilt = Math.asin(sinAlpha);

        // 4) PD-controller to swing into that tilt
        double Kp = 10.0, Kd = 5.0;
        double tiltError     = desiredTilt - theta;
        double tiltRateError = 0.0         - thetaDot;
        double vPlan = Kp * tiltError + Kd * tiltRateError;

        // 5) clamp to side-thruster limit
        return clamp(vPlan, -1.0, 1.0);
    }
}
