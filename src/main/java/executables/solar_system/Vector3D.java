package executables.solar_system;

public class Vector3D {


    public double x, y, z;

    // Konstruktor
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Wektor zerowy
    public static Vector3D zero() {
        return new Vector3D(0, 0, 0);
    }

    // Dodawanie wektorów
    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    // Odejmowanie
    public Vector3D subtract(Vector3D other) {
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    // Mnożenie przez skalar
    public Vector3D scale(double scalar) {
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    // Długość (moduł) wektora
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    // Dystans między dwoma wektorami
    public double distanceTo(Vector3D other) {
        return this.subtract(other).magnitude();
    }

    // Normalizacja (jednostkowy wektor)
    public Vector3D normalize() {
        double mag = magnitude();
        if (mag == 0) return Vector3D.zero();
        return scale(1.0 / mag);
    }

    // Iloczyn skalarny
    public double dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    // Iloczyn wektorowy
    public Vector3D cross(Vector3D other) {
        return new Vector3D(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    @Override
    public String toString() {
        return String.format("(%.6f, %.6f, %.6f)", x, y, z);
    }
}


