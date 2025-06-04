package com.example.solar_system;

import com.example.lander.LanderSimulator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LanderVisualizer extends Application {

    // === CONFIGURATION: adjust as needed ===
    private static final int WIDTH = 1920;     // window width in pixels
    private static final int HEIGHT = 1080;    // window height in pixels

    // Simulation parameters (must match LanderSimulator.simulateFeedback()):
    private static final double DT = 0.5;      // [s] timestep
    private static final int MAX_STEPS = 10000;
    private static final double WIND = 0.0001; // km/s
    private static final double MASS = 10000.0; // kg (from JSON’s "initialMass")

    // Initial state (derived from your JSON “finalPosition” & “finalVelocity” relative to Titan):
    private static final double[] INIT_STATE = new double[] {
        0.0,                             // x = 0 km (above pad)
        1500.0,                          // y = 1500 km altitude
        1.48698278114141,                // xDot = 1.48698278114141 km/s (tangential orbital)
        0.0,                             // yDot = 0 km/s (no radial velocity)
        0.0,                             // θ = 0 rad (upright)
        0.0                              // θ̇ = 0 rad/s
    };

    // Playback speed factor (5× real time)
    private static final double SPEED_FACTOR = 20.0;

    // Will hold the full trajectory returned by the simulator:
    private double[][] trajectory;

    // Scaling factors to convert km → pixels
    private double verticalScale;    // px per km
    private double horizontalScale;  // px per km

    // Graphics canvas:
    private Canvas canvas;
    private Image backgroundImage;

    @Override
    public void start(Stage primaryStage) {
        // 1) Run the feedback-controlled landing simulation with the JSON-derived initial state:
        trajectory = LanderSimulator.simulateFeedback(
                INIT_STATE,
                DT,
                MAX_STEPS,
                WIND,
                MASS
        );

        if (trajectory.length <= 1) {
            System.err.println("Trajectory has length ≤ 1; nothing to animate.");
            System.exit(1);
        }

        // 2) Find minX, maxX, maxY over the entire trajectory, so we can compute km→pixel scales:
        double maxY = Double.NEGATIVE_INFINITY;
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        for (double[] row : trajectory) {
            double x = row[1];
            double y = row[2];
            if (y > maxY) maxY = y;
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
        }
        // Add 10% margin:
        maxY *= 1.1;
        if (maxY < 0.1) maxY = 0.1;

        double xRange = (maxX - minX) * 1.1;
        if (xRange < 0.1) xRange = 0.1;

        // Map y ∈ [0..maxY] → pixel ∈ [HEIGHT-50 .. 50]  (inverted vertically)
        verticalScale = (HEIGHT - 100) / maxY;
        // Map x ∈ [minX..maxX] → pixel ∈ [50 .. WIDTH-50]
        horizontalScale = (WIDTH - 100) / xRange;

        backgroundImage = new Image(
            getClass().getResource("/guiStyling/titan_surface.jpg").toExternalForm(),
            WIDTH,    // requestedWidth
            HEIGHT,   // requestedHeight
            false,    // preserveRatio? (false means stretch to exactly WIDTH×HEIGHT)
            true      // smooth?
        );

        // 3) Prepare a Canvas and draw the static background (ground line, title):
        canvas = new Canvas(WIDTH, HEIGHT);
        drawStaticBackground();

        // 4) Create a Timeline: one KeyFrame per trajectory row. Each fires at t = (DT*1000*index)/SPEED_FACTOR ms.
        Timeline timeline = new Timeline();
        timeline.setCycleCount(trajectory.length);

        for (int i = 0; i < trajectory.length; i++) {
            final int idx = i;
            KeyFrame frame = new KeyFrame(
                    Duration.millis((DT * 1000 * idx) / SPEED_FACTOR),
                    e -> drawLanderAtStep(idx)
            );
            timeline.getKeyFrames().add(frame);
        }


        timeline.play();

        // 5) Show the window:
        primaryStage.setTitle("Titan Lander Visualization (5× Speed)");
        primaryStage.setScene(new Scene(new javafx.scene.Group(canvas)));
        primaryStage.show();
    }

    /**
     * Draws a black background, a gray “ground” line at y=0, and a title.
     */
    private void drawStaticBackground() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Optionally clear to black first (not required if image covers all):
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw the Titan surface background image:
        gc.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT);

        // Draw ground line at y=0 → pixelY = mapYToPixel(0.0)
        double groundY = mapYToPixel(0.0);
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(4);
        gc.strokeLine(0, groundY, WIDTH, groundY);

        // Label:
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(14));
        gc.fillText("Titan Lander Descent (5× Playback Speed)", 20, 20);
        gc.fillText("Ground (y = 0 km)", 10, groundY - 10);
    }

    /**
     * Draws the lander as an orange circle (with a red “nose”) at trajectory[row],
     * then overlays the current time in the top-right.
     */
    private void drawLanderAtStep(int step) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        // Redraw background to clear the previous frame:
        drawStaticBackground();

        // Extract x, y, θ from this row of the trajectory:
        double x_km  = trajectory[step][1];
        double y_km  = trajectory[step][2];
        double theta = trajectory[step][5];

        // Convert to pixel coordinates:
        double pixelX = mapXToPixel(x_km);
        double pixelY = mapYToPixel(y_km);

        // Draw a circle of radius 8 px at (pixelX, pixelY), rotated by θ:
        double radius = 8.0;
        gc.save();
        gc.translate(pixelX, pixelY);
        gc.rotate(-Math.toDegrees(theta)); // θ=0 means “nose” straight up on screen
        gc.setFill(Color.ORANGE);
        gc.fillOval(-radius, -radius, radius * 2, radius * 2);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(-radius, -radius, radius * 2, radius * 2);
        gc.restore();

        // Draw a red line from the center to indicate “nose” direction:
        gc.save();
        gc.translate(pixelX, pixelY);
        gc.rotate(-Math.toDegrees(theta));
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(0, 0, 0, -radius * 1.5);
        gc.restore();

        // Draw the current simulation time:
        double t = trajectory[step][0];
        gc.setFill(Color.LIME);
        gc.setFont(Font.font(12));
        gc.fillText(String.format("t = %.1f s", t), WIDTH - 120, 20);
    }

    /** Map a y-coordinate (km) to a pixel Y (inverted so larger y = higher on screen). */
    private double mapYToPixel(double y_km) {
        // Pixel = (HEIGHT - 50) - y_km * verticalScale, clamped:
        double py = HEIGHT - 50 - (y_km * verticalScale);
        if (py < 50)           py = 50;            // don’t go above top margin
        if (py > HEIGHT - 50)  py = HEIGHT - 50;    // don’t go below ground line
        return py;
    }

    /** Map an x-coordinate (km) to a pixel X (centered horizontally, with 50px margins). */
    private double mapXToPixel(double x_km) {
        double centerX = WIDTH / 2.0;
        double px = centerX + (x_km * horizontalScale);
        if (px < 50)          px = 50;
        if (px > WIDTH - 50)  px = WIDTH - 50;
        return px;
    }

    public static void main(String[] args) {
        launch(args);
    }
}