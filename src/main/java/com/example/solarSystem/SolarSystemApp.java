package com.example.solarSystem;

import com.example.demo.NthDimension;
import executables.solvers.RK4Solver;
import executables.solvers.RKF45Solver;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
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



import java.util.*;
import java.util.function.BiFunction;

public class SolarSystemApp extends Application {

    private static final int SCALE = 200000;
    private final List<Sphere> planetSpheres = new ArrayList<>();
    private List<CelestialBody> bodies;
    private double[] currentState;
    private BiFunction<Double, double[], double[]> ode;

    private final double G = 6.67430e-20; // km^3 / kg / s^2
    private final double EARTH_MATH = 5.972e24; // kg

    private double anchorX, anchorY;
    private double anchorAngleX = 45;
    private double anchorAngleY = 0;

    private final Group orbitRingGroup = new Group();
    private boolean orbitsVisible = true;


    private final Map<KeyCode, Double> velocities = new HashMap<>();
    private final Set<KeyCode> activeKeys = new HashSet<>();
    @Override
    public void start(Stage primaryStage) {
        bodies = DataLoader.loadBodiesFromCSV("src/main/java/com/example/solarSystem/IC.csv");
        if (bodies == null || bodies.isEmpty()) {
            System.err.println("Failed to load celestial bodies!");
            return;
        }





        CelestialBody earth = bodies.stream().filter(b -> b.getName().equalsIgnoreCase("earth")).findFirst().orElse(null);
        CelestialBody moon = bodies.stream().filter(b -> b.getName().equalsIgnoreCase("moon")).findFirst().orElse(null);

        if (earth != null && moon != null) {

            double earthRadius = getScaledRadius("earth") * SCALE;
            double moonRadius = getScaledRadius("moon") * SCALE;




            // --- PHYSICS VALUES ---
            double moonPhysicsDistance = 384_400; // km (real physics orbital radius)
            double moonSpeed = Math.sqrt(G * EARTH_MATH / moonPhysicsDistance); // ~1.022 km/s

            double visualBuffer = 8 * SCALE;

            double boostedDistance = moonPhysicsDistance + 20 * SCALE;
            Vector3D r = new Vector3D(moonPhysicsDistance, 0, 0);
            Vector3D moonPhysicsPos = earth.getPosition().add(r);
            Vector3D tangential = new Vector3D(0, 0, 1);
            Vector3D orbitalVel = tangential.scale(moonSpeed);
            Vector3D moonPhysicsVel = earth.getVelocity().add(orbitalVel);
            moon.setPosition(moonPhysicsPos);
            moon.setVelocity(moonPhysicsVel);



        }

        double[] state0 = StateUtils.extractStateVector(bodies);
        System.out.println("Earth pos: " + earth.getPosition());
        System.out.println("Moon pos: " + moon.getPosition());
        System.out.println("Distance: " + earth.getPosition().subtract(moon.getPosition()).magnitude());


        state0[3] = 0; state0[4] = 0; state0[5] = 0;

        currentState = state0.clone();


        ode = SolarSystemODE.generateODE(bodies);
        currentState = state0.clone();

        Group root = new Group();
        SubScene subScene = new SubScene(root, 1000, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

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


        orbitRingGroup.getChildren().clear();


        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody body = bodies.get(i);
            Sphere sphere = new Sphere(getScaledRadius(body.getName()));
            String name = body.getName().toLowerCase();


            double visualScale = SCALE;
            if (!INNER_PLANETS.contains(name) && !name.equals("moon")) {
                visualScale = SCALE * 1;
            }


            if (!name.equals("sun") && !name.equals("moon") && !name.equals("titan")) {
                double radius = body.getPosition().magnitude() / visualScale;
                int segments = 200;

                double dotRadius = 2.5;
                double dotHeight = 1.0;

                Group ring = new Group();
                for (int j = 0; j < segments; j++) {
                    double angle = 2 * Math.PI * j / segments;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);

                    Cylinder dot = new Cylinder(dotRadius, dotHeight);
                    dot.setMaterial(new PhongMaterial(Color.WHITE));
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
            sphere.setTranslateX(position.x / visualScale);
            sphere.setTranslateY(position.z / visualScale);
            sphere.setTranslateZ(position.y / visualScale);

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

        uiOverlay.getChildren().addAll(showPositionBtn, resetBtn, toggleOrbitsBtn);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(subScene, uiOverlay);
        Scene scene = new Scene(stackPane);

        scene.setOnKeyPressed(e -> {
            activeKeys.add(e.getCode());
            scene.getRoot().requestFocus();
        });
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));

        double moveSpeed = 3500; // Adjust movement speed for smoother transitions

        AnimationTimer movementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handle_rkf45(now);
            }
        };
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
                System.out.printf("📷 Camera: Z=%.0f, rotX=%.1f, rotY=%.1f%n",
                        camera.getTranslateZ(), cameraX.getRotate(), cameraY.getRotate());
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Solar System – Mouse Controlled View");
        primaryStage.show();

        orbitTimer.start();
    }

    final Set<String> INNER_PLANETS = Set.of("mercury", "venus", "earth", "mars");
    final long[] lastUpdate = {System.nanoTime()};

    AnimationTimer orbitTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            double step = 4000; // simulate one hour per frame

            RK4Solver rk4 = new RK4Solver();
            currentState = rk4.solveStep(ode, 0, currentState, step);
            StateUtils.applyStateVector(currentState, bodies);

            for (int i = 0; i < bodies.size(); i++) {
                CelestialBody body = bodies.get(i);
                Vector3D pos = body.getPosition();
                String name = body.getName().toLowerCase();

                double visualScale = SCALE;
                if (!INNER_PLANETS.contains(name) && !name.equals("moon")) {
                    visualScale = SCALE * 1;
                }

                if (name.equals("moon")) {
                    Vector3D earthPos = bodies.stream()
                            .filter(b -> b.getName().equalsIgnoreCase("earth"))
                            .findFirst()
                            .map(CelestialBody::getPosition)
                            .orElse(new Vector3D(0, 0, 0));

                    Vector3D directionFromEarth = pos.subtract(earthPos).normalize();
                    pos = pos.add(directionFromEarth.scale(15 * SCALE));  // visually push moon out from Earth
                }

                planetSpheres.get(i).setTranslateX(pos.x / visualScale);
                planetSpheres.get(i).setTranslateY(pos.z / visualScale);
                planetSpheres.get(i).setTranslateZ(pos.y / visualScale);
            }
        }
    };

    private double getScaledOrbitRadius(String name) {
        return switch (name.toLowerCase()) {
            case "mercury" -> 57.9e6 / SCALE;
            case "venus"   -> 108.2e6 / SCALE;
            case "earth"   -> 149.6e6 / SCALE;
            case "mars"    -> 227.9e6 / SCALE;
            case "jupiter" -> 778.3e6 / SCALE;
            case "saturn"  -> 1427.0e6 / SCALE;
            case "uranus"  -> 2871.0e6 / SCALE;
            case "neptune" -> 4497.1e6 / SCALE;
            default        -> -1; // For moon, titan, etc.
        };
    }



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
                case "sun" -> 150.65;
                case "mercury" -> 4.0;
                case "venus" -> 9.0;
                case "earth" -> 10.0;
                case "moon" -> 2.5;
                case "mars" -> 5.5;
                case "jupiter" -> 70.0;
                case "saturn" -> 60.0;
                case "titan" -> 4.0;
                case "uranus" -> 26.0;
                case "neptune" -> 23.0;
                default -> 5.0;
            };
        }



    public void handle_rkf45(long now) {

        double step = 3_600;            // 3 600 s = 1 h


        RKF45Solver rkf45 = new RKF45Solver();
    /*  We integrate a *single* step and keep only the 5-th-order solution
        that RKF45 returns in the last row.  Column 0 is the time stamp,
        columns 1…N are the state vector.                                     */
        double[][] traj   = rkf45.solve(ode,
                0.0,
                currentState,
                step,
                1,
                null);

        double[] nextState = new double[currentState.length];
        System.arraycopy(traj[traj.length - 1], 1, nextState, 0, nextState.length);
        currentState = nextState;


        StateUtils.applyStateVector(currentState, bodies);

        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody body = bodies.get(i);
            Vector3D pos      = body.getPosition();
            String   name     = body.getName().toLowerCase();

            double visualScale = SCALE;
            if (!INNER_PLANETS.contains(name) && !name.equals("moon")) {
                visualScale = SCALE * 1;
            }

            if (name.equals("moon")) {
                Vector3D earthPos = bodies.stream()
                        .filter(b -> b.getName().equalsIgnoreCase("earth"))
                        .findFirst()
                        .map(CelestialBody::getPosition)
                        .orElse(new Vector3D(0, 0, 0));

                Vector3D directionFromEarth = pos.subtract(earthPos).normalize();
                pos = pos.add(directionFromEarth.scale(15 * SCALE));
            }

            planetSpheres.get(i).setTranslateX(pos.x / visualScale);
            planetSpheres.get(i).setTranslateY(pos.z / visualScale);
            planetSpheres.get(i).setTranslateZ(pos.y / visualScale);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
