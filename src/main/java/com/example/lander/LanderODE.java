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

    public LanderODE(Controller controller,
                     TitanEnvironment environment,
                     double dragCoefficient,
                     double maxAtmosphereHeight,
                     double landerMassKilograms) {

        this.controller = controller;
        Vector3D initialPosition = new Vector3D(0, 0, 0);
        Vector3D initialVelocity = new Vector3D(0, 0, 0);
        this.tempShip = new SpaceShip(
                "LanderDummy",
                0.0,
                initialVelocity,
                landerMassKilograms,
                0.0,
                initialPosition
        );
        this.dragModel = new AtmosphericForce(environment, dragCoefficient, maxAtmosphereHeight);
    }

    @Override
    public double[] apply(Double time, double[] stateVector) {
        double horizontalPosition = stateVector[0];
        double verticalPosition = stateVector[1];
        double horizontalVelocity = stateVector[2];
        double verticalVelocity = stateVector[3];
        double tiltAngle = stateVector[4];
        double tiltRate = stateVector[5];

        Vector3D shipPosition3D = new Vector3D(horizontalPosition, verticalPosition, 0);
        Vector3D shipVelocity3D = new Vector3D(horizontalVelocity, verticalVelocity, 0);
        tempShip.setPosition(shipPosition3D);
        tempShip.setVelocity(shipVelocity3D);

        Vector3D dragForce = dragModel.compute(tempShip);
        double massKilograms = tempShip.getMass();
        double dragAccelerationX = dragForce.getX() / massKilograms;
        double dragAccelerationY = dragForce.getY() / massKilograms;

        double thrustAcceleration = controller.getU(time, stateVector);
        double rotationAcceleration = controller.getV(time, stateVector);

        double[] derivatives = new double[6];
        derivatives[0] = horizontalVelocity;
        derivatives[1] = verticalVelocity;
        derivatives[2] = thrustAcceleration * Math.sin(tiltAngle) + dragAccelerationX;
        derivatives[3] = thrustAcceleration * Math.cos(tiltAngle) - G_TITAN + dragAccelerationY;
        derivatives[4] = tiltRate;
        derivatives[5] = rotationAcceleration;

        return derivatives;
    }
}