package executables.solar_system;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

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

    @Override
    public void start(Stage primaryStage) {
        bodies = DataLoader.loadBodiesFromCSV("src/main/java/executables/solar_system/IC.csv");
        if (bodies == null || bodies.isEmpty()) {
            System.err.println("❌ Nie wczytano ciał niebieskich!");
            return;
        }

        double[] state0 = StateUtils.extractStateVector(bodies);

        state0[3] = 0;
        state0[4] = 0;
        state0[5] = 0;

        var ode = SolarSystemODE.generateODE(bodies);
        simulationData = executables.solvers.NthDimension.rungeKutta4(ode, 0, state0, 86400, 365);

        Group root = new Group();
        SubScene subScene = new SubScene(root, 1000, 800, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        // === Kamera + grupy obrotu ===
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(100000);

// Kamera patrzy pionowo w dół na płaszczyznę XZ
        camera.setTranslateX(0);
        camera.setTranslateY(-3000);   // nad układem
        camera.setTranslateZ(0);




        Group cameraX = new Group(camera);     // obrót góra/dół
        Group cameraY = new Group(cameraX);    // obrót lewo/prawo
        root.getChildren().add(cameraY);       // dodajemy grupy do sceny

        cameraX.setRotationAxis(Rotate.X_AXIS);
        cameraX.setRotate(90);         // patrzymy w dół

        cameraY.setRotationAxis(Rotate.Y_AXIS);
        cameraY.setRotate(0);

        subScene.setCamera(camera);


        // === Światła ===
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateY(-1000);
        light.setTranslateZ(-500);
        root.getChildren().addAll(light, new AmbientLight(Color.color(0.3, 0.3, 0.3)));

//        // === Siatka + środek ===
//        Box grid = new Box(30000, 1, 30000);
//        grid.setMaterial(new PhongMaterial(Color.DARKGRAY));
//        root.getChildren().add(grid);


        // === Planety ===
        for (CelestialBody body : bodies) {
            Sphere sphere = new Sphere(getScaledRadius(body.getName()));
            PhongMaterial mat = new PhongMaterial(getColorForBody(body.getName()));
            sphere.setMaterial(mat);
            planetSpheres.add(sphere);
            root.getChildren().add(sphere);
        }

        updatePositions(currentStep);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(STEP_DELAY_MS), e -> {
            currentStep++;
            if (currentStep >= simulationData.length) currentStep = 0;
            updatePositions(currentStep);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Group main = new Group(subScene);
        Scene scene = new Scene(main);


        scene.setOnKeyPressed(e -> {
            double step = 100;

            switch (e.getCode()) {
                case W -> camera.setTranslateZ(camera.getTranslateZ() + step);
                case S -> camera.setTranslateZ(camera.getTranslateZ() - step);
                case A -> camera.setTranslateX(camera.getTranslateX() - step);
                case D -> camera.setTranslateX(camera.getTranslateX() + step);
                case Q -> camera.setTranslateY(camera.getTranslateY() - step);
                case E -> camera.setTranslateY(camera.getTranslateY() + step);
            }


            System.out.printf("🎥 Kamera: X=%.0f, Y=%.0f, Z=%.0f%n",
                    camera.getTranslateX(),
                    camera.getTranslateY(),
                    camera.getTranslateZ());
        });



        // === Obsługa myszy ===
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

        scene.setOnScroll((ScrollEvent e) -> {
            double zoom = e.getDeltaY();
            camera.setTranslateZ(camera.getTranslateZ() + zoom * 10);
        });

        scene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                System.out.printf("📷 Kamera: Z=%.0f, rotX=%.1f, rotY=%.1f%n",
                        camera.getTranslateZ(),
                        cameraX.getRotate(),
                        cameraY.getRotate());
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Solar System – Mouse Controlled View");
        primaryStage.show();
    }

    private void updatePositions(int step) {
        StateUtils.applyStateVector(simulationData[step], bodies);
        // Twarde zablokowanie Słońca w (0, 0, 0)
        bodies.get(0).getPosition().x = 0;
        bodies.get(0).getPosition().y = 0;
        bodies.get(0).getPosition().z = 0;

        for (int i = 0; i < bodies.size(); i++) {
            Vector3D pos = bodies.get(i).getPosition();

            // Wymuszenie: ruch w XZ, Y = nachylenie
            planetSpheres.get(i).setTranslateX(pos.z / SCALE);  // lewo-prawo
            planetSpheres.get(i).setTranslateZ(pos.y / SCALE);  // głębokość (Y z danych)
            planetSpheres.get(i).setTranslateY(pos.x / SCALE);  // nachylenie orbity (Z z danych)
        }


        Vector3D sun = bodies.get(0).getPosition();
        System.out.printf("Step %3d: Sun → x=%.2e, y=%.2e, z=%.2e%n", step, sun.x, sun.y, sun.z);
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
        return switch (name.toLowerCase()) {
            case "sun"     -> 696_340 / 500;
            case "mercury" -> 2_440 / 10.0;
            case "venus"   -> 6_052 / 10.0;
            case "earth"   -> 6_371 / 10.0;
            case "moon"    -> 1_737 / 5.0;
            case "mars"    -> 3_390 / 10.0;
            case "jupiter" -> 69_911 / 50.0;
            case "saturn"  -> 58_232 / 50.0;
            case "titan"   -> 2_575 / 10.0;
            case "uranus"  -> 25_362 / 30.0;
            case "neptune" -> 24_622 / 30.0;
            default        -> 300.0;
        };
    }


    public static void main(String[] args) {
        launch(args);
    }

    //Kamera: Z=-35893, rotX=185,7, rotY=-179,7
}
