package executables;

import static executables.testing.EulerTest_analytical.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
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

    public static void main(String[] args) {
        launch(args);
        // close the application to see
        eulerErrorDefault();

    }
}

