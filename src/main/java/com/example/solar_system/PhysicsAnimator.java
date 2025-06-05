package com.example.solar_system;

import com.example.utilities.StateUtils;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.OrbitalEnergyMonitor;
import executables.solvers.RK4Solver;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import java.util.List;

public class PhysicsAnimator {

    private final OrbitalEnergyMonitor energyMonitor;
    private final List<CelestialBody> bodies;
    private final List<Sphere> planetSpheres;
    private final Group spaceshipGroup;
    private final double SCALE;
    private final LabelManager labelManager;
    private final PerspectiveCamera camera;
    private final SubScene subScene;

    private final RK4Solver rk4Solver = new RK4Solver();
    private double[] stateVector;

    public PhysicsAnimator(List<CelestialBody> bodies, List<Sphere> planetSpheres,
                           Group spaceshipGroup, double SCALE,
                           LabelManager labelManager,
                           PerspectiveCamera camera, SubScene subScene) {
        this.bodies = bodies;
        this.planetSpheres = planetSpheres;
        this.spaceshipGroup = spaceshipGroup;
        this.SCALE = SCALE;
        this.labelManager = labelManager;
        this.camera = camera;
        this.subScene = subScene;
        List<CelestialBody> trackedBodies = bodies.stream()
                .filter(b -> !b.getName().equalsIgnoreCase("noah's ark"))
                .toList();

        this.energyMonitor = new OrbitalEnergyMonitor(trackedBodies);

        // Extract initial state vector (positions and velocities)
        this.stateVector = StateUtils.extractStateVector(bodies);
    }

    public void initializeLabels() {
        Sphere spaceshipLabelAnchor = new Sphere(1);
        spaceshipLabelAnchor.setMaterial(new PhongMaterial(Color.TRANSPARENT));
        spaceshipLabelAnchor.setMouseTransparent(true);
        spaceshipGroup.getChildren().add(spaceshipLabelAnchor);

        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody body = bodies.get(i);
            String name = body.getName();

            if (name.equalsIgnoreCase("noah's ark")) {
                labelManager.addLabel(spaceshipLabelAnchor, name);
            } else {
                Sphere sphere = planetSpheres.get(i);
                labelManager.addLabel(sphere, name);
            }
        }
    }

    public AnimationTimer createOrbitTimer() {
        return new AnimationTimer() {
            private double currentTime = 0;

            @Override
            public void handle(long now) {
                double step = 3000;

                // Apply RK4 solver step to advance physics
                stateVector = rk4Solver.solveStep(
                        (t, y) -> {
                            StateUtils.applyStateVector(y, bodies);
                            return StateUtils.computeDerivatives(y, bodies);
                        },
                        currentTime,
                        stateVector,
                        step
                );

                StateUtils.applyStateVector(stateVector, bodies);

                Vector3D rocketPos = null;
                Vector3D titanPos = null;

                // Find Saturn position (needed for Titan visual offset)
                Vector3D saturnPos = bodies.stream()
                        .filter(b -> b.getName().equalsIgnoreCase("saturn"))
                        .findFirst()
                        .map(CelestialBody::getPosition)
                        .orElse(Vector3D.zero());

                // Loop over bodies for rendering and track rocket/titan raw positions
                for (int i = 0; i < bodies.size(); i++) {
                    CelestialBody body = bodies.get(i);
                    Vector3D pos = body.getPosition();
                    String name = body.getName().toLowerCase();

                    // Save raw positions of rocket and titan
                    if (name.equals("noah's ark")) {
                        rocketPos = pos;
                    } else if (name.equals("titan")) {
                        titanPos = pos;
                    }

                    // Apply moon visual offset (for rendering only)
                    if (name.equals("moon")) {
                        Vector3D earthPos = bodies.stream()
                                .filter(b -> b.getName().equalsIgnoreCase("earth"))
                                .findFirst()
                                .map(CelestialBody::getPosition)
                                .orElse(Vector3D.zero());
                        Vector3D directionFromEarth = pos.subtract(earthPos).normalize();
                        pos = pos.add(directionFromEarth.scale(SCALE * 15));
                    }

                    // Apply titan visual offset (for rendering only)
                    if (name.equals("titan")) {
                        Vector3D directionFromSaturn = pos.subtract(saturnPos).normalize();
                        pos = pos.add(directionFromSaturn.scale(44 * SCALE));
                    }

                    // Convert to visual coordinates by dividing by SCALE
                    double x = pos.x / SCALE;
                    double y = pos.y / SCALE;
                    double z = pos.z / SCALE;

                    // Update 3D positions of spaceship or planets in scene
                    if (name.equals("noah's ark") && spaceshipGroup != null) {
                        spaceshipGroup.setTranslateX(x);
                        spaceshipGroup.setTranslateZ(y);
                        spaceshipGroup.setTranslateY(z);
                    } else if (!name.equals("noah's ark")) {
                        Sphere sphere = planetSpheres.get(i);
                        sphere.setTranslateX(x);
                        sphere.setTranslateZ(y);
                        sphere.setTranslateY(z);
                    }
                }

                // Calculate and print raw physics distance (in km)
                if (rocketPos != null && titanPos != null) {
                    double distanceMeters = rocketPos.subtract(titanPos).magnitude();
                    double distanceKm = distanceMeters / 1000.0;
                    System.out.printf("Distance to Titan (physics): %.3f km%n", distanceKm);
                }

                labelManager.updateLabelPositions();
                currentTime += step;
            }
        };
    }
}
