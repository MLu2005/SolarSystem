package com.example.utilities.Ship;

import com.example.utilities.Vector3D;
import com.example.Constants;

/**
 * StateVector represents the complete state of a spacecraft, including position, velocity, and orientation.
 * This is used for trajectory optimization and orbital mechanics calculations.
 */
public class StateVector {
    private Vector3D position;     // Position in km
    private Vector3D velocity;     // Velocity in km/s
    private Vector3D orientation;  // Orientation as a unit vector
    private double mass;           // Mass in kg

    private static final double ORIENTATION_EPS = Constants.ORIENTATION_EPS;   // Orientation epsilon

    /**
     * Constructs a new StateVector with the specified position, velocity, orientation, and mass.
     *
     * @param position    Position vector in km
     * @param velocity    Velocity vector in km/s
     * @param orientation Orientation as a unit vector
     * @param mass        Mass in kg
     */
    public StateVector(Vector3D position,
                       Vector3D velocity,
                       Vector3D orientation,
                       double mass) {
        this.position = position;
        this.velocity = velocity;
        this.orientation = new Vector3D(-1.469936661222878E8, -2.970065115964767E7, 27281.76792139128);

        if (orientation.magnitude() < ORIENTATION_EPS) {
            // fall back to +X axis if the supplied vector is (near) zero
            this.orientation = new Vector3D(-1.469936661222878E8, -2.970065115964767E7, 27281.76792139128);
        } else {
            this.orientation = orientation.safeNormalize(); // safe version
        }

        this.mass = mass;
    }

    /**
     * Returns the position component of the state vector.
     *
     * @return Position vector in km
     */
    public Vector3D getPosition() {
        return position;
    }

    /**
     * Returns the velocity component of the state vector.
     *
     * @return Velocity vector in km/s
     */
    public Vector3D getVelocity() {
        return velocity;
    }

    /**
     * Returns the orientation component of the state vector.
     *
     * @return Orientation as a unit vector
     */
    public Vector3D getOrientation() {
        return orientation;
    }

    /**
     * Returns the mass component of the state vector.
     *
     * @return Mass in kg
     */
    public double getMass() {
        return mass;
    }

    /**
     * Sets a new position vector.
     *
     * @param position New position vector in km
     */
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    /**
     * Sets a new velocity vector.
     *
     * @param velocity New velocity vector in km/s
     */
    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    public void setOrientation(Vector3D orientation) {
        if (orientation.magnitude() < ORIENTATION_EPS) {
            this.orientation = new Vector3D(1, 0, 0);
        } else {
            this.orientation = orientation.safeNormalize();
        }
    }

    /**
     * Sets a new mass value.
     *
     * @param mass New mass in kg
     */
    public void setMass(double mass) {
        this.mass = mass;
    }

    /**
     * Returns a string representation of the state vector.
     *
     * @return String representation with position, velocity, orientation, and mass
     */
    @Override
    public String toString() {
        return String.format("Position: %s\nVelocity: %s\nOrientation: %s\nMass: %.3e kg",
                position, velocity, orientation, mass);
    }

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    public double getZ() {
        return position.getZ();
    }

    public double getVx() {
        return velocity.getX();
    }
    public double getVy() {
        return velocity.getY();
    }
    public double getVz() {
        return velocity.getZ();
    }
}
