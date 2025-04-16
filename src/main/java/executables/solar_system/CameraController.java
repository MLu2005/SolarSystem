package executables.solar_system;

import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.Group;

/**
 * CameraController handles mouse-based camera manipulation.
 * It allows for rotating the view around the scene and zooming in/out using mouse input.
 */
public class CameraController {
    private final Group rotateXGroup;
    private final Group rotateYGroup;
    private double anchorX, anchorY;
    private double anchorAngleX;
    private double anchorAngleY;

    /**
     * Initializes the camera controller with the rotation groups.
     * rotateYGroup rotates horizontally (Y-axis), rotateXGroup rotates vertically (X-axis).
     */
    public CameraController(Group rotateYGroup, Group rotateXGroup) {
        this.rotateYGroup = rotateYGroup;
        this.rotateXGroup = rotateXGroup;
    }

    /**
     * Saves the initial mouse position and camera rotation angles.
     * Called when the mouse button is pressed.
     */
    public void onMousePressed(double x, double y) {
        anchorX = x;
        anchorY = y;
        anchorAngleX = rotateXGroup.getRotate();
        anchorAngleY = rotateYGroup.getRotate();
    }

    /**
     * Rotates the camera based on the mouse movement.
     * Horizontal movement rotates around Y-axis, vertical movement around X-axis.
     */
    public void onMouseDragged(double x, double y) {
        double deltaX = x - anchorX;
        double deltaY = y - anchorY;

        rotateYGroup.setRotationAxis(Rotate.Y_AXIS);
        rotateYGroup.setRotate(anchorAngleY + deltaX / 2.0);

        rotateXGroup.setRotationAxis(Rotate.X_AXIS);
        rotateXGroup.setRotate(anchorAngleX - deltaY / 2.0);
    }

    /**
     * Zooms the camera in or out based on scroll wheel movement.
     */
    public void onScroll(PerspectiveCamera camera, double delta) {
        double zoomSpeed = 100;
        camera.setTranslateZ(camera.getTranslateZ() + delta * zoomSpeed);
    }

    /**
     * Prints the current camera position and rotation angles to the console.
     */
    public void logCameraInfo(PerspectiveCamera camera) {
        System.out.printf("📷 Camera Info: Z=%.0f, rotX=%.1f, rotY=%.1f%n",
                camera.getTranslateZ(),
                rotateXGroup.getRotate(),
                rotateYGroup.getRotate());
    }
}
