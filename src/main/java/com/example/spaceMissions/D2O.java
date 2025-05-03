package com.example.spaceMissions;

import com.example.solarSystem.Vector3D;
import java.lang.Math;

public class D2O {

    private Vector3D object1;
    private Vector3D object2;

    public D2O (Vector3D object1, Vector3D object2) {
        this.object1 = object1;
        this.object2 = object2;

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

    public double getDistance(Vector3D object1, Vector3D object2) {

        double xDistance = Math.abs(object1.x - object2.x);
        double yDistance = Math.abs(object1.y - object2.y);
        double zDistance = Math.abs(object1.z - object2.z);

        double sDD = xDistance * xDistance + yDistance * yDistance;
        double distance = Math.sqrt(sDD + Math.pow(zDistance, 2));

        return distance;
    }
}
