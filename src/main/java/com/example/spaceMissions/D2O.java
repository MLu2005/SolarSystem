package com.example.spaceMissions;

import com.example.solarSystem.Vector3D;
import org.jetbrains.annotations.NotNull;

import java.lang.Math;

public class D2O {

    private String name;
    private Vector3D object1;
    private Vector3D object2;
    private double distance;

    public D2O (Vector3D object1, Vector3D object2) {
        this.object1 = object1;
        this.object2 = object2;
        this.distance = getDistance(object1, object2);

    }
    public Vector3D getObject1() {
        return object1;
    }

    public void setObject1(Vector3D object1) {
        this.object1 = object1;
    }
    public Vector3D getObject2() {
        return object2;
    }

    public void setObject2(Vector3D object2) {
        this.object2 = object2;
    }

    /**
     *this method takes the 3D vector of 2 celestial bodies as parameters and extracts the
     x, y, z coordinates of the 2 bodies and calculates the exact Euclidean distance using the Pythagoras rule

     @param object1 any planet, moon, or star
     @param object2 any other planet, moon, or star

     */

    public double getDistance(@NotNull Vector3D object1, @NotNull Vector3D object2) {

        double xDistance = Math.abs(object1.x - object2.x);
        double yDistance = Math.abs(object1.y - object2.y);
        double zDistance = Math.abs(object1.z - object2.z);

        double sDD = xDistance * xDistance + yDistance * yDistance;

        return Math.sqrt(sDD + Math.pow(zDistance, 2));
    }
}
