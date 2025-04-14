package executables.solar_system;

public class CelestialBody {
    private final String name;
    private final double mass; // w kilogramach
    private Vector3D position; // w kilometrach
    private Vector3D velocity; // w km/s
    private Vector3D acceleration; // w km/s^2

    public CelestialBody(String name, double mass, Vector3D position, Vector3D velocity) {
        this.name = name;
        this.mass = mass;
        this.position = position;
        this.velocity = velocity;
        this.acceleration = Vector3D.zero(); // domyślnie 0
    }

    // Gettery
    public String getName() {
        return name;
    }

    public double getMass() {
        return mass;
    }

    public Vector3D getPosition() {
        return position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public Vector3D getAcceleration() {
        return acceleration;
    }

    // Settery
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    public void setAcceleration(Vector3D acceleration) {
        this.acceleration = acceleration;
    }

    @Override
    public String toString() {
        return String.format("%s\nMass: %.3e kg\nPos: %s\nVel: %s\n", name, mass, position, velocity);
    }
}
