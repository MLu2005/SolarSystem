package src;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;


public class Controller {

    @FXML private AnchorPane root;
    @FXML private AnchorPane sun;
    @FXML private AnchorPane venus;
    @FXML private AnchorPane mercury;
    @FXML private AnchorPane earth;
    @FXML private AnchorPane mars;
    @FXML private AnchorPane jupiter;
    @FXML private AnchorPane saturn;

    @FXML
    public void initialize() {

        bindPlanetPositions();
    }

    private void bindPlanetPositions() {

        // Positioning based on relative width and height of the root pane.
        DoubleBinding sunLayoutX = root.widthProperty().multiply(0.1);
        sun.layoutXProperty().bind(sunLayoutX);
        sun.layoutYProperty().bind(root.heightProperty().multiply(0.1));

        DoubleBinding venusLayoutX = root.widthProperty().multiply(0.2);
        venus.layoutXProperty().bind(venusLayoutX);
        venus.layoutYProperty().bind(root.heightProperty().multiply(0.2));

        DoubleBinding mercuryLayoutX = root.widthProperty().multiply(0.3);
        mercury.layoutXProperty().bind(mercuryLayoutX);
        mercury.layoutYProperty().bind(root.heightProperty().multiply(0.3));

        DoubleBinding earthLayoutX = root.widthProperty().multiply(0.4);
        earth.layoutXProperty().bind(earthLayoutX);
        earth.layoutYProperty().bind(root.heightProperty().multiply(0.4));

        DoubleBinding marsLayoutX = root.widthProperty().multiply(0.5);
        mars.layoutXProperty().bind(marsLayoutX);
        mars.layoutYProperty().bind(root.heightProperty().multiply(0.5));

        DoubleBinding jupiterLayoutX = root.widthProperty().multiply(0.6);
        jupiter.layoutXProperty().bind(jupiterLayoutX);
        jupiter.layoutYProperty().bind(root.heightProperty().multiply(0.6));

        DoubleBinding saturnLayoutX = root.widthProperty().multiply(0.7);
        saturn.layoutXProperty().bind(saturnLayoutX);
        saturn.layoutYProperty().bind(root.heightProperty().multiply(0.7));
    }
}
