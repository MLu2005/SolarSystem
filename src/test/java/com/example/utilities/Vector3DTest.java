package com.example.utilities;

import org.junit.jupiter.api.Test;

import java.util.Vector;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Vector3D class.
 * Verifies all public methods and common edge cases.
 */
class Vector3DTest {

    /**
     * Tests basic getters for x, y, z.
     */
    @Test
    void testGetters() {
        Vector3D v = new Vector3D(1.0, -2.0, 3.5);
        assertEquals(1.0, v.getX(), 1e-9);
        assertEquals(-2.0, v.getY(), 1e-9);
        assertEquals(3.5, v.getZ(), 1e-9);
    }

    /**
     * Tests that the zero vector returns all components as 0.
     */
    @Test
    void testZeroVector() {
        Vector3D zero = Vector3D.zero();
        assertEquals(0.0, zero.getX(), 1e-9);
        assertEquals(0.0, zero.getY(), 1e-9);
        assertEquals(0.0, zero.getZ(), 1e-9);
    }

    /**
     * Tests vector addition.
     */
    @Test
    void testAdd() {
        Vector3D a = new Vector3D(1, 2, 3);
        Vector3D b = new Vector3D(4, -1, 2);
        Vector3D result = a.add(b);
        assertEquals(5, result.getX(), 1e-9);
        assertEquals(1, result.getY(), 1e-9);
        assertEquals(5, result.getZ(), 1e-9);
    }

    /**
     * Tests vector subtraction.
     */
    @Test
    void testSubtract() {
        Vector3D a = new Vector3D(5, 3, -2);
        Vector3D b = new Vector3D(1, 1, 1);
        Vector3D result = a.subtract(b);
        assertEquals(4, result.getX(), 1e-9);
        assertEquals(2, result.getY(), 1e-9);
        assertEquals(-3, result.getZ(), 1e-9);
    }

    /**
     * Tests scalar multiplication.
     */
    @Test
    void testScale() {
        Vector3D v = new Vector3D(2, -4, 3);
        Vector3D scaled = v.scale(2.5);
        assertEquals(5, scaled.getX(), 1e-9);
        assertEquals(-10, scaled.getY(), 1e-9);
        assertEquals(7.5, scaled.getZ(), 1e-9);
    }

    /**
     * Tests magnitude and magnitudeSquared functions.
     */
    @Test
    void testMagnitudeAndSquared() {
        Vector3D v = new Vector3D(3, 4, 12);
        assertEquals(169.0, v.magnitudeSquared(), 1e-9);
        assertEquals(13.0, v.magnitude(), 1e-9);
    }

    /**
     * Tests distance between two vectors.
     */
    @Test
    void testDistanceTo() {
        Vector3D a = new Vector3D(1, 2, 3);
        Vector3D b = new Vector3D(4, 6, 3);
        assertEquals(5.0, a.distanceTo(b), 1e-9);
    }

    /**
     * Tests normalization of a non-zero vector.
     */
    @Test
    void testNormalize() {
        Vector3D v = new Vector3D(0, 3, 4);
        Vector3D norm = v.normalize();
        assertEquals(0.0, norm.getX(), 1e-9);
        assertEquals(0.6, norm.getY(), 1e-9);
        assertEquals(0.8, norm.getZ(), 1e-9);
        assertEquals(1.0, norm.magnitude(), 1e-9);
    }

    /**
     * Tests normalization of a zero vector.
     */
    @Test
    void testNormalizeZeroVector() {
        Vector3D zero = Vector3D.zero();
        Vector3D norm = zero.normalize();
        assertEquals(0.0, norm.magnitude(), 1e-9);
    }

    /**
     * Tests safeNormalize for near-zero magnitude.
     */
    @Test
    void testSafeNormalizeTinyVector() {
        Vector3D tiny = new Vector3D(1e-14, 1e-14, 1e-14);
        Vector3D result = tiny.safeNormalize();
        assertEquals(0.0, result.magnitude(), 1e-9);
    }

    /**
     * Tests dot product calculation.
     */
    @Test
    void testDotProduct() {
        Vector3D a = new Vector3D(1, 2, 3);
        Vector3D b = new Vector3D(4, -5, 6);
        double dot = a.dot(b);
        assertEquals(12.0, dot, 1e-9); // 1*4 + 2*(-5) + 3*6 = 4 -10 +18 = 12
    }

    /**
     * Tests cross product output.
     */
    @Test
    void testCrossProduct() {
        Vector3D a = new Vector3D(1, 0, 0);
        Vector3D b = new Vector3D(0, 1, 0);
        Vector3D cross = a.cross(b);
        assertEquals(0, cross.getX(), 1e-9);
        assertEquals(0, cross.getY(), 1e-9);
        assertEquals(1, cross.getZ(), 1e-9);
    }

    /**
     * Tests toString output format.
     */
    @Test
    void testToString() {
        Vector3D v = new Vector3D(1.23456789, -2.34567891, 3.14159265);
        String expected = String.format("(%.6f, %.6f, %.6f)", 1.23456789, -2.34567891, 3.14159265);
        assertEquals(expected, v.toString());
    }

    /**
     * Tests conversion to Vector<Double>.
     */
    @Test
    void testToVector() {
        Vector3D v = new Vector3D(1.5, -2.5, 3.5);
        Vector<Double> vector = v.toVector();

        // Check size
        assertEquals(3, vector.size());

        // Check components
        assertEquals(1.5, vector.get(0), 1e-9);
        assertEquals(-2.5, vector.get(1), 1e-9);
        assertEquals(3.5, vector.get(2), 1e-9);
    }
}
