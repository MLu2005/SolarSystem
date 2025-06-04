package com.example.solar_system;

import com.example.utilities.StateUtils;
import com.example.utilities.Vector3D;
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

    private final List<CelestialBody> bodies;
    private final List<Sphere> planetSpheres;
    private final Group spaceshipGroup;
    private final double SCALE;
    private final LabelManager labelManager;
    private final PerspectiveCamera camera;
    private final SubScene subScene;
    private final BurnManager burnManager;

    private final RK4Solver rk4Solver = new RK4Solver();
    private double[] stateVector;

    public PhysicsAnimator(List<CelestialBody> bodies, List<Sphere> planetSpheres,
                           Group spaceshipGroup, double SCALE,
                           LabelManager labelManager,
                           PerspectiveCamera camera, SubScene subScene,
                           BurnManager burnManager) {
        this.bodies = bodies;
        this.planetSpheres = planetSpheres;
        this.spaceshipGroup = spaceshipGroup;
        this.SCALE = SCALE;
        this.labelManager = labelManager;
        this.camera = camera;
        this.subScene = subScene;
        this.burnManager = burnManager;

        // Extract initial state
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
                double step = 10000;


                if (burnManager != null) {
                    burnManager.tryApplyBurn(bodies, "noah's ark");
                }

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

                for (int i = 0; i < bodies.size(); i++) {
                    CelestialBody body = bodies.get(i);
                    Vector3D pos = body.getPosition();
                    String name = body.getName().toLowerCase();

                    if (name.equals("moon")) {
                        Vector3D earthPos = bodies.stream()
                                .filter(b -> b.getName().equalsIgnoreCase("earth"))
                                .findFirst()
                                .map(CelestialBody::getPosition)
                                .orElse(new Vector3D(0, 0, 0));
                        Vector3D directionFromEarth = pos.subtract(earthPos).normalize();
                        pos = pos.add(directionFromEarth.scale(SCALE * 15));
                    }

                    if (name.equals("titan")) {
                        Vector3D saturnPos = bodies.stream()
                                .filter(b -> b.getName().equalsIgnoreCase("saturn"))
                                .findFirst()
                                .map(CelestialBody::getPosition)
                                .orElse(new Vector3D(0, 0, 0));
                        Vector3D directionFromSaturn = pos.subtract(saturnPos).normalize();
                        pos = pos.add(directionFromSaturn.scale(44 * SCALE));
                    }

                    // Set Y (vertical) to zero for all
                    double fixedY = 0;

                    if (name.equals("noah's ark") && spaceshipGroup != null) {
                        spaceshipGroup.setTranslateX(pos.x / SCALE);
                        spaceshipGroup.setTranslateY(fixedY);
                        spaceshipGroup.setTranslateZ(pos.y / SCALE); // physics y mapped to z? Or use pos.z?
                    } else if (!name.equals("noah's ark")) {
                        Sphere sphere = planetSpheres.get(i);
                        sphere.setTranslateX(pos.x / SCALE);
                        sphere.setTranslateY(fixedY);
                        sphere.setTranslateZ(pos.y / SCALE);
                    }
                }

                labelManager.updateLabelPositions();
                currentTime += step;
            }
        };
    }
}

