package com.example.coreGui;

import com.example.solar_system.IconSetter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SolvonautGUI extends Application {
    private double x = 0, y = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/everythingUse.fxml"));
        primaryStage.initStyle(StageStyle.UNDECORATED);

        IconSetter.setIcons(primaryStage, "/guiStyling/controlCenter.png");

        primaryStage.setTitle("Solvonaut \uD83E\uDD16");

        root.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - x);
            primaryStage.setY(event.getScreenY() - y);
        });

        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
