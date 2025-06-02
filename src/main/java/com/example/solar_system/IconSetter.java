package com.example.solar_system;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Utility for setting one or multiple icons on JavaFX windows.
 * To use call -> IconSetter.setIcons(stage, "path to the icon");
 */
public class IconSetter {

    /**
     * Sets one or more icons on the given stage.
     * Clears existing icons before adding new ones.
     *
     * @param stage       The target stage (window).
     * @param iconPaths   One or more resource paths to icon guiStyling, e.g. "/styles/programIcon.png"
     */
    public static void setIcons(Stage stage, String... iconPaths) {
        Objects.requireNonNull(stage, "Stage must not be null");
        stage.getIcons().clear();
        for (String path : iconPaths) {
            Image icon = new Image(Objects.requireNonNull(IconSetter.class.getResourceAsStream(path)));
            stage.getIcons().add(icon);
        }
    }

    /*
     * The main solarSystemApp icon.
     * @param stage The target stage.
     */
    public static void setAppIcon(Stage stage) {
        setIcons(stage, "/styles/solarSystemStyling/programIcon.png");
    }

}
