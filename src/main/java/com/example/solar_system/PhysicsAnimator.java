package com.example.solar_system;

import com.example.utilities.StateUtils;
import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.PhysicsEngine;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import java.util.List;


/**
 * Handles the real-time animation and physics-based movement of celestial bodies and a spaceship
 * using a numerical physics engine in a JavaFX 3D scene.
 */
public class PhysicsAnimator {

    private final List<CelestialBody> bodies;
    private final List<Sphere> planetSpheres;
    private final Group spaceshipGroup;
    private final double SCALE;


    private final LabelManager labelManager;
    private final PerspectiveCamera camera;
    private final SubScene subScene;

    /**
     * Constructs a PhysicsAnimator instance.
     *
     * @param bodies          the list of celestial bodies to simulate
     * @param planetSpheres   the corresponding JavaFX spheres for rendering the bodies
     * @param spaceshipGroup  the JavaFX group representing the spaceship
     * @param SCALE           the scale factor used to convert real-world coordinates to display units
     */
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
    }


    public void initializeLabels() {
        // Clear previous labels if needed

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




    /**
     * Handles everything that moves in the utilities by feeding information to the PhysicsEngine
     * The rocket might be used with RungeKF45.
     */
    public AnimationTimer createOrbitTimer() {
        return new AnimationTimer() {
            private boolean titanOverrideDone = false;

            @Override
            public void handle(long now) {


//                if (!titanOverrideDone) {
//                    for (CelestialBody body : bodies) {
//                        if (body.getName().equalsIgnoreCase("titan")) {
//                            body.setPosition(new Vector3D(1.31553474845E9, 2.054189449E7, -3.774477837E7));
//                            body.setVelocity(new Vector3D(-0.36, 15.04, 0.0));
//                            break;
//                        }
//                    }
//                    titanOverrideDone = true;
//                }

                double step = 3000;
                PhysicsEngine engine = new PhysicsEngine();

                for (CelestialBody body : bodies) {
                    engine.addBody(body);
                }

                engine.step(step);

                double[] currentState = StateUtils.extractStateVector(bodies);
                StateUtils.applyStateVector(currentState, bodies);

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
                                .orElse(new Vector3D(0, 1, 0));
                        Vector3D directionFromSaturn = pos.subtract(saturnPos).normalize();
                        pos = pos.add(directionFromSaturn.scale(44 * SCALE));
                    }

                    if (name.equals("noah's ark") && spaceshipGroup != null) {
                        spaceshipGroup.setTranslateX(pos.x / SCALE);
                        spaceshipGroup.setTranslateY(pos.y / SCALE);
                        spaceshipGroup.setTranslateZ(0);
                    } else {
                        if (!name.equals("noah's ark")) {
                            planetSpheres.get(i).setTranslateX(pos.x / SCALE);
                            planetSpheres.get(i).setTranslateZ(pos.y / SCALE);
                            planetSpheres.get(i).setTranslateY(0);
                        }
                    }
                }

                labelManager.updateLabelPositions();
            }
        };
    }

}

