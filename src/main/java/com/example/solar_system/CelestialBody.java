package com.example.solar_system;

import com.example.utilities.Ship.StateVector;
import com.example.utilities.Vector3D;
import javafx.scene.Node;

/**
 * CelestialBody represents a physical object in space such as a planet or a star.
 * It stores basic physical properties: name, mass, position, velocity, and acceleration.
 */
public class CelestialBody {
    private final String name;
    private final double mass; // in kilograms
    private Vector3D position; // in kilometers
    private Vector3D velocity; // in km/s
    private Vector3D acceleration; // in km/s^2


    private transient Node node;

    /**
     * Constructs a new CelestialBody with initial position and velocity.
     * Acceleration is set to zero by default.
     */
    public CelestialBody(String name, double mass, Vector3D position, Vector3D velocity) {
        this.name = name;
        this.mass = mass;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = Vector3D.zero(); // default is 0
    }

    // Getters

    /** Returns the name of the celestial body. */
    public String getName() {
        return name;
    }

    /** Returns the mass in kilograms. */
    public double getMass() {
        return mass;
    }

    /** Returns the current position vector (km). */
    public Vector3D getPosition() {
        return position;
    }

    /** Returns the current velocity vector (km/s). */
    public Vector3D getVelocity() {
        return velocity;
    }

    /** Returns the current acceleration vector (km/s²). */
    public Vector3D getAcceleration() {
        return acceleration;
    }

    // Setters

    /** Sets a new position vector. */
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    /** Sets a new velocity vector. */
    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    /** Sets a new acceleration vector. */
    public void setAcceleration(Vector3D acceleration) {
        this.acceleration = acceleration;
    }

    /** Returns a string representation of the body with mass, position, and velocity. */
    @Override
    public String toString() {
        return String.format("%s\nMass: %.3e kg\nPos: %s\nVel: %s\n", name, mass, position, velocity);
    }

    public StateVector getState() {
        return new StateVector(position, velocity, Vector3D.zero(), mass);
    }
}
