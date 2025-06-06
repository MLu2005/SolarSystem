package com.example.utilities;

import java.util.Vector;

/**
 * Vector3D represents a 3-dimensional vector used for positions, velocities, and accelerations
 * in the solar system simulation. Includes common vector operations like addition, normalization, and dot product.
 */
public class Vector3D {

    public static final Vector3D ZERO = new Vector3D(0, 0, 0);
    public double x, y, z;

    /**
     * Constructs a new 3D vector with specified x, y, and z values.
     */
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX(){
        return this.x;
    }

    public double getY(){
        return this.y;
    }

    public double getZ(){
        return this.z;
    }

    /**
     * Returns the zero vector (0, 0, 0).
     */
    public static Vector3D zero() {
        return new Vector3D(0, 0, 0);
    }

    /**
     * Returns a new vector that is the sum of this vector and another vector.
     */
    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    /**
     * Returns a new vector that is the difference between this vector and another vector.
     */
    public Vector3D subtract(Vector3D other) {
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    /**
     * Returns a new vector that is this vector scaled by a scalar value.
     */
    public Vector3D scale(double scalar) {
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    /**
     * Returns the magnitude (length) of the vector.
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns the squared magnitude of the vector.
     * This is more efficient than magnitude() when only comparing distances.
     */
    public double magnitudeSquared() {
        return x * x + y * y + z * z;
    }

    /**
     * Computes the Euclidean distance between this vector and another vector.
     */
    public double distanceTo(Vector3D other) {
        return this.subtract(other).magnitude();
    }

    /**
     * Returns a unit vector (vector of length 1) in the direction of this vector.
     * If the vector has zero magnitude, returns the zero vector.
     */
    public Vector3D normalize() {
        double mag = magnitude();
        if (mag == 0) return Vector3D.zero();
        return scale(1.0 / mag);
    }

    /**
     * Returns a unit vector (vector of length 1) in the direction of this vector.
     * If the vector has magnitude less than a small threshold, returns the zero vector.
     * This is safer than normalize() for numerical stability.
     */
    public Vector3D safeNormalize() {
        double mag = magnitude();
        return mag < 1e-12 ? Vector3D.zero() : scale(1.0 / mag);
    }

    /**
     * Computes the dot product (scalar product) of this vector with another vector.
     */
    public double dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    /**
     * Computes the cross product of this vector with another vector.
     * Returns a new vector that is perpendicular to both.
     */
    public Vector3D cross(Vector3D other) {
        return new Vector3D(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    /**
     * Returns the string representation of the vector in the format (x, y, z).
     */
    @Override
    public String toString() {
        return String.format("(%.6f, %.6f, %.6f)", x, y, z);
    }

    /**
     * Returns a copy (clone) of this vector.
     */
    public Vector3D copy() {
        return new Vector3D(this.x, this.y, this.z);
    }

    public double norm() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D scalarMultiply(double v) {
        return new Vector3D(this.x * v, this.y * v, this.z * v);
    }

    /**
     * Converts this Vector3D to a Vector<Double> containing the x, y, and z components.
     * 
     * @return a Vector<Double> containing the x, y, and z components of this Vector3D
     */
    public Vector<Double> toVector() {
        Vector<Double> vector = new Vector<>();
        vector.add(this.x);
        vector.add(this.y);
        vector.add(this.z);
        return vector;
    }
}
