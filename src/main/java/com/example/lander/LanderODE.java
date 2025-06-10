package com.example.lander;

import java.util.function.BiFunction;

import com.example.utilities.Ship.SpaceShip;
import com.example.utilities.Vector3D;
import com.example.utilities.titanAtmosphere.AtmosphericForce;
import com.example.utilities.titanAtmosphere.TitanEnvironment;

public class LanderODE implements BiFunction<Double, double[], double[]> {
    private static final double G_TITAN = 1.352e-3;

    private final Controller controller;
    private final AtmosphericForce dragModel;
    private final SpaceShip tempShip;

    public LanderODE(Controller controller, TitanEnvironment environment, double dragCoefficient, double maxAtmosphere, double massKg) {
        this.controller = controller;
        this.tempShip = new SpaceShip("Noah Ark",0.0, new Vector3D(0, 0, 0), massKg,0.0, new Vector3D(0, 0, 0));
        this.dragModel = new AtmosphericForce(environment, dragCoefficient, maxAtmosphere);
    }

    @Override
    public double[] apply(Double time, double[] state) {
        double horizontalPosition = state[0];
        double altitude = state[1];
        double horizontalVelocity = state[2];
        double verticalVelocity = state[3];
        double tiltAngle = state[4];
        double tiltRate = state[5];

        tempShip.setPosition(new Vector3D(horizontalPosition, altitude, 0));
        tempShip.setVelocity(new Vector3D(horizontalVelocity, verticalVelocity, 0));
        Vector3D dragForce = dragModel.compute(tempShip);
        double mass = tempShip.getMass();

        double dragAccelerationX = dragForce.getX() / mass;
        double dragAccelerationY = dragForce.getY() / mass;

        double thrust = controller.getU(time, state);
        double torque = controller.getV(time, state);

        double[] derivatives = new double[6];
        derivatives[0] = horizontalVelocity;
        derivatives[1] = verticalVelocity;
        derivatives[2] = thrust * Math.sin(tiltAngle) + dragAccelerationX;

        double netVerticalAcceleration = thrust * Math.cos(tiltAngle) - G_TITAN + dragAccelerationY;
        if (altitude < 1e-4 && netVerticalAcceleration > 0) {
            netVerticalAcceleration = 0;
        }
        derivatives[3] = netVerticalAcceleration;

        derivatives[4] = tiltRate;
        derivatives[5] = torque;
        return derivatives;
    }
}
