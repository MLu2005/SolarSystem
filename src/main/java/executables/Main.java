package executables;

import java.time.LocalDateTime;
import java.util.List;

import com.example.solarSystem.CelestialBody;
import com.example.solarSystem.DataLoader;
import com.example.solarSystem.PlanetPositionCalculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/executables/HomeView.fxml"));

        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Solar System");
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        // Use J2000 epoch (January 1, 2000 at 12:00 UTC) as the test date
        LocalDateTime j2000 = LocalDateTime.of(2000, 1, 1, 12, 0);
        System.out.println("Testing propagation to J2000 epoch: " + j2000 + " UTC");

        // Load bodies and run the calculator
        List<CelestialBody> bodies = DataLoader.loadBodiesFromCSV("src/main/java/com/example/solarSystem/IC.csv");

        PlanetPositionCalculator calculator = new PlanetPositionCalculator(bodies);
        calculator.propagateTo(j2000);

        // Print out the resulting positions and velocities
        System.out.println("Results at J2000 (should match initial conditions):");
        for (CelestialBody cb : calculator.getBodies()) {
            System.out.println(cb);
        }
    }

}

