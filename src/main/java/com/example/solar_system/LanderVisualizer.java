package com.example.solar_system;

import com.example.lander.LanderSimulator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LanderVisualizer extends Application {

    // ─── CONFIGURATION ─────────────────────────────────────
    private static final int    CANVAS_WIDTH   = 1250;
    private static final int    CANVAS_HEIGHT  = 950;
    private static final double MARGIN         = 50;
    private double currentXKm = 0;

    private static final double DT             = 1.0;       // seconds per sim step
    private static final int    MAX_STEPS      = 200_000;
    private static final double WIND_KM_S      = 0.0001;
    private static final double MASS_KG       = 50_000.0;
    private static final double SPEED_FACTOR  = 1_000.0;   // playback speed

    private static final double[] INIT_STATE = {
        -2715.3163563925214,   // x
         (2875.004939539644 - 2575.0), // y
         0.5807482731466309,   // vx
        -1.6690138988461283,   // vy
         0.0,                  // θ
         0.0                   // θdot
    };
    // ────────────────────────────────────────────────────────

    private double[][] trajectory;
    private double   xScale, yScale, xOffset, yOffset;
    private Canvas   canvas;
    private Image    background;

    @Override
    public void start(Stage stage) {
        // 1) simulate
        trajectory = LanderSimulator.simulateCombined(
            INIT_STATE, DT, MAX_STEPS, WIND_KM_S, MASS_KG
        );
        if (trajectory.length < 2) {
            System.err.println("Not enough trajectory points!");
            System.exit(1);
        }

        // 2) compute world bounds
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (var row : trajectory) {
            double x = row[1], y = row[2];
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        // add padding
        double xRange = (maxX - minX) * 1.1;
        maxY *= 1.1;

        // 3) mapping parameters
        xScale  = (CANVAS_WIDTH  - 2*MARGIN) / xRange;
        yScale  = (CANVAS_HEIGHT - 2*MARGIN) / maxY;
        xOffset = CANVAS_WIDTH/2.0;
        yOffset = CANVAS_HEIGHT - MARGIN;

        // 4) load background
        background = new Image(
            getClass().getResource("/guiStyling/titan_surface.jpg").toExternalForm(),
            CANVAS_WIDTH, CANVAS_HEIGHT, false, true
        );

        // 5) set up Canvas
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        drawStatic(canvas.getGraphicsContext2D());

        // 6) animation
        AnimationTimer timer = new AnimationTimer() {
            private long last = 0;
            private int  idx  = 0;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                }
                double elapsedSec = (now - last) / 1e9 * SPEED_FACTOR;
                int steps = (int)(elapsedSec / DT);
                if (steps > 0) {
                    idx = Math.min(idx + steps, trajectory.length - 1);
                    last = now;
                    renderStep(idx);
                    if (idx >= trajectory.length - 1) stop();
                }
            }
        };
        timer.start();

        // 7) show
        stage.setTitle("Titan Lander Visualizer");
        stage.setScene(new Scene(new Group(canvas)));
        stage.show();
    }

    private void drawStatic(GraphicsContext gc) {
        // black background + surface
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gc.drawImage(background, 0, 0);

        // ground line
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(4);
        double groundY = mapY(0);
        gc.strokeLine(0, groundY, CANVAS_WIDTH, groundY);

        // title
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(18));
        gc.fillText("Titan Lander Descent", 20, 30);
    }

    private void renderStep(int step) {
        // grab the current state
        double[] s = trajectory[step];
        currentXKm = s[1];  // update camera focus

        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawStatic(gc);

        // draw trail
        gc.setStroke(Color.LIME);
        gc.setLineWidth(2);
        gc.beginPath();
        for (int i = 0; i <= step; i++) {
            double x = mapX(trajectory[i][1]);
            double y = mapY(trajectory[i][2]);
            if (i == 0) gc.moveTo(x, y);
            else       gc.lineTo(x, y);
        }
        gc.stroke();

        // draw lander at center
        double px = mapX(currentXKm);
        double py = mapY(s[2]);
        double theta = s[5];
        double r = 8;

        gc.save();
        gc.translate(px, py);
        gc.rotate(-Math.toDegrees(theta));
        gc.setFill(Color.ORANGE);
        gc.fillOval(-r, -r, 2*r, 2*r);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(-r, -r, 2*r, 2*r);
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeLine(0, 0, 0, -r * 1.5);
        gc.restore();

        // draw time
        gc.setFill(Color.LIME);
        gc.setFont(Font.font(14));
        gc.fillText(String.format("t = %.1f s", s[0]), CANVAS_WIDTH - 120, 30);
    }

    // now mapX shifts everything so that currentXKm → center of screen
    private double mapX(double xKm) {
        double dx = xKm - currentXKm;
        return CANVAS_WIDTH / 2.0 + dx * xScale;
    }

    private double mapY(double yKm) {
        // downward y positive in screen coords
        return yOffset - yKm * yScale;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
