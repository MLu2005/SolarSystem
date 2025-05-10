package com.example.solarSystem;

import com.example.solarSystem.Physics.PhysicsEngine;
import executables.solvers.RKF45Solver;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.shape.Cylinder;
import com.example.solarSystem.Physics.SolarSystemFactory;
import org.jetbrains.annotations.NotNull;


import java.util.*;
import java.util.function.BiFunction;

public class SolarSystemApp extends Application {

    private static final int SCALE = 80000;
    private final List<Sphere> planetSpheres = new ArrayList<>();
    private BiFunction<Double, double[], double[]> ode;

    private double anchorX, anchorY;
    private double anchorAngleX = 45;
    private double anchorAngleY = 0;

    private final Group orbitRingGroup = new Group();
    private boolean orbitsVisible = true;

    private List<CelestialBody> bodies;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    @Override
    public void start(Stage primaryStage) {
        bodies = SolarSystemFactory.loadFromTable();
        if (bodies == null || bodies.isEmpty()) {
            System.err.println("Failed to load celestial bodies!");
            return;
        }

        Group root = new Group();
        SubScene subScene = new SubScene(root, 1000, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        //* Setting the camera.
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(100000);
        camera.setTranslateX(-1362.1);
        camera.setTranslateY(-5555.7);
        camera.setTranslateZ(-11820.0);
        Group cameraX = new Group(camera);
        Group cameraY = new Group(cameraX);
        cameraX.setRotationAxis(Rotate.X_AXIS);
        cameraX.setRotate(-26.3);
        cameraY.setRotationAxis(Rotate.Y_AXIS);
        cameraY.setRotate(6.2);
        root.getChildren().add(cameraY);

        subScene.setCamera(camera);

        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateY(-1000);
        light.setTranslateZ(-500);
        root.getChildren().addAll(light, new AmbientLight(Color.color(0.3, 0.3, 0.3)));


        // * The following part is responsible for adding ORBITS.
        for (CelestialBody body : bodies) {
            Sphere sphere = new Sphere(getScaledRadius(body.getName()));
            String name = body.getName().toLowerCase();


            if (!name.equals("sun") && !name.equals("moon") && !name.equals("titan")) {
                double radius = body.getPosition().magnitude() / SCALE;
                int segments = 1500;

                double dotRadius = 3.5;
                double dotHeight = 2.0;

                Group ring = new Group();
                for (int j = 0; j < segments; j++) {
                    double angle = 2 * Math.PI * j / segments;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);

                    Cylinder dot = new Cylinder(dotRadius, dotHeight);
                    dot.setMaterial(new PhongMaterial(Color.LIGHTSLATEGREY));
                    dot.setRotationAxis(Rotate.X_AXIS);
                    dot.setRotate(90);
                    dot.setTranslateX(x);
                    dot.setTranslateZ(z);

                    ring.getChildren().add(dot);
                }
                orbitRingGroup.getChildren().add(ring);
            }


            PhongMaterial mat = new PhongMaterial(getColorForBody(body.getName()));
            sphere.setMaterial(mat);
            Vector3D position = body.getPosition();
            sphere.setTranslateX(position.x / SCALE);
            sphere.setTranslateY(position.z / SCALE);
            sphere.setTranslateZ(0);

            planetSpheres.add(sphere);
            root.getChildren().add(sphere);
        }
        root.getChildren().add(orbitRingGroup);



        VBox uiOverlay = new VBox(10);
        uiOverlay.setTranslateX(20);
        uiOverlay.setTranslateY(20);
        uiOverlay.setPickOnBounds(false);

        Button showPositionBtn = new Button("Show Camera Location");
        Button resetBtn = new Button("Reset Camera");
        Button toggleOrbitsBtn = new Button("Toggle Orbits");
        Button toggleFullScreen = new Button( "Toggle FullScreen");
        toggleOrbitsBtn.setOnAction(e -> {
            orbitsVisible = !orbitsVisible;
            orbitRingGroup.setVisible(orbitsVisible);
        });

        showPositionBtn.setOnAction(e -> {
            System.out.printf("📷 Camera: X=%.1f, Y=%.1f, Z=%.1f | rotX=%.1f, rotY=%.1f%n",
                    camera.getTranslateX(),
                    camera.getTranslateY(),
                    camera.getTranslateZ(),
                    cameraX.getRotate(),
                    cameraY.getRotate());
        });

        resetBtn.setOnAction(e -> {
            camera.setTranslateX(-1362.1);
            camera.setTranslateY(-5555.7);
            camera.setTranslateZ(-11820.0);
            cameraX.setRotate(-26.3);
            cameraY.setRotate(6.2);
        });

        uiOverlay.getChildren().addAll(showPositionBtn, resetBtn, toggleOrbitsBtn, toggleFullScreen);
        toggleFullScreen.setOnAction(e -> primaryStage.setFullScreen(!primaryStage.isFullScreen()));

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(subScene, uiOverlay);
        Scene scene = new Scene(stackPane, 800 , 600);
        subScene.widthProperty().bind(scene.widthProperty());
        subScene.heightProperty().bind(scene.heightProperty());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Solar System Simulator");
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.show();

        scene.setOnKeyPressed(e -> {
            activeKeys.add(e.getCode());
            scene.getRoot().requestFocus();
        });
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        AnimationTimer movementTimer = getAnimationTimer(cameraY, camera);
        movementTimer.start();



        scene.setOnMousePressed(e -> {
            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
            anchorAngleX = cameraX.getRotate();
            anchorAngleY = cameraY.getRotate();
        });

        scene.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - anchorX;
            double deltaY = e.getSceneY() - anchorY;
            cameraX.setRotate(anchorAngleX - deltaY * 0.09);
            cameraY.setRotate(anchorAngleY + deltaX * 0.09);
        });

        scene.setOnScroll((ScrollEvent e) -> {
            double zoomFactor = e.getDeltaY() * 0.1;


            double pitch = Math.toRadians(cameraX.getRotate());
            double yaw = Math.toRadians(cameraY.getRotate());

            double dx = Math.sin(yaw) * Math.cos(pitch);
            double dy = -Math.sin(pitch);
            double dz = Math.cos(yaw) * Math.cos(pitch);


            camera.setTranslateX(camera.getTranslateX() + dx * zoomFactor * 50);
            camera.setTranslateY(camera.getTranslateY() + dy * zoomFactor * 50);
            camera.setTranslateZ(camera.getTranslateZ() + dz * zoomFactor * 50);
        });


        scene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.printf(" Camera: Z=%.0f, rotX=%.1f, rotY=%.1f%n",
                        camera.getTranslateZ(), cameraX.getRotate(), cameraY.getRotate());
            }
        });


        orbitTimer.start();
    }

    @NotNull
    private AnimationTimer getAnimationTimer(Group cameraY, PerspectiveCamera camera) {
        double moveSpeed = 3150; // Adjust movement speed for smoother transitions

        AnimationTimer movementTimer = new AnimationTimer() {
            private long lastTime = -1;

            @Override
            public void handle(long now) {
                if (lastTime < 0) {
                    lastTime = now;
                    return;
                }
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;


                double dx = 0, dy = 0, dz = 0;

                if (activeKeys.contains(KeyCode.W)) dz += moveSpeed * deltaTime;  // Forward
                if (activeKeys.contains(KeyCode.S)) dz -= moveSpeed * deltaTime;  // Backward
                if (activeKeys.contains(KeyCode.A)) dx -= moveSpeed * deltaTime;  // Left
                if (activeKeys.contains(KeyCode.D)) dx += moveSpeed * deltaTime;  // Right

                if (activeKeys.contains(KeyCode.CONTROL)) dy += moveSpeed * deltaTime; // DOWN (Ctrl moves )
                if (activeKeys.contains(KeyCode.SPACE)) dy -= moveSpeed * deltaTime; // UP (Space moves up )

                double yaw = Math.toRadians(cameraY.getRotate()); // controls horizontal movement.



                double forwardX = Math.sin(yaw);
                double forwardZ = Math.cos(yaw);
                double rightX = Math.cos(yaw);
                double rightZ = -Math.sin(yaw);


                camera.setTranslateX(camera.getTranslateX() + (dx * rightX + dz * forwardX));
                camera.setTranslateY(camera.getTranslateY() + dy);
                camera.setTranslateZ(camera.getTranslateZ() + (dx * rightZ + dz * forwardZ));
            }
        };
        return movementTimer;
    }


    AnimationTimer orbitTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            double step = 1400;
            ode = SolarSystemODE.generateODE(bodies);
            PhysicsEngine engine = new PhysicsEngine();

            for (CelestialBody body : bodies) {
                engine.addBody(body);
            }

            engine.step(step);

            // * Get the current state of all bodies (using the current positions and velocities)
            double[] currentState = StateUtils.extractStateVector(bodies);

            // * Only use the Physics Engine for the solar simulation for higher accuracy but use RungeKuta for rocket  simulation only.

//            // * Give it back to rk45
//            RKF45Solver rkf45 = new RKF45Solver();
//            double[][] traj = rkf45.solve(ode,
//                    0.0,
//                    currentState,
//                    step,
//                    5,
//                    null);
//
//            double[] nextState = new double[currentState.length];
//            System.arraycopy(traj[traj.length - 1], 1, nextState, 0, nextState.length);
//            currentState = nextState;

            StateUtils.applyStateVector(currentState, bodies);

            for (int i = 0; i < bodies.size(); i++) {
                CelestialBody body = bodies.get(i);
                Vector3D pos = body.getPosition();
                String name = body.getName().toLowerCase();



                // * Assign moon to earth and visually pushing it out
                if (name.equals("moon")) {
                    Vector3D earthPos = bodies.stream()
                            .filter(b -> b.getName().equalsIgnoreCase("earth"))
                            .findFirst()
                            .map(CelestialBody::getPosition)
                            .orElse(new Vector3D(0, 0, 0));

                    Vector3D directionFromEarth = pos.subtract(earthPos).normalize();
                    pos = pos.add(directionFromEarth.scale( SCALE * 15));  // visually push moon out from Earth
                }

                // * Assign titan to saturn and visually pushing it out
                if (name.equals("titan")) {
                    Vector3D saturnPos = bodies.stream()
                            .filter(b -> b.getName().equalsIgnoreCase("saturn"))
                            .findFirst()
                            .map(CelestialBody::getPosition)
                            .orElse(new Vector3D(0, 1, 0));

                    Vector3D directionFromSaturn = pos.subtract(saturnPos).normalize();
                    pos = pos.add(directionFromSaturn.scale(44 * SCALE));  // visually push titan out from Saturn

                }



                planetSpheres.get(i).setTranslateX(pos.x / SCALE);
                planetSpheres.get(i).setTranslateZ(pos.y / SCALE);
                planetSpheres.get(i).setTranslateY(0);

            }
        }
    };

    private Color getColorForBody(String name) {
        return switch (name.toLowerCase()) {
            case "sun" -> Color.GOLD;
            case "mercury" -> Color.SILVER;
            case "venus" -> Color.BURLYWOOD;
            case "earth" -> Color.BLUE;
            case "moon" -> Color.LIGHTGRAY;
            case "mars" -> Color.RED;
            case "jupiter" -> Color.ORANGE;
            case "saturn" -> Color.BEIGE;
            case "titan" -> Color.DARKKHAKI;
            case "uranus" -> Color.AQUA;
            case "neptune" -> Color.DARKBLUE;
            default -> Color.WHITE;
        };
    }

    private double getScaledRadius(String name) {
            // * Sizes have been edited to make them as stable as possible.
            return switch (name.toLowerCase()) {
                case "sun" -> 510.0;
                case "mercury" -> 5.0;
                case "venus" -> 9.0;
                case "earth" -> 10.0;
                case "moon" -> 2.5;
                case "mars" -> 6.5;
                case "jupiter" -> 60.0;
                case "saturn" -> 40.0;
                case "titan" -> 6.0;
                case "uranus" -> 26.0;
                case "neptune" -> 23.0;
                default -> 5.0;
            };
        }



    public static void main(String[] args) {
        launch(args);
    }
}
