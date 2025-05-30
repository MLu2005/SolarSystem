package com.example.solar_system;

import com.example.utilities.Vector3D;
import com.example.utilities.physics_utilities.SolarSystemFactory;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class SolarSystemApp extends Application {
    private Node soundNode;
    private SpaceShipSound spaceShipSound;

    private Group spaceshipGroup;
    private static final int SCALE = 400000;
    private final List<Sphere> planetSpheres = new ArrayList<>();
    private List<CelestialBody> bodies;

    /**
     * Entry point of the JavaFX application.
     * Shows loading screen first, then loads the main scene.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        Stage splashStage = new Stage();
        SplashScreenManager splashManager = new SplashScreenManager(splashStage);
        splashManager.showSplashScreen(() -> {
            try {
                setupMainScene(primaryStage);
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Sets up the main scene with celestial bodies, spaceship, UI, camera, and sound.
     *
     * @param primaryStage the main stage of the application
     * @throws IOException if FXML loading fails
     */
    private void setupMainScene(Stage primaryStage) throws IOException {
        bodies = SolarSystemFactory.loadFromTable();

        if (bodies.isEmpty()) {
            System.err.println("Failed to load celestial bodies!");
            return;
        }

        StageBuilder sceneBuilder = new StageBuilder(SCALE);
        sceneBuilder.setupLighting();
        sceneBuilder.prepareOrbits(bodies);

        Group root = sceneBuilder.getRoot();
        SubScene subScene = sceneBuilder.createSubScene();
        OrbitRendering orbitRenderer = sceneBuilder.getOrbitRenderer();

        PerspectiveCamera camera = new PerspectiveCamera(true);
        CameraController cameraController = new CameraController(camera);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/executables/buttonContainer.fxml"));
        VBox uiOverlay = loader.load();
        UIButtonsController uiController = loader.getController();
        uiController.initialize(cameraController, orbitRenderer, primaryStage);

        Pane labelPane = new Pane();
        labelPane.setMouseTransparent(true);
        LabelManager labelManager = new LabelManager(labelPane, camera, subScene);

        for (CelestialBody body : bodies) {
            String name = body.getName().toLowerCase();

            if (name.equals("spaceship")) {
                spaceshipGroup = SpaceShipBuilder.build(body.getPosition(), SCALE);
                root.getChildren().add(spaceshipGroup);

                // Attach 3D positional audio to the spaceship
                soundNode = spaceshipGroup;
                spaceShipSound = new SpaceShipSound("/Audio/rocketSound.mp3");
                spaceShipSound.attachToNode(spaceshipGroup);

                continue;
            }

            Sphere sphere = new Sphere(BodyDecorations.getScaledRadius(name));
            sphere.setMaterial(BodyDecorations.getMaterial(name));
            BodyDecorations.applyGlowEffectIfSun(sphere, name);

            Vector3D position = body.getPosition();
            sphere.setTranslateX(position.x / SCALE);
            sphere.setTranslateY(position.z / SCALE);

            planetSpheres.add(sphere);
            root.getChildren().add(sphere);
            labelManager.updateLabelPositions();
        }

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(subScene, labelPane, uiOverlay);
        Scene scene = new Scene(stackPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        subScene.widthProperty().bind(scene.widthProperty());
        subScene.heightProperty().bind(scene.heightProperty());

        labelPane.setPrefWidth(800);
        labelPane.setPrefHeight(600);
        labelPane.prefWidthProperty().bind(scene.widthProperty());
        labelPane.prefHeightProperty().bind(scene.heightProperty());

        primaryStage.setScene(scene);
        IconSetter.setAppIcon(primaryStage);
        primaryStage.setTitle("Solar System Simulator");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(900);
        PopUps.showHowTo(primaryStage);

        root.getChildren().add(cameraController.getCameraGroup());
        subScene.setCamera(camera);
        cameraController.startMovement();

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
        scene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 3) PopUps.showCameraLocation(cameraController, primaryStage);
        });

        PhysicsAnimator animator = new PhysicsAnimator(
                bodies, planetSpheres, spaceshipGroup, SCALE,
                labelManager, camera, subScene
        );
        animator.initializeLabels();

        AnimationTimer orbitTimer = animator.createOrbitTimer();
        orbitTimer.start();

        // * Adjusts sound volume based on camera distance
        if (spaceShipSound != null) {
            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    spaceShipSound.updateVolumeRelativeToCamera(cameraController.getCameraGroup());
                }
            }.start();
        }
    }

    /**
     * Launches the application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
