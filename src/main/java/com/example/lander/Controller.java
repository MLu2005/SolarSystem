package com.example.lander;

public interface Controller {
    /**
     * @param t     current simulation time (s)
     * @param state array of length 6: [x, y, xDot, yDot, theta, thetaDot]
     * @return desired main‐engine acceleration (km/s²)
     */
    double getU(double t, double[] state);

    /**
     * @param t     current simulation time (s)
     * @param state array of length 6: [x, y, xDot, yDot, theta, thetaDot]
     * @return desired rotational acceleration (rad/s²)
     */
    double getV(double t, double[] state);
}
