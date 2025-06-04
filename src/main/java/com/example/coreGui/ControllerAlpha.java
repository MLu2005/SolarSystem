package com.example.coreGui;

import com.example.ode_gui.odeGui;
import com.example.solar_system.SolarSystemApp;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerAlpha implements Initializable {

    @FXML
    private AnchorPane drawerPane;

    @FXML
    private Label drawerImage;

    @FXML
    private ImageView exit;

    @FXML
    private ImageView minimize;

    @FXML
    private AnchorPane topBar;

    private boolean drawerOpen = false;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exit.setOnMouseClicked(event -> System.exit(0));

        minimize.setOnMouseClicked(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setIconified(true);
        });

        // Hide drawer initially
        TranslateTransition hideDrawer = new TranslateTransition(Duration.seconds(0.3), drawerPane);
        hideDrawer.setByX(-600);
        hideDrawer.play();

        // Toggle drawer on click
        drawerImage.setOnMouseClicked(event -> {
            TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), drawerPane);
            if (drawerOpen) {
                transition.setByX(-600);
                drawerOpen = false;
            } else {
                transition.setByX(600);
                drawerOpen = true;
            }
            transition.play();
        });

        // Drag window
        topBar.setOnMousePressed(this::handleMousePressed);
        topBar.setOnMouseDragged(this::handleMouseDragged);
    }

    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    private void launchSolarSystem() {
        Thread thread = new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    try {
                        SolarSystemApp app = new SolarSystemApp();
                        Stage newStage = new Stage();
                        app.start(newStage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void launchODE() {
        Thread thread = new Thread(() -> {
            try {
                odeGui.startNewWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
