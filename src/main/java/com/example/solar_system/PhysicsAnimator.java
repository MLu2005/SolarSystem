package com.example.solar_system;

import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.OrbitalEnergyMonitor;
import com.example.utilities.physics_utilities.PhysicsEngine;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

import java.util.List;

/**
 * Handles the animation of the solar system, including the spaceship and planets.
 * It uses a physics engine to move everything correctly over time.
 * While adding and managing the moving of labels above each object.
 */
public class PhysicsAnimator {

    private final OrbitalEnergyMonitor energyMonitor;
    private final List<CelestialBody> bodies;
    private final List<Sphere> planetSpheres;
    private final Group spaceshipGroup;
    private final double SCALE;
    private final LabelManager labelManager;
    private final PerspectiveCamera camera;
    private final SubScene subScene;
    private final PhysicsEngine engine;
    private BurnManager burnManager;

    private boolean isLockedToTitanVisual = false;

    /**
     * Creates a new animator for the solar system simulation.
     *
     * @param bodies List of all space objects like planets and spaceship
     * @param planetSpheres 3D shapes for showing the planets
     * @param spaceshipGroup Group that holds the spaceship
     * @param SCALE A number to make space sizes smaller for display
     * @param labelManager Helps add and update text labels near planets and spaceship
     * @param camera The camera used to view the scene
     * @param subScene The part of the screen where the 3D space is shown
     * @param engine The physics engine that moves everything over time
     */
    public PhysicsAnimator(List<CelestialBody> bodies,
                           List<Sphere> planetSpheres,
                           Group spaceshipGroup,
                           double SCALE,
                           LabelManager labelManager,
                           PerspectiveCamera camera,
                           SubScene subScene,
                           PhysicsEngine engine) {
        this.bodies = bodies;
        this.planetSpheres = planetSpheres;
        this.spaceshipGroup = spaceshipGroup;
        this.SCALE = SCALE;
        this.labelManager = labelManager;
        this.camera = camera;
        this.subScene = subScene;
        this.engine = engine;

        // * Only tracks planets (not the spaceship) for energy monitoring
        List<CelestialBody> trackedBodies = bodies.stream()
                .filter(b -> !b.getName().equalsIgnoreCase("noah's ark"))
                .toList();

        this.energyMonitor = new OrbitalEnergyMonitor(trackedBodies);
    }

    /**
     * Sets the burn manager that will control spaceship speed changes.
     *
     * @param burnManager The object that manages when and how the spaceship changes speed
     */
    public void setBurnManager(BurnManager burnManager) { this.burnManager = burnManager; }


    /**
     * Adds labels next to the planets and spaceship so their names are visible in the scene.
     */
    public void initializeLabels() {
        // * This sphere will anchor onto the spaceship label to avoid flickering.
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
     * Creates and returns the timer that updates the simulation at regular time steps.
     * This moves the planets and spaceship, updates labels, and checks for landing.
     *
     * @return A timer that runs the physics updates and visual changes
     */
    public AnimationTimer createOrbitTimer() {
        return new AnimationTimer() {

            private double currentTime = 0;

            @Override
            public void handle(long now) {
                double step = 1850;

                engine.step(step);

                Vector3D rocketPos = null;
                Vector3D titanPos = null;
                Vector3D saturnPos = null;

                for (int i = 0; i < bodies.size(); i++) {
                    CelestialBody body = bodies.get(i);
                    Vector3D pos = body.getPosition();
                    String name = body.getName().toLowerCase();

                    // * saves important object positions.
                    if (name.equals("noah's ark")) {
                        rocketPos = pos;
                    } else if (name.equals("titan")) {
                        titanPos = pos;
                    } else if (name.equals("saturn")) {
                        saturnPos = pos;
                    }

                    // * pushes moon visually away from earth for visualization purposes.
                    if (name.equals("moon")) {
                        Vector3D earthPos = bodies.stream()
                                .filter(b -> b.getName().equalsIgnoreCase("earth"))
                                .findFirst()
                                .map(CelestialBody::getPosition)
                                .orElse(Vector3D.zero());
                        Vector3D directionFromEarth = pos.subtract(earthPos).normalize();
                        pos = pos.add(directionFromEarth.scale(SCALE * 15));
                    }

                    // * same for titan
                    if (name.equals("titan")) {
                        Vector3D directionFromSaturn = pos.subtract(saturnPos).normalize();
                        pos = pos.add(directionFromSaturn.scale(44 * SCALE));
                    }

                    // * scaling factors
                    double x = pos.x / SCALE;
                    double y = pos.y / SCALE;
                    double z = pos.z / SCALE;

                    if (!name.equals("noah's ark")) {
                        Sphere sphere = planetSpheres.get(i);
                        sphere.setTranslateX(x);
                        sphere.setTranslateZ(y);
                        sphere.setTranslateY(0);
                    }
                }

                // * Handles the spaceship visualization and when to show the landingVisualizer
                if (rocketPos != null && titanPos != null && saturnPos != null) {
                    double distanceKm = rocketPos.subtract(titanPos).magnitude() / 1000.0;
                    // * Debugging distance for understanding and when to use burn logic
                    System.out.printf("Distance to Titan (physics): %.3f km%n", distanceKm);

                    if (!isLockedToTitanVisual && distanceKm <= 13000) {
                        isLockedToTitanVisual = true;
                        System.out.println("Visual lock-on to Titan triggered.");
                    }

                    // * Applying any needed burns
                    burnManager.tryApplyBurn(bodies, "Noah's Ark", distanceKm);

                        // * After becoming 1500 above the surface of titan we launch the landingVisualizer.
                        if (burnManager.isComplete()) {
                        stop();
                        System.out.println("COMPLETE! Landing simulation ended.");

                        Platform.runLater(() -> {
                            try {
                                new LanderVisualizer().start(new Stage());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        return;
                    }


                    Vector3D displayRocketPos;
                    if (isLockedToTitanVisual) {
                        Vector3D directionFromSaturn = titanPos.subtract(saturnPos).normalize();
                        Vector3D titanVisualPos = titanPos.add(directionFromSaturn.scale(44 * SCALE));
                        Vector3D relativeRocketPos = rocketPos.subtract(titanPos);
                        displayRocketPos = titanVisualPos.add(relativeRocketPos);
                    } else {
                        displayRocketPos = rocketPos;
                    }

                    double x = displayRocketPos.x / SCALE;
                    double y = displayRocketPos.y / SCALE;
                    double z = displayRocketPos.z / SCALE;

                    spaceshipGroup.setTranslateX(x);
                    spaceshipGroup.setTranslateZ(y);
                    spaceshipGroup.setTranslateY(0);
                }

                labelManager.updateLabelPositions();
                currentTime += step;
            }
        };
    }
}
