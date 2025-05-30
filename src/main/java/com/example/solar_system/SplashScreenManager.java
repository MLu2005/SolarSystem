package com.example.solar_system;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Manages the splash screen display.
 */
public class SplashScreenManager {

    private final Stage splashStage;

    public SplashScreenManager(Stage splashStage) {
        this.splashStage = splashStage;
    }

    /**
     * Show splash screen, then run the provided callback after splash finishes.
     *
     * @param onSplashFinished Runnable to run after splash closes
     */
    public void showSplashScreen(Runnable onSplashFinished) {
        LoadingScreen splash = new LoadingScreen(splashStage);

        splash.showSplash(() -> {
            Platform.runLater(() -> {
                splashStage.close();
                onSplashFinished.run();
            });
        });
    }
}
