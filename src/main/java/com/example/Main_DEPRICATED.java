package com.example;

import java.time.LocalDateTime;
import java.util.List;

import com.example.solar_system.CelestialBody;
import com.example.utilities.DataLoader;
import com.example.utilities.PlanetPositionCalculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main_DEPRICATED extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(Main_DEPRICATED.class.getResource("/executables/odeUse.fxml"));

        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Solar System");
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        LocalDateTime j2000 = LocalDateTime.of(2000, 1, 1, 12, 0);
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV("src/main/java/com/example/utilities/IC.csv");
        PlanetPositionCalculator calculator = new PlanetPositionCalculator(bodies);
        calculator.propagateTo(j2000);

        System.out.println("Results:");
        for (CelestialBody cb : calculator.getBodies()) {
            System.out.println(cb);
        }
    }

}

