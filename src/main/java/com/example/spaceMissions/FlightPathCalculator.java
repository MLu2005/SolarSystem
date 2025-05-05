package com.example.spaceMissions;

import com.example.solarSystem.Vector3D;
import org.jetbrains.annotations.NotNull;

public class FlightPathCalculator {

    //nothing more to see here yet, I am just adding structures that will be useful in th future

    //Create a stochastic hill climbing algorithm, that should be the most optimal :)
    //use the distance as a baseline around, which the algorithm should work around

    /**
     *this method takes the 3D vector of 2 celestial bodies as parameters and extracts the
     x, y, z coordinates of the 2 bodies and calculates the exact Euclidean distance using the Pythagoras rule

     @param object1 any planet, moon, or star
     @param object2 any other planet, moon, or star

     */

    public double getEuclideanDistance(@NotNull Vector3D object1, @NotNull Vector3D object2) {

        double xDistance = Math.abs(object1.x - object2.x);
        double yDistance = Math.abs(object1.y - object2.y);
        double zDistance = Math.abs(object1.z - object2.z);

        double sDD = xDistance * xDistance + yDistance * yDistance;

        return Math.sqrt(sDD + Math.pow(zDistance, 2));
    }
}
