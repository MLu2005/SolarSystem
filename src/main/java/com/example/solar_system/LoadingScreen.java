package com.example.solar_system;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

/**
 * Shows a loading screen (video)
 * The splash screen plays a video and then closes automatically.
 */
public class LoadingScreen {

    private final Stage stage;

    /**
     * Creates a LoadingScreen attached to the given stage.
     *
     * @param stage the stage to show the loading screen on
     */
    public LoadingScreen(Stage stage) {
        this.stage = stage;
    }

    /**
     * Shows the splash screen video. When the video finishes, closes the splash screen
     * and runs the provided onFinish callback.
     *
     * The stage is shown only after the video media is ready to avoid blank screen flicker.
     *
     * @param onFinish the callback to run after the splash screen closes; can be null
     */
    public void showSplash(Runnable onFinish) {
        String videoPath = getClass().getResource("/videos/loadingScreen.mp4").toExternalForm();
        Media media = new Media(videoPath);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        StackPane root = new StackPane(mediaView);
        Scene scene = new Scene(root, 850, 480);

        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.setResizable(false);
        stage.setTitle(" ⌛ Loading...");

        IconSetter.setIcons(stage, "/styles/loadingIcon.png");

        mediaPlayer.setOnReady(() -> {
            // Show the stage only when media is ready
            Platform.runLater(() -> {
                stage.show();
                mediaPlayer.play();
            });
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.dispose();
            Platform.runLater(() -> {
                stage.close();
                if (onFinish != null) {
                    onFinish.run();
                }
            });
        });
    }

}
