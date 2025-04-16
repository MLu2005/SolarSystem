package executables.solar_system;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

/**
 * SolarSystemApp creates a 2.5D visualization of a solar system using JavaFX.
 * It renders celestial bodies from CSV data, animates their motion based on ODE simulation,
 * and provides mouse and keyboard controls for camera navigation.
 */
public class SolarSystemApp extends Application {

    private static final int SCALE = 20000;
    private static final double SPHERE_SCALE = 300;
    private static final int STEP_DELAY_MS = 40;

    private final List<Sphere> planetSpheres = new ArrayList<>();
    private List<CelestialBody> bodies;
    private double[][] simulationData;
    private int currentStep = 0;

    private double anchorX, anchorY;
    private double anchorAngleX = 45;
    private double anchorAngleY = 0;

    /**
     * Initializes the application: loads celestial data, sets up camera, GUI, lighting, and animation.
     */
    @Override
    public void start(Stage primaryStage) {
        bodies = DataLoader.loadBodiesFromCSV("src/main/java/executables/solar_system/IC.csv");
        if (bodies == null || bodies.isEmpty()) {
            System.err.println("Failed to load celestial bodies!");
            return;
        }

        double[] state0 = StateUtils.extractStateVector(bodies);

        // Lock the sun at the center (0,0,0)
        state0[3] = 0;
        state0[4] = 0;
        state0[5] = 0;

        var ode = SolarSystemODE.generateODE(bodies);
        simulationData = executables.solvers.NthDimension.rungeKutta4(ode, 0, state0, 86400, 365);

        Group root = new Group();
        SubScene subScene = new SubScene(root, 1000, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        // === Camera setup with rotation groups ===
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(100000);
        camera.setTranslateX(0);
        camera.setTranslateY(-3000);  // above the system
        camera.setTranslateZ(0);

        Group cameraX = new Group(camera);     // vertical rotation
        Group cameraY = new Group(cameraX);    // horizontal rotation
        root.getChildren().add(cameraY);       // add to scene

        cameraX.setRotationAxis(Rotate.X_AXIS);
        cameraX.setRotate(90);         // looking straight down

        cameraY.setRotationAxis(Rotate.Y_AXIS);
        cameraY.setRotate(0);

        subScene.setCamera(camera);

        // === Lighting ===
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateY(-1000);
        light.setTranslateZ(-500);
        root.getChildren().addAll(light, new AmbientLight(Color.color(0.3, 0.3, 0.3)));

        // === Celestial Bodies ===
        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody body = bodies.get(i);
            Sphere sphere = new Sphere(getScaledRadius(body.getName()));
            PhongMaterial mat = new PhongMaterial(getColorForBody(body.getName()));
            sphere.setMaterial(mat);
            planetSpheres.add(sphere);
            root.getChildren().add(sphere);
        }

        updatePositions(currentStep);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(STEP_DELAY_MS), e -> {
            currentStep++;
            if (currentStep >= simulationData.length) {
                currentStep = 0;
            }
            updatePositions(currentStep);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Group main = new Group(subScene);
        Scene scene = new Scene(main);

        // === Keyboard controls for movement (WSAD + Q/E) ===
        scene.setOnKeyPressed(e -> {
            double step = 100;

            switch (e.getCode()) {
                case W:
                    camera.setTranslateZ(camera.getTranslateZ() + step);
                    break;
                case S:
                    camera.setTranslateZ(camera.getTranslateZ() - step);
                    break;
                case A:
                    camera.setTranslateX(camera.getTranslateX() - step);
                    break;
                case D:
                    camera.setTranslateX(camera.getTranslateX() + step);
                    break;
                case Q:
                    camera.setTranslateY(camera.getTranslateY() - step);
                    break;
                case E:
                    camera.setTranslateY(camera.getTranslateY() + step);
                    break;
            }

            System.out.printf("Camera: X=%.0f, Y=%.0f, Z=%.0f%n",
                    camera.getTranslateX(),
                    camera.getTranslateY(),
                    camera.getTranslateZ());
        });

        // === Mouse drag to rotate camera ===
        scene.setOnMousePressed(e -> {
            anchorX = e.getSceneX();
            anchorY = e.getSceneY();
            anchorAngleX = cameraX.getRotate();
            anchorAngleY = cameraY.getRotate();
        });

        scene.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - anchorX;
            double deltaY = e.getSceneY() - anchorY;
            cameraY.setRotate(anchorAngleY + deltaX / 2);
            cameraX.setRotate(anchorAngleX - deltaY / 2);
        });

        // === Scroll to zoom ===
        scene.setOnScroll((ScrollEvent e) -> {
            double zoom = e.getDeltaY();
            camera.setTranslateZ(camera.getTranslateZ() + zoom * 10);
        });

        // === Double-click to print camera info ===
        scene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.printf("📷 Camera: Z=%.0f, rotX=%.1f, rotY=%.1f%n",
                        camera.getTranslateZ(),
                        cameraX.getRotate(),
                        cameraY.getRotate());
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Solar System – Mouse Controlled View");
        primaryStage.show();
    }

    /**
     * Updates the position of all celestial bodies in the simulation.
     * Sun is fixed at (0, 0, 0), others move based on simulation data.
     */
    private void updatePositions(int step) {
        StateUtils.applyStateVector(simulationData[step], bodies);

        // Lock the Sun at the center
        bodies.get(0).getPosition().x = 0;
        bodies.get(0).getPosition().y = 0;
        bodies.get(0).getPosition().z = 0;

        for (int i = 0; i < bodies.size(); i++) {
            Vector3D pos = bodies.get(i).getPosition();

            // Custom 2.5D mapping (XZ = orbital plane, Y = height/tilt)
            planetSpheres.get(i).setTranslateX(pos.z / SCALE);  // left-right
            planetSpheres.get(i).setTranslateZ(pos.y / SCALE);  // depth
            planetSpheres.get(i).setTranslateY(pos.x / SCALE);  // height (orbital inclination)
        }
    }

    /**
     * Returns the color of a planet or star based on its name.
     */
    private Color getColorForBody(String name) {
        String lower = name.toLowerCase();

        if (lower.equals("sun")) return Color.GOLD;
        if (lower.equals("mercury")) return Color.SILVER;
        if (lower.equals("venus")) return Color.BURLYWOOD;
        if (lower.equals("earth")) return Color.BLUE;
        if (lower.equals("moon")) return Color.LIGHTGRAY;
        if (lower.equals("mars")) return Color.RED;
        if (lower.equals("jupiter")) return Color.ORANGE;
        if (lower.equals("saturn")) return Color.BEIGE;
        if (lower.equals("titan")) return Color.DARKKHAKI;
        if (lower.equals("uranus")) return Color.AQUA;
        if (lower.equals("neptune")) return Color.DARKBLUE;

        return Color.WHITE;
    }

    /**
     * Returns the scaled radius of a celestial body based on real data.
     */
    private double getScaledRadius(String name) {
        String lower = name.toLowerCase();

        if (lower.equals("sun")) return 696_340 / 500.0;
        if (lower.equals("mercury")) return 2440 / 10.0;
        if (lower.equals("venus")) return 6052 / 10.0;
        if (lower.equals("earth")) return 6371 / 10.0;
        if (lower.equals("moon")) return 1737 / 5.0;
        if (lower.equals("mars")) return 3390 / 10.0;
        if (lower.equals("jupiter")) return 69911 / 50.0;
        if (lower.equals("saturn")) return 58232 / 50.0;
        if (lower.equals("titan")) return 2575 / 10.0;
        if (lower.equals("uranus")) return 25362 / 30.0;
        if (lower.equals("neptune")) return 24622 / 30.0;

        return 300.0;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
