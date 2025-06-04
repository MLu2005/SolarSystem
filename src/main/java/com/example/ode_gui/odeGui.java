package com.example.ode_gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class odeGui extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        startNewWindow();
    }

    public static void startNewWindow() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(odeGui.class.getResource("/fxmls/odeUse.fxml"));
                AnchorPane root = loader.load();
                Scene scene = new Scene(root);

                Stage stage = new Stage();
                stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}