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
 * Allows rotation with the mouse and movement with keyboard keys.
 */
public class CameraController {

    private final PerspectiveCamera camera;
    private final Group cameraX = new Group();
    private final Group cameraY = new Group();

    private double anchorX, anchorY;
    private double anchorAngleX, anchorAngleY;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private double moveSpeed = 3150;  // Units per second

    private final AnimationTimer movementTimer = new AnimationTimer() {
        private long lastTime = -1;

        /**
         * Updates camera position based on currently pressed keys.
         */
        @Override
        public void handle(long now) {
            if (lastTime < 0) {
                lastTime = now;
                return;
            }

            double deltaTime = (now - lastTime) / 1_000_000_000.0;
            lastTime = now;

            double dx = 0, dy = 0, dz = 0;

            if (activeKeys.contains(KeyCode.W)) dz += moveSpeed * deltaTime;
            if (activeKeys.contains(KeyCode.S)) dz -= moveSpeed * deltaTime;
            if (activeKeys.contains(KeyCode.A)) dx -= moveSpeed * deltaTime;
            if (activeKeys.contains(KeyCode.D)) dx += moveSpeed * deltaTime;
            if (activeKeys.contains(KeyCode.E)) dy -= moveSpeed * deltaTime;  // UP
            if (activeKeys.contains(KeyCode.Q)) dy += moveSpeed * deltaTime; // DOWN


            double yaw = Math.toRadians(cameraY.getRotate());

            double forwardX = Math.sin(yaw);
            double forwardZ = Math.cos(yaw);
            double rightX = Math.cos(yaw);
            double rightZ = -Math.sin(yaw);

            cameraY.setTranslateX(cameraY.getTranslateX() + (dx * rightX + dz * forwardX));
            cameraY.setTranslateY(cameraY.getTranslateY() + dy);
            cameraY.setTranslateZ(cameraY.getTranslateZ() + (dx * rightZ + dz * forwardZ));
        }


    };
    /**
     * Creates a new CameraController for the given camera.
     *
     * @param camera the PerspectiveCamera to control
     */
    public CameraController(PerspectiveCamera camera) {
        this.camera = camera;
        initializeCamera();
    }
    /**
     * Initializes camera settings and group hierarchy.
     */
    private void initializeCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(100000);

        reset();

        cameraX.setRotationAxis(Rotate.X_AXIS);
        cameraY.setRotationAxis(Rotate.Y_AXIS);

        cameraX.getChildren().add(camera);
        cameraY.getChildren().add(cameraX);
    }
    /**
     * Returns the camera group that should be added to the scene.
     *
     * @return the root group containing the camera
     */
    public Group getCameraGroup() {
        return cameraY;
    }

    /**
     * Stores the current mouse position and camera rotation when a mouse press occurs.
     *
     * @param e the mouse press event
     */
    public void handleMousePressed(MouseEvent e) {
        anchorX = e.getSceneX();
        anchorY = e.getSceneY();
        anchorAngleX = cameraX.getRotate();
        anchorAngleY = cameraY.getRotate();
    }

    /**
     * Updates the camera's rotation based on mouse drag.
     *
     * @param e the mouse drag event
     */
    public void handleMouseDragged(MouseEvent e) {
        double deltaX = e.getSceneX() - anchorX;
        double deltaY = e.getSceneY() - anchorY;
        cameraX.setRotate(anchorAngleX - deltaY * 0.09);
        cameraY.setRotate(anchorAngleY + deltaX * 0.09);
    }

    /**
     * Moves the camera forward or backward based on zoom factor.
     *
     * @param zoomFactor positive to zoom in, negative to zoom out
     */
    public void zoom(double zoomFactor) {
        double pitch = Math.toRadians(cameraX.getRotate());
        double yaw = Math.toRadians(cameraY.getRotate());

        double dx = Math.sin(yaw) * Math.cos(pitch);
        double dy = -Math.sin(pitch);
        double dz = Math.cos(yaw) * Math.cos(pitch);

        cameraY.setTranslateX(cameraY.getTranslateX() + dx * zoomFactor * 50);
        cameraY.setTranslateY(cameraY.getTranslateY() + dy * zoomFactor * 50);
        cameraY.setTranslateZ(cameraY.getTranslateZ() + dz * zoomFactor * 50);
    }

    /**
     * Starts continuous camera movement handling.
     */
    public void startMovement() {
        movementTimer.start();
    }

    /**
     * Registers a key as pressed for movement.
     *
     * @param code the key code
     */
    public void onKeyPressed(KeyCode code) {
        activeKeys.add(code);
    }

    /**
     * Unregisters a key as released for movement.
     *
     * @param code the key code
     */
    public void onKeyReleased(KeyCode code) {
        activeKeys.remove(code);
    }

    /**
     * Returns the current rotation around the X axis.
     *
     * @return X rotation in degrees
     */
    public double getRotationX() {
        return cameraX.getRotate();
    }

    /**
     * Returns the current rotation around the Y axis.
     *
     * @return Y rotation in degrees
     */
    public double getRotationY() {
        return cameraY.getRotate();
    }


    /**
     * Resets the camera position and rotation to the default state.
     */
    public void reset() {
        cameraY.setTranslateX(-1362.1);
        cameraY.setTranslateY(-5555.7);
        cameraY.setTranslateZ(-11820.0);
        cameraX.setRotate(-26.3);
        cameraY.setRotate(6.2);
    }
}
