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
        IconSetter.setIcons(dialogStage, "/styles/solarSystemStyling/locationIcon.png");

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
                📘 How to Interact with the Free Space
                
                                                       🔼 Elevate:             E
                                                       🔽 Descend:             Q
                
                                                       ⬆️ Move Forward:        W
                                                       ⬇️ Move Backward:       S
                                                       ➡️ Move Right:          D
                                                       ⬅️ Move Left:           A
                                                       ⚡ Move Extremely Fast:  Left Shift (L-Shift)
                
                                                       🖱️ Zoom In/Out:         Mouse Scroll
                
                                                       🖥️ Fullscreen:          F11
                
                                                       📍 Get Camera Coordinates:  3 Simultaneous Left-clicks
                
                                                       --------------------------------------------------------
                
                                                       To use Spectator Mode:
                                                       - Choose the object you want to follow.
                                                       - You must manually look around to explore the surroundings.
                                                       - The camera will automatically follow the selected object.
                
                                                       To exit Spectator Mode:
                                                       - Press H
            """;

        Alert howTo = new Alert(Alert.AlertType.INFORMATION);
        howTo.setTitle("How to?");
        howTo.setHeaderText("Please follow the instructions below to properly interact with the free space!");
        howTo.setContentText(howto);

        Stage dialogStage = (Stage) howTo.getDialogPane().getScene().getWindow();
        IconSetter.setIcons(dialogStage, "/styles/solarSystemStyling/howTo.png");

        // * This part ensures that the pop-up is pushed to the front!
        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.toFront();

        howTo.show();
    }

    // * Pop-up indicating which object are you going to spectate.
    public static void showSpectatorPopup(String targetName, Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Spectator Mode Activated");
        alert.setHeaderText("Camera now following:");
        alert.setContentText(targetName);

        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        IconSetter.setIcons(dialogStage, "/styles/solarSystemStyling/spectateIcon.png");

        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.toFront();

        alert.show();
    }

    // * Tells the user that he got out of spectator mode.
    public static void showSpectatorExitPopup(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Spectator Mode Disabled");
        alert.setHeaderText(null);
        alert.setContentText("👁️❌ You have exited Spectator Mode.\nThe camera has been reset.");

        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        IconSetter.setIcons(dialogStage, "/styles/solarSystemStyling/exitIcon.png");

        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.toFront();

        alert.show();
    }

    // * Warns the user about not being in spectatorMode to exit in the first place.
    public static void showNotInSpectatorModePopup(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Not in Spectator Mode");
        alert.setHeaderText(null);
        alert.setContentText("‼!! You are currently NOT in Spectator Mode.");

        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        IconSetter.setIcons(dialogStage, "/styles/solarSystemStyling/warningIcon.png");

        dialogStage.initOwner(primaryStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.toFront();

        alert.show();
    }


}
