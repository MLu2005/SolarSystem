package com.example.lander;

public interface Controller {
    double getU(double t, double[] state);
    double getV(double t, double[] state);
}
