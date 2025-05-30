package com.example.solar_system;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import java.util.List;

/**
 * Renders orbital rings for celestial bodies in a JavaFX 3D scene.
 * Only bodies that are not the Sun, Moon, or Titan will have rings drawn.
 */
public class OrbitRendering {
    private final Group orbitRingGroup = new Group();
    private final int scale;

    /**
     * Constructs an OrbitRendering instance with the given scale factor.
     *
     * @param scale the scaling factor used to shrink the orbit radius
     */
    public OrbitRendering(int scale) {
        this.scale = scale;
    }

    /**
     * Adds visual orbital rings for a list of celestial bodies.
     * Rings are created using many small cylinders placed in a circular path.
     *
     * @param bodies list of celestial bodies to process
     */
    public void addOrbits(List<CelestialBody> bodies) {
        for (CelestialBody body : bodies) {
            String name = body.getName().toLowerCase();

            if (!name.equals("sun") && !name.equals("moon") && !name.equals("titan")) {
                double radius = body.getPosition().magnitude() / scale;
                int segments = 1750;

                double dotRadius = 1;
                double dotHeight = 1;

                Group ring = new Group();
                for (int j = 0; j < segments; j++) {
                    double angle = 2 * Math.PI * j / segments;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);

                    Cylinder dot = new Cylinder(dotRadius, dotHeight);
                    dot.setMaterial(new PhongMaterial(Color.NAVAJOWHITE));
                    dot.setRotationAxis(Rotate.X_AXIS);
                    dot.setRotate(90);
                    dot.setTranslateX(x);
                    dot.setTranslateZ(z);

                    ring.getChildren().add(dot);
                }
                orbitRingGroup.getChildren().add(ring);
            }
        }
    }
    /**
     * Returns the group containing all orbit rings.
     *
     * @return the orbit ring group
     */
    public Group getOrbitGroup() {
        return orbitRingGroup;
    }

    /**
     * Sets the visibility of all orbit rings.
     *
     * @param visible true to show the rings, false to hide them
     */
    public void setVisible(boolean visible) {
        orbitRingGroup.setVisible(visible);
    }
}
