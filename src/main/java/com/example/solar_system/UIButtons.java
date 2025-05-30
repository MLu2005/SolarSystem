package com.example.solar_system;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UIButtons extends VBox {
    public UIButtons(Stage stage, CameraController cameraController, OrbitRendering orbitRenderer) {
        setSpacing(10);
        setTranslateX(20);
        setTranslateY(20);
        setPickOnBounds(false);

        Button reset = new Button("Reset Camera");
        Button toggleOrbits = new Button("Toggle Orbits");
        Button fullScreen = new Button("Toggle FullScreen");

        reset.setOnAction(e -> cameraController.reset());
        toggleOrbits.setOnAction(e -> orbitRenderer.setVisible(!orbitRenderer.getOrbitGroup().isVisible()));
        fullScreen.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));

        getChildren().addAll(reset, toggleOrbits, fullScreen);
    }
}
