package com.example.solar_system;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages overlay labels for 3D objects in a JavaFX scene,
 * projecting them into screen space relative to the camera.
 *
 * This version adds position update smoothing to reduce flicker.
 */
public class LabelManager {

    private final Map<Node, Label> labels = new HashMap<>();
    private final Pane labelPane;
    private final PerspectiveCamera camera;
    private final SubScene subScene;
    private static final double LABEL_OFFSET_Y = -25;

    // Threshold in pixels: label position must move more than this to trigger repositioning
    private static final double MIN_MOVE_THRESHOLD = 0.5;



    public LabelManager(Pane labelPane, PerspectiveCamera camera, SubScene subScene) {
        if (labelPane == null || camera == null || subScene == null) {
            throw new NullPointerException("Arguments must not be null");
        }
        this.labelPane = labelPane;
        this.camera = camera;
        this.subScene = subScene;
    }

    /**
     * Adds a label for the given 3D node.
     *
     * @param target the 3D Node to track
     * @param text the text to display in the label
     */
    public void addLabel(Node target, String text) {
        if (target == null || text == null) {
            throw new NullPointerException("Target node and text must not be null");
        }

        // * :D
//        if (text.equalsIgnoreCase("Spaceship")) {
//            text = "Noah's ark";
//        }

        Label label = new Label(text);
        label.getStyleClass().add("object-label");
        labels.put(target, label);
        labelPane.getChildren().add(label);
    }

    /**
     * Updates the positions of all labels based on their 3D nodes and the current camera view.
     *
     * Should be called inside Platform.runLater() to ensure smooth updates after layout pass.
     */
    public void updateLabelPositions() {
        for (Map.Entry<Node, Label> entry : labels.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();

            Point3D screenPos = computeScreenPosition(node);
            if (screenPos == null) {
                label.setVisible(false);
                continue;
            }

            Point2D paneCoords = labelPane.screenToLocal(screenPos.getX(), screenPos.getY());

            boolean insidePane = paneCoords.getX() >= 0 && paneCoords.getX() <= labelPane.getWidth()
                    && paneCoords.getY() >= 0 && paneCoords.getY() <= labelPane.getHeight();

            if (insidePane) {
                label.setVisible(true);

                double newX = paneCoords.getX() - label.getWidth() / 2;
                double newY = paneCoords.getY() + LABEL_OFFSET_Y;

                // Only update position if moved significantly (to reduce flicker)
                if (Math.abs(label.getLayoutX() - newX) > MIN_MOVE_THRESHOLD
                        || Math.abs(label.getLayoutY() - newY) > MIN_MOVE_THRESHOLD) {
                    label.setLayoutX(newX);
                    label.setLayoutY(newY);
                }
            } else {
                label.setVisible(false);
            }
        }
    }

    /**
     * Computes the screen position (in pixels) of the center of the given 3D node.
     *
     * @param node the 3D Node
     * @return screen position as Point3D (z always 0), or null if not visible on screen
     */
    private Point3D computeScreenPosition(Node node) {
        Bounds boundsInScreen = node.localToScreen(node.getBoundsInLocal());
        if (boundsInScreen == null) return null;

        double screenX = (boundsInScreen.getMinX() + boundsInScreen.getMaxX()) / 2;
        double screenY = (boundsInScreen.getMinY() + boundsInScreen.getMaxY()) / 2;
        return new Point3D(screenX, screenY, 0);
    }


}
