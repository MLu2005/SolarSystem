// src/com/example/lander/LanderODE.java
package com.example.lander;

import java.util.function.BiFunction;

import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.Vector3D;
import com.example.utilities.titanAtmosphere.AtmosphericForce;
import com.example.utilities.titanAtmosphere.TitanEnvironment;

public class LanderODE implements BiFunction<Double,double[],double[]> {
    private static final double G_TITAN = 1.352e-3;

    private final Controller controller;
    private final AtmosphericForce dragModel;
    private final SpaceShip tempShip;

    public LanderODE(Controller controller,
                     TitanEnvironment env,
                     double dragC,
                     double maxAtm,
                     double massKg) {
        this.controller = controller;
        this.tempShip   = new SpaceShip("LanderDummy", 0.0,
                                        new Vector3D(0,0,0),
                                        massKg, 0.0,
                                        new Vector3D(0,0,0));
        this.dragModel  = new AtmosphericForce(env, dragC, maxAtm);
    }

    @Override
    public double[] apply(Double time, double[] s) {
        double x = s[0], y = s[1],
               vX= s[2], vY= s[3],
               θ = s[4], θdot = s[5];

        tempShip.setPosition(new Vector3D(x, y, 0));
        tempShip.setVelocity(new Vector3D(vX, vY, 0));
        Vector3D drag = dragModel.compute(tempShip);
        double m = tempShip.getMass();

        double aDragX = drag.getX()/m;
        double aDragY = drag.getY()/m;

        double u = controller.getU(time, s);
        double v = controller.getV(time, s);

        double[] d = new double[6];
        d[0] = vX;
        d[1] = vY;
        d[2] = u * Math.sin(θ) + aDragX;
        // vertical: clamp upward pop at <0.1 m
        double netY = u * Math.cos(θ) - G_TITAN + aDragY;
        if (y < 1e-4 && netY > 0) netY = 0;
        d[3] = netY;
        d[4] = θdot;
        d[5] = v;
        return d;
    }
}
