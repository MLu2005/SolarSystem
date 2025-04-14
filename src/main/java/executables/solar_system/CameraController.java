package executables.solar_system;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.PerspectiveCamera;

public class CameraController {
    private final Group rotateXGroup;
    private final Group rotateYGroup;
    private double anchorX, anchorY;
    private double anchorAngleX;
    private double anchorAngleY;

    public CameraController(Group rotateYGroup, Group rotateXGroup) {
        this.rotateYGroup = rotateYGroup;
        this.rotateXGroup = rotateXGroup;
    }

    public void onMousePressed(double x, double y) {
        anchorX = x;
        anchorY = y;
        anchorAngleX = rotateXGroup.getRotate();
        anchorAngleY = rotateYGroup.getRotate();
    }

    public void onMouseDragged(double x, double y) {
        double deltaX = x - anchorX;
        double deltaY = y - anchorY;

        rotateYGroup.setRotationAxis(Rotate.Y_AXIS);
        rotateYGroup.setRotate(anchorAngleY + deltaX / 2.0);

        rotateXGroup.setRotationAxis(Rotate.X_AXIS);
        rotateXGroup.setRotate(anchorAngleX - deltaY / 2.0);
    }

    public void onScroll(PerspectiveCamera camera, double delta) {
        double zoomSpeed = 100;
        camera.setTranslateZ(camera.getTranslateZ() + delta * zoomSpeed);
    }

    public void logCameraInfo(PerspectiveCamera camera) {
        System.out.printf("📷 Kamera: Z=%.0f, rotX=%.1f, rotY=%.1f%n",
                camera.getTranslateZ(),
                rotateXGroup.getRotate(),
                rotateYGroup.getRotate());
    }
}
