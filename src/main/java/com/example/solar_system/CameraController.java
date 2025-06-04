package com.example.solar_system;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles camera movement, rotation, and zooming.
 * Supports free-flight navigation and basic mouse drag control.
 */
public class CameraController {

    private final PerspectiveCamera camera;
    private final Group cameraX = new Group();
    private final Group cameraY = new Group();
    private final Group cameraZ = new Group();

    private double anchorX, anchorY;
    private double anchorAngleX, anchorAngleY;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private double baseMoveSpeed = 250;
    private double shiftMultiplier = 8.0;

    private double velocityX = 0;
    private double velocityY = 0;
    private double velocityZ = 0;

    private double acceleration = 4000;
    private double deceleration = 5000;

    private double zoomVelocity = 0;
    private double zoomAcceleration = 10000;
    private double maxZoomSpeed = 400;

    private boolean movementEnabled = true;
    private SpectatorMode spectatorMode;
    private SpectatorMode cameraController;

    /**
     * Timer that continuously updates camera position and velocity.
     */
    private final AnimationTimer movementTimer = new AnimationTimer() {
        private long lastTime = -1;

        @Override
        public void handle(long now) {
            if (lastTime < 0) {
                lastTime = now;
                return;
            }

            double deltaTime = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            if (spectatorMode != null && spectatorMode.isLocked()) {
                return;
            }

            double moveSpeed = activeKeys.contains(KeyCode.SHIFT) ? baseMoveSpeed * shiftMultiplier : baseMoveSpeed;

            double targetVX = 0, targetVY = 0, targetVZ = 0;
            if (movementEnabled) {
                if (activeKeys.contains(KeyCode.W)) targetVZ += moveSpeed;
                if (activeKeys.contains(KeyCode.S)) targetVZ -= moveSpeed;
                if (activeKeys.contains(KeyCode.A)) targetVX -= moveSpeed;
                if (activeKeys.contains(KeyCode.D)) targetVX += moveSpeed;
                if (activeKeys.contains(KeyCode.E)) targetVY -= moveSpeed;
                if (activeKeys.contains(KeyCode.Q)) targetVY += moveSpeed;
            }

            velocityX = approach(velocityX, targetVX, (velocityX == 0 || Math.signum(velocityX) == Math.signum(targetVX)) ? acceleration * deltaTime : deceleration * deltaTime);
            velocityY = approach(velocityY, targetVY, (velocityY == 0 || Math.signum(velocityY) == Math.signum(targetVY)) ? acceleration * deltaTime : deceleration * deltaTime);
            velocityZ = approach(velocityZ, targetVZ, (velocityZ == 0 || Math.signum(velocityZ) == Math.signum(targetVZ)) ? acceleration * deltaTime : deceleration * deltaTime);

            double yaw = Math.toRadians(cameraY.getRotate());
            double forwardX = Math.sin(yaw);
            double forwardZ = Math.cos(yaw);
            double rightX = Math.cos(yaw);
            double rightZ = -Math.sin(yaw);

            cameraY.setTranslateX(cameraY.getTranslateX() + (velocityX * rightX + velocityZ * forwardX) * deltaTime);
            cameraY.setTranslateY(cameraY.getTranslateY() + velocityY * deltaTime);
            cameraY.setTranslateZ(cameraY.getTranslateZ() + (velocityX * rightZ + velocityZ * forwardZ) * deltaTime);

            zoomVelocity = approach(zoomVelocity, 0, zoomAcceleration * deltaTime);

            if (zoomVelocity != 0) {
                double pitch = Math.toRadians(cameraX.getRotate());
                double dx = Math.sin(yaw) * Math.cos(pitch);
                double dy = -Math.sin(pitch);
                double dz = Math.cos(yaw) * Math.cos(pitch);

                cameraY.setTranslateX(cameraY.getTranslateX() + dx * zoomVelocity * deltaTime);
                cameraY.setTranslateY(cameraY.getTranslateY() + dy * zoomVelocity * deltaTime);
                cameraY.setTranslateZ(cameraY.getTranslateZ() + dz * zoomVelocity * deltaTime);
            }
        }
    };

    /**
     * Smoothly approaches the target value with a given maximum change.
     */
    private double approach(double current, double target, double maxDelta) {
        double diff = target - current;
        if (diff > maxDelta) return current + maxDelta;
        if (diff < -maxDelta) return current - maxDelta;
        return target;
    }

    /**
     * Creates a camera controller for the given PerspectiveCamera.
     */
    public CameraController(PerspectiveCamera camera) {
        this.camera = camera;
        initializeCamera();
    }

    /**
     * Initializes the camera groups and nesting structure.
     */
    private void initializeCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(100000);
        spectatorMode = new SpectatorMode(camera, cameraX, cameraY, cameraZ);
        reset();

        cameraX.setRotationAxis(Rotate.X_AXIS);
        cameraY.setRotationAxis(Rotate.Y_AXIS);
        cameraZ.setRotationAxis(Rotate.Z_AXIS);


        cameraX.getChildren().add(camera);
        cameraY.getChildren().add(cameraX);
        cameraZ.getChildren().add(cameraY);
    }


    /**
     * Returns the root camera group used in the scene.
     */
    public Group getCameraGroup() {
        return cameraY;
    }

    public Group getCameraXGroup() {
        return cameraX;
    }

    public Group getCameraYGroup() {
        return cameraY;
    }

    public Group getCameraZGroup() {
        return cameraZ;
    }

    /**
     * Handles initial mouse press to capture drag start.
     */
    public void handleMousePressed(MouseEvent e) {
        anchorX = e.getSceneX();
        anchorY = e.getSceneY();
        anchorAngleX = cameraX.getRotate();
        anchorAngleY = cameraY.getRotate();
    }

    /**
     * Handles mouse dragging to rotate the camera view.
     */
    public void handleMouseDragged(MouseEvent e) {
        if (spectatorMode != null && spectatorMode.isLocked()) {
            return;
        }

        double deltaX = e.getSceneX() - anchorX;
        double deltaY = e.getSceneY() - anchorY;
        cameraX.setRotate(anchorAngleX - deltaY * 0.09);
        cameraY.setRotate(anchorAngleY + deltaX * 0.09);
    }

    /**
     * Applies a zooming force based on mouse wheel or input.
     */
    public void zoom(double zoomFactor) {
        if (spectatorMode != null && spectatorMode.isLocked()) {
            // Zoom disabled when locked
            return;
        }

        double targetZoomVelocity = zoomFactor * maxZoomSpeed;
        if (Math.abs(targetZoomVelocity) > Math.abs(zoomVelocity)) {
            zoomVelocity = targetZoomVelocity;
        }
    }

    /**
     * Starts the continuous movement updates.
     */
    public void startMovement() {
        movementTimer.start();
    }
    /**
     * Tracks a key being pressed for movement.
     */
    public void onKeyPressed(KeyCode code) {
        activeKeys.add(code);
    }
    /**
     * Tracks a key being released.
     */
    public void onKeyReleased(KeyCode code) {
        activeKeys.remove(code);
    }

    // uhhhhhf
    public double getRotationX() {
        return cameraX.getRotate();
    }

    public double getRotationY() {
        return cameraY.getRotate();
    }

    /**
     * Handles key events and toggles fullscreen or spectator mode.
     */
    public void setupKeyHandler(Scene scene, Stage primaryStage, SpectatorMode spectatorMode) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F11 -> toggleFullScreen(primaryStage);
                case H -> {
                    if (spectatorMode != null) {
                        if (spectatorMode.isLocked()) {
                            spectatorMode.unlock();
                            reset();
                            PopUps.showSpectatorExitPopup(primaryStage);
                        } else {
                            PopUps.showNotInSpectatorModePopup(primaryStage);
                        }
                    }
                }
                default -> onKeyPressed(event.getCode());
            }
        });

        scene.setOnKeyReleased(event -> onKeyReleased(event.getCode()));
    }



    // Turns the full screen off/on.
    public void toggleFullScreen(Stage stage) {
        if (stage != null) {
            stage.setFullScreen(!stage.isFullScreen());
        }
    }

    /**
     * Resets camera position and orientation to defaults.
     */
    public void reset() {
        cameraY.setTranslateX(-1195.7);
        cameraY.setTranslateY(-217.0);
        cameraY.setTranslateZ(-531);
        cameraX.setRotate(-8.9);
        cameraY.setRotate(76.9);
    }


}
