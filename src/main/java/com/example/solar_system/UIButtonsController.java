package com.example.solar_system;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// * Used in SceneBuilder to allow modifications to the solarSystemApp
public class UIButtonsController {

    @FXML private VBox buttonContainer;

    private CameraController cameraController;
    private OrbitRendering orbitRenderer;
    private Stage stage;

    public void initialize(CameraController cameraController, OrbitRendering orbitRenderer, Stage stage) {
        this.cameraController = cameraController;
        this.orbitRenderer = orbitRenderer;
        this.stage = stage;
    }

    @FXML
    private void handleResetCamera() {
        if (cameraController != null) {
            cameraController.reset();
        }
    }

    @FXML
    private void handleToggleOrbits() {
        if (orbitRenderer != null) {
            orbitRenderer.setVisible(!orbitRenderer.getOrbitGroup().isVisible());
        }
    }

    @FXML
    private void handleToggleFullScreen() {
        if (stage != null) {
            stage.setFullScreen(!stage.isFullScreen());
        }
    }
}
