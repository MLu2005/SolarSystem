package com.example.solar_system;

import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Utility class for showing various informational popups.
 */
public class PopUps {

    /**
     * Displays a popup with the current camera position and rotation.
     *
     * @param controller   The CameraController instance.
     * @param primaryStage The owner stage for proper z-order.
     */
    public static void showCameraLocation(CameraController controller, Stage primaryStage) {
        var camGroup = controller.getCameraGroup();

        double z = camGroup.getTranslateZ();
        double rotX = controller.getRotationX();
        double rotY = controller.getRotationY();
        double offsetX = camGroup.getTranslateX();
        double offsetY = camGroup.getTranslateY();

        String location = String.format(
                "📷 Camera Position:\n\nZ = %.0f\nrotX = %.1f°\nrotY = %.1f°\noffsetX = %.1f\noffsetY = %.1f",
                z, rotX, rotY, offsetX, offsetY
        );

        Alert cameraLocation = new Alert(Alert.AlertType.INFORMATION);
        cameraLocation.setTitle("Camera Location");
        cameraLocation.setHeaderText("Current Camera Location");
        cameraLocation.setContentText(location);

        Stage dialogStage = (Stage) cameraLocation.getDialogPane().getScene().getWindow();
        IconSetter.setIcons(dialogStage, "/styles/locationIcon.png");

        // * This part ensures that the pop-up is pushed to the front!
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.toFront();

        cameraLocation.show();
    }

    /**
     * Displays a popup with instructions on how to interact with the simulation.
     *
     * @param primaryStage The owner stage for proper z-order.
     */
    public static void showHowTo(Stage primaryStage) {
        String howto = """
            📘 How to Interact with Free Space

            🔼 Elevate:       E
            🔽 Descend:       Q

            ⬆️ Move Forward:  W
            ⬇️ Move Backward: S
            ➡️ Move Right:    D
            ⬅️ Move Left:     A
            ⚡ Move Extremely fast: L-Shift

            🖱️ Zoom In/Out:  Use Mouse Scroll

            🖥️ Fullscreen: Press F11
            
            📍 Give Camera Coordinates: 3 Simultaneous Left-clicks
            """;

        Alert howTo = new Alert(Alert.AlertType.INFORMATION);
        howTo.setTitle("How to?");
        howTo.setHeaderText("Please follow the instructions below to properly interact with the free space!");
        howTo.setContentText(howto);

        Stage dialogStage = (Stage) howTo.getDialogPane().getScene().getWindow();
        IconSetter.setIcons(dialogStage, "/styles/howTo.png");

        // * This part ensures that the pop-up is pushed to the front!
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.toFront();

        howTo.show();
    }
}
