package com.example.solar_system;

import com.example.utilities.physics_utilities.SolarSystemFactory;
import com.example.utilities.Vector3D;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

import java.util.*;

/**
 * The main entry point of the Solar System Simulator application.
 * Loads celestial body data, builds the scene with lighting and orbits,
 * renders planets and spaceship, handles user input, and runs the physics animation.
 */
public class SolarSystemApp extends Application {

    /** Group to hold the spaceship model */
    private Group spaceshipGroup;

    /** Scale factor used to convert astronomical distances to scene coordinates */
    private static final int SCALE = 400000;

    /** List of sphere representations for each planet */
    private final List<Sphere> planetSpheres = new ArrayList<>();

    /** List of celestial bodies used in the simulation */
    private List<CelestialBody> bodies;

    /**
     * Starts the JavaFX application.
     *
     * @param primaryStage the main window (stage)
     */
    @Override
    public void start(Stage primaryStage) {
        // Load celestial bodies from static table
        bodies = SolarSystemFactory.loadFromTable();

        if (bodies.isEmpty()) {
            System.err.println("Failed to load celestial bodies!");
            return;
        }
        // * Build and configure the scene
        SceneBuilder sceneBuilder = new SceneBuilder(SCALE);
        sceneBuilder.setupLighting(); //lighting
        sceneBuilder.prepareOrbits(bodies); //orbits

        Group root = sceneBuilder.getRoot();
        SubScene subScene = sceneBuilder.createSubScene();
        OrbitRendering orbitRenderer = sceneBuilder.getOrbitRenderer();


        // * Create and place each celestial body as a 3D sphere
        for (CelestialBody body : bodies) {
            String name = body.getName().toLowerCase();

            if (name.equals("spaceship")) {
                spaceshipGroup = SpaceShipBuilder.build(body.getPosition(), SCALE);
                root.getChildren().add(spaceshipGroup);
                continue;
            }

            Sphere sphere = new Sphere(BodyDecorations.getScaledRadius(name));
            sphere.setMaterial(BodyDecorations.getMaterial(name));
            BodyDecorations.applyGlowEffectIfSun(sphere, name);

            Vector3D position = body.getPosition();
            sphere.setTranslateX(position.x / SCALE);
            sphere.setTranslateY(position.z / SCALE);
            sphere.setTranslateZ(0);

            planetSpheres.add(sphere);
            root.getChildren().add(sphere);
        }

        // * Setup camera and UI
        PerspectiveCamera camera = new PerspectiveCamera(true);
        CameraController cameraController = new CameraController(camera);
        UIButtons uiOverlay = new UIButtons(primaryStage, cameraController, orbitRenderer);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(subScene, uiOverlay);
        Scene scene = new Scene(stackPane, 800 , 600);
        subScene.widthProperty().bind(scene.widthProperty());
        subScene.heightProperty().bind(scene.heightProperty());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Solar System Simulator");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();

        // * Attach camera and begin movement
        root.getChildren().add(cameraController.getCameraGroup());
        subScene.setCamera(camera);
        cameraController.startMovement();

        // * Handle keyboard and mouse input for camera control
        scene.setOnKeyPressed(e -> {
            e.consume();
            cameraController.onKeyPressed(e.getCode());
        });
        scene.setOnKeyReleased(e -> {
            e.consume();
            cameraController.onKeyReleased(e.getCode());
        });

        scene.setOnMousePressed(cameraController::handleMousePressed);
        scene.setOnMouseDragged(cameraController::handleMouseDragged);
        scene.setOnScroll(e -> cameraController.zoom(e.getDeltaY() * 0.1));

        // * Gets camera location on double click
        scene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.printf("📷 Camera: Z=%.0f, rotX=%.1f, rotY=%.1f%n",
                        camera.getTranslateZ(),
                        cameraController.getRotationX(),
                        cameraController.getRotationY());
            }
        });

        // * Start the physics animation loop
        PhysicsAnimator animator = new PhysicsAnimator(bodies, planetSpheres, spaceshipGroup, SCALE);
        AnimationTimer orbitTimer = animator.createOrbitTimer();
        orbitTimer.start();
    }


    /**
     * Launches the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
