package com.example.solar_system;

import com.example.utilities.Vector3D;

import java.util.List;

public class BurnManager {

    private boolean burnApplied = false;
    private double burnMagnitude; // m/s
    private String targetBodyName;
    private double triggerDistance; //  meters

    public BurnManager(double burnMagnitude, String targetBodyName, double triggerDistance) {
        this.burnMagnitude = burnMagnitude;
        this.targetBodyName = targetBodyName;
        this.triggerDistance = triggerDistance;
    }

    public void tryApplyBurn(List<CelestialBody> bodies, String spaceshipName) {
        if (burnApplied) {
            System.out.println("Burn already applied.");
            return;
        }

        CelestialBody spaceship = findBody(bodies, spaceshipName);
        CelestialBody target = findBody(bodies, targetBodyName);

        if (spaceship == null || target == null) {
            System.out.println("Could not find target or spaceship.");
            return;
        }

        double distance = spaceship.getPosition().subtract(target.getPosition()).magnitude();
        System.out.println("Distance to target: " + distance);

        if (distance < triggerDistance) {
            Vector3D relativeVelocity = spaceship.getVelocity().subtract(target.getVelocity());
            Vector3D burnDirection = relativeVelocity.normalize().scale(-1);
            Vector3D deltaV = burnDirection.scale(burnMagnitude);
            spaceship.setVelocity(spaceship.getVelocity().add(deltaV));
            burnApplied = true;
            System.out.println("Burn applied to " + spaceshipName + " opposite relative velocity toward " + targetBodyName + " with Δv = " + deltaV);
        }
    }



    private CelestialBody findBody(List<CelestialBody> bodies, String name) {
        CelestialBody result = bodies.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (result == null) {
            System.out.println("Warning: Could not find celestial body named \"" + name + "\".");
            System.out.println("Available names:");
            bodies.forEach(b -> System.out.println(" - " + b.getName()));
        }

        return result;
    }

}
