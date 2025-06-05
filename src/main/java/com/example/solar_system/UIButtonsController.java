package com.example.solar_system;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UIButtonsController {

    @FXML private VBox buttonContainer;
    @FXML private SplitMenuButton spectatorMenuButton;

    @FXML private MenuItem itemMercury;
    @FXML private MenuItem itemVenus;
    @FXML private MenuItem itemEarth;
    @FXML private MenuItem itemMars;
    @FXML private MenuItem itemJupiter;
    @FXML private MenuItem itemSaturn;
    @FXML private MenuItem itemUranus;
    @FXML private MenuItem itemNeptune;
    @FXML private MenuItem itemSpaceShip;
    @FXML private ToggleButton toggleRunButton;

    private AnimationTimer orbitTimer;

    private SpectatorMode spectatorMode;
    private CameraController cameraController;
    private OrbitRendering orbitRenderer;
    private Stage stage;

    /**
     * Called externally by SolarSystemApp to initialize dependencies.
     */
    public void initialize(CameraController cameraController, OrbitRendering orbitRenderer, Stage stage) {
        this.cameraController = cameraController;
        this.orbitRenderer = orbitRenderer;
        this.stage = stage;
    }

    /**
     * Called externally to wire up the SpectatorMode and attach menu item handlers.
     */
    public void setSpectatorMovement(SpectatorMode spectatorMode) {
        this.spectatorMode = spectatorMode;
        setupSpectatorMenu();
    }

    /**
     * Called externally to provide the orbit animation timer and set up toggle logic.
     */
    public void setOrbitTimer(AnimationTimer orbitTimer) {
        this.orbitTimer = orbitTimer;

        setupRunToggle(); // First, attach event handler

        // Set initial state: simulation is paused by default
        toggleRunButton.setSelected(false);
        toggleRunButton.setText("Run Simulation");
    }

    private void setupSpectatorMenu() {
        itemMercury.setOnAction(e -> handleSpectatorSelection("Mercury"));
        itemVenus.setOnAction(e -> handleSpectatorSelection("Venus"));
        itemEarth.setOnAction(e -> handleSpectatorSelection("Earth"));
        itemMars.setOnAction(e -> handleSpectatorSelection("Mars"));
        itemJupiter.setOnAction(e -> handleSpectatorSelection("Jupiter"));
        itemSaturn.setOnAction(e -> handleSpectatorSelection("Saturn"));
        itemUranus.setOnAction(e -> handleSpectatorSelection("Uranus"));
        itemNeptune.setOnAction(e -> handleSpectatorSelection("Neptune"));
        itemSpaceShip.setOnAction(e -> handleSpectatorSelection("Noah's ark"));
    }

    @FXML
    public void setupRunToggle() {
        toggleRunButton.setOnAction(e -> {
            if (toggleRunButton.isSelected()) {
                toggleRunButton.setText("Pause Simulation");
                if (orbitTimer != null) orbitTimer.start();
            } else {
                toggleRunButton.setText("Run Simulation");
                if (orbitTimer != null) orbitTimer.stop();
            }
        });
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

    private void handleSpectatorSelection(String name) {
        if (spectatorMode != null) {
            spectatorMode.setFollowedByName(name);
            PopUps.showSpectatorPopup(name, stage);
        }
    }
}
