package com.example.solar_system;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;

import java.util.HashSet;
import java.util.Set;

/**
 * Controls a 3D camera in a JavaFX scene.
 * Allows rotation with the mouse and smooth movement with keyboard keys.
 * Holding L-SHIFT increases speed.
 * WASD movements.
 * Q down
 * E up
 */
public class CameraController {

    private final PerspectiveCamera camera;
    private final Group cameraX = new Group();
    private final Group cameraY = new Group();

    private double anchorX, anchorY;
    private double anchorAngleX, anchorAngleY;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private double baseMoveSpeed = 250;  // max units per second (normal speed)
    private double shiftMultiplier = 8.0; // speed multiplier when L-SHIFT held

    // * Velocity components for acceleration/deceleration
    private double velocityX = 0;
    private double velocityY = 0;
    private double velocityZ = 0;

    private double acceleration = 4000;  // units per second^2, controls how fast velocity ramps up
    private double deceleration = 5000;  // units per second^2, controls how fast velocity ramps down

    private double zoomVelocity = 0;
    private double zoomAcceleration = 10000;  // controls how fast zoom velocity ramps up/down
    private double maxZoomSpeed = 400;

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

            // * Applies move speed, increase if L-SHIFT is held
            double moveSpeed = activeKeys.contains(KeyCode.SHIFT) ? baseMoveSpeed * shiftMultiplier : baseMoveSpeed;

            // * Determine target velocity from key inputs
            double targetVX = 0, targetVY = 0, targetVZ = 0;
            if (activeKeys.contains(KeyCode.W)) targetVZ += moveSpeed;
            if (activeKeys.contains(KeyCode.S)) targetVZ -= moveSpeed;
            if (activeKeys.contains(KeyCode.A)) targetVX -= moveSpeed;
            if (activeKeys.contains(KeyCode.D)) targetVX += moveSpeed;
            if (activeKeys.contains(KeyCode.E)) targetVY -= moveSpeed;
            if (activeKeys.contains(KeyCode.Q)) targetVY += moveSpeed;

            // * Approaches target velocity (acceleration/deceleration)
            velocityX = approach(velocityX, targetVX, (velocityX == 0 || Math.signum(velocityX) == Math.signum(targetVX)) ? acceleration * deltaTime : deceleration * deltaTime);
            velocityY = approach(velocityY, targetVY, (velocityY == 0 || Math.signum(velocityY) == Math.signum(targetVY)) ? acceleration * deltaTime : deceleration * deltaTime);
            velocityZ = approach(velocityZ, targetVZ, (velocityZ == 0 || Math.signum(velocityZ) == Math.signum(targetVZ)) ? acceleration * deltaTime : deceleration * deltaTime);

            // * Calculate directional vectors based on current yaw rotation
            double yaw = Math.toRadians(cameraY.getRotate());
            double forwardX = Math.sin(yaw);
            double forwardZ = Math.cos(yaw);
            double rightX = Math.cos(yaw);
            double rightZ = -Math.sin(yaw);

            // * Update camera position based on smooth velocities
            cameraY.setTranslateX(cameraY.getTranslateX() + (velocityX * rightX + velocityZ * forwardX) * deltaTime);
            cameraY.setTranslateY(cameraY.getTranslateY() + velocityY * deltaTime);
            cameraY.setTranslateZ(cameraY.getTranslateZ() + (velocityX * rightZ + velocityZ * forwardZ) * deltaTime);


            zoomVelocity = approach(zoomVelocity, 0, zoomAcceleration * deltaTime);

            // * Zoom in and out with the camera’s forward direction
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

        private double approach(double current, double target, double maxDelta) {
        double diff = target - current;
        if (diff > maxDelta) return current + maxDelta;
        if (diff < -maxDelta) return current - maxDelta;
        return target;
    }

    public CameraController(PerspectiveCamera camera) {
        this.camera = camera;
        initializeCamera();
    }

    private void initializeCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(100000);

        reset();

        cameraX.setRotationAxis(Rotate.X_AXIS);
        cameraY.setRotationAxis(Rotate.Y_AXIS);

        cameraX.getChildren().add(camera);
        cameraY.getChildren().add(cameraX);
    }

    public Group getCameraGroup() {
        return cameraY;
    }

    public void handleMousePressed(MouseEvent e) {
        anchorX = e.getSceneX();
        anchorY = e.getSceneY();
        anchorAngleX = cameraX.getRotate();
        anchorAngleY = cameraY.getRotate();
    }

    public void handleMouseDragged(MouseEvent e) {
        double deltaX = e.getSceneX() - anchorX;
        double deltaY = e.getSceneY() - anchorY;
        cameraX.setRotate(anchorAngleX - deltaY * 0.09);
        cameraY.setRotate(anchorAngleY + deltaX * 0.09);
    }

    /**
     * Smoothly adjusts zoom velocity based on zoom factor input.
     * @param zoomFactor positive to zoom in, negative to zoom out
     */
    public void zoom(double zoomFactor) {
        // Map zoomFactor input to target zoom velocity, scaled by maxZoomSpeed
        // Clamp zoomVelocity to maxZoomSpeed
        double targetZoomVelocity = zoomFactor * maxZoomSpeed;
        if (Math.abs(targetZoomVelocity) > Math.abs(zoomVelocity)) {
            zoomVelocity = targetZoomVelocity;
        } else {
            // Let zoomVelocity decay smoothly in timer
        }
    }

    public void startMovement() {
        movementTimer.start();
    }

    public void onKeyPressed(KeyCode code) {
        activeKeys.add(code);
    }

    public void onKeyReleased(KeyCode code) {
        activeKeys.remove(code);
    }

    public double getRotationX() {
        return cameraX.getRotate();
    }

    public double getRotationY() {
        return cameraY.getRotate();
    }
    // * Starting positions and reset position.
    public void reset() {
        cameraY.setTranslateX(-1195.7);
        cameraY.setTranslateY(-217.0);
        cameraY.setTranslateZ(-531);
        cameraX.setRotate(-8.9);
        cameraY.setRotate(76.9);
    }
}
