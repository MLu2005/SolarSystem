package com.example.solar_system;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;


import java.util.HashMap;
import java.util.Map;

/**
 * Handles spectator mode, locking the camera to follow a specific object.
 * The camera is locked so that the target object is always at the center of the screen,
 * viewed from above and slightly from the side. Movement and looking around are disabled.
 */
public class SpectatorMode {

    private final PerspectiveCamera camera;
    private final Group cameraX;
    private final Group cameraY;
    private final Group cameraZ;

    private Node target;

    private final AnimationTimer lockTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (target != null) {
                updateCameraPosition();
            }
        }
    };

    private Map<String, Node> namedTargets = new HashMap<>();

    public SpectatorMode(PerspectiveCamera camera, Group cameraX, Group cameraY, Group cameraZ) {
        this.camera = camera;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
    }

    public void setNamedTargets(Map<String, Node> targets) {
        this.namedTargets = targets;
    }

    public void setFollowedByName(String name) {
        Node node = namedTargets.get(name);
        if (node != null) {
                lockOnto(node);
                if (namedTargets.get(name).equals("Noah's ark")) {
                    lockOnto(node);
                }
        } else {
            System.out.println("No object with that name.");
        }
    }

    public void lockOnto(Node target) {
        this.target = target;
        updateCameraPosition();
        lockTimer.start();
    }


    public void unlock() {
        this.target = null;
        lockTimer.stop();
    }

    private void updateCameraPosition() {
        if (target == null) return;

        // *  Gets target's world coordinates
        double tx = target.getTranslateX();
        double ty = target.getTranslateY();
        double tz = target.getTranslateZ();

        //* Puts camera position slightly behind and above target to allow visibility
        double camX = tx + 0;
        double camY = ty - 270;
        double camZ = tz - 270;

        // * Set camera group position
        cameraY.setTranslateX(camX);
        cameraY.setTranslateY(camY);
        cameraY.setTranslateZ(camZ);

        // * Clear rotations
        cameraY.getTransforms().clear();
        cameraX.getTransforms().clear();
        cameraZ.getTransforms().clear();
        camera.getTransforms().clear();

        // * Calculate direction vector from camera to target
        double dx = tx - camX;
        double dy = ty - camY;
        double dz = tz - camZ;

        // * Calculate yaw and pitch
        double yaw = Math.toDegrees(Math.atan2(dx, dz)); // horizontal angle
        double distance = Math.sqrt(dx*dx + dz*dz);
        double pitch = -Math.toDegrees(Math.atan2(dy, distance)); // vertical angle

        // * Applys rotations to camera group
        cameraY.getTransforms().add(new Rotate(yaw, Rotate.Y_AXIS));
        cameraX.getTransforms().add(new Rotate(pitch, Rotate.X_AXIS));
    }


    public boolean isLocked() {
        return target != null;
    }

}
