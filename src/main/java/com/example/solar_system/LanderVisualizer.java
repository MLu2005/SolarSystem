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
    private static final int CANVAS_WIDTH = 1250;
    private static final int CANVAS_HEIGHT = 950;
    private static final double MARGIN = 50;
    private double currentHorizontalPositionKm = 0;

    private static final double TIME_STEP_SECONDS = 1.0;
    private static final int MAXIMUM_STEPS = 200000;
    private static final double WIND_SPEED_KM_PER_S = 0.0001;
    private static final double LANDER_MASS_KG = 50000.0;
    private static final double PLAYBACK_SPEED_FACTOR = 1000.0;

    private static final double[] INITIAL_STATE = {
        -2715.3163563925214,
        2875.004939539644 - 2575.0,
        0.5807482731466309,
        -1.6690138988461283,
        0.0,
        0.0
    };

    private double[][] trajectoryData;
    private double horizontalScale;
    private double verticalScale;
    private double horizontalOffset;
    private double verticalOffset;
    private Canvas drawingCanvas;
    private Image backgroundImage;

    @Override
    public void start(Stage primaryStage) {
        trajectoryData = LanderSimulator.simulateCombined(
            INITIAL_STATE,
            TIME_STEP_SECONDS,
            MAXIMUM_STEPS,
            WIND_SPEED_KM_PER_S,
            LANDER_MASS_KG
        );

        if (trajectoryData.length < 2) {
            System.err.println("Not enough trajectory points!");
            System.exit(1);
        }

        double minHorizontalPosition = Double.POSITIVE_INFINITY;
        double maxHorizontalPosition = Double.NEGATIVE_INFINITY;
        double maxVerticalPosition = Double.NEGATIVE_INFINITY;
        
        for (var state : trajectoryData) {
            double horizontalPos = state[1];
            double verticalPos = state[2];
            minHorizontalPosition = Math.min(minHorizontalPosition, horizontalPos);
            maxHorizontalPosition = Math.max(maxHorizontalPosition, horizontalPos);
            maxVerticalPosition = Math.max(maxVerticalPosition, verticalPos);
        }

        double horizontalRange = (maxHorizontalPosition - minHorizontalPosition) * 1.1;
        maxVerticalPosition *= 1.1;

        horizontalScale = (CANVAS_WIDTH - 2 * MARGIN) / horizontalRange;
        verticalScale = (CANVAS_HEIGHT - 2 * MARGIN) / maxVerticalPosition;
        horizontalOffset = CANVAS_WIDTH / 2.0;
        verticalOffset = CANVAS_HEIGHT - MARGIN;

        backgroundImage = new Image(
            getClass().getResource("/guiStyling/titan_surface.jpg").toExternalForm(),
            CANVAS_WIDTH, CANVAS_HEIGHT, false, true
        );

        drawingCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        drawStaticElements(drawingCanvas.getGraphicsContext2D());

        AnimationTimer animationTimer = new AnimationTimer() {
            private long lastUpdateTime = 0;
            private int currentStepIndex = 0;

            @Override
            public void handle(long currentTime) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = currentTime;
                }
                double elapsedSeconds = (currentTime - lastUpdateTime) / 1e9 * PLAYBACK_SPEED_FACTOR;
                int stepsToAdvance = (int)(elapsedSeconds / TIME_STEP_SECONDS);
                
                if (stepsToAdvance > 0) {
                    currentStepIndex = Math.min(currentStepIndex + stepsToAdvance, trajectoryData.length - 1);
                    lastUpdateTime = currentTime;
                    renderCurrentState(currentStepIndex);
                    if (currentStepIndex >= trajectoryData.length - 1) {
                        stop();
                    }
                }
            }
        };
        animationTimer.start();

        primaryStage.setTitle("Titan Lander Visualizer");
        primaryStage.setScene(new Scene(new Group(drawingCanvas)));
        primaryStage.show();
    }

    private void drawStaticElements(GraphicsContext graphicsContext) {
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        graphicsContext.drawImage(backgroundImage, 0, 0);

        graphicsContext.setStroke(Color.DARKGRAY);
        graphicsContext.setLineWidth(4);
        double groundLevelY = mapVerticalPosition(0);
        graphicsContext.strokeLine(0, groundLevelY, CANVAS_WIDTH, groundLevelY);

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.setFont(Font.font(18));
        graphicsContext.fillText("Titan Lander Descent", 20, 30);
    }

    private void renderCurrentState(int stepIndex) {
        double[] currentState = trajectoryData[stepIndex];
        currentHorizontalPositionKm = currentState[1];

        GraphicsContext graphicsContext = drawingCanvas.getGraphicsContext2D();
        drawStaticElements(graphicsContext);

        graphicsContext.setStroke(Color.LIME);
        graphicsContext.setLineWidth(2);
        graphicsContext.beginPath();
        
        for (int i = 0; i <= stepIndex; i++) {
            double x = mapHorizontalPosition(trajectoryData[i][1]);
            double y = mapVerticalPosition(trajectoryData[i][2]);
            if (i == 0) {
                graphicsContext.moveTo(x, y);
            } else {
                graphicsContext.lineTo(x, y);
            }
        }
        graphicsContext.stroke();

        double landerX = mapHorizontalPosition(currentHorizontalPositionKm);
        double landerY = mapVerticalPosition(currentState[2]);
        double tiltAngle = currentState[5];
        double landerRadius = 8;

        graphicsContext.save();
        graphicsContext.translate(landerX, landerY);
        graphicsContext.rotate(-Math.toDegrees(tiltAngle));
        graphicsContext.setFill(Color.ORANGE);
        graphicsContext.fillOval(-landerRadius, -landerRadius, 2 * landerRadius, 2 * landerRadius);
        graphicsContext.setStroke(Color.WHITE);
        graphicsContext.strokeOval(-landerRadius, -landerRadius, 2 * landerRadius, 2 * landerRadius);
        graphicsContext.setStroke(Color.RED);
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeLine(0, 0, 0, -landerRadius * 1.5);
        graphicsContext.restore();

        graphicsContext.setFill(Color.LIME);
        graphicsContext.setFont(Font.font(14));
        graphicsContext.fillText(String.format("t = %.1f s", currentState[0]), CANVAS_WIDTH - 120, 30);
    }

    private double mapHorizontalPosition(double horizontalPositionKm) {
        double positionDifference = horizontalPositionKm - currentHorizontalPositionKm;
        return CANVAS_WIDTH / 2.0 + positionDifference * horizontalScale;
    }

    private double mapVerticalPosition(double verticalPositionKm) {
        return verticalOffset - verticalPositionKm * verticalScale;
    }

    public static void main(String[] args) {
        launch(args);
    }
}