package executables;

import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;import javafx.scene.chart.LineChart;
import javafx.scene.layout.StackPane;
import executables.solvers.FirstDimension;
import executables.testing.plotter;
import java.util.function.BiFunction;



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
    private StackPane chartContainer;

    @FXML
    public void initialize() {

        BiFunction<Double, Double, Double> f = (x, y) -> x + y;


        double[][] solution = FirstDimension.euler1st(f, 0, 1, 0.1, 100);


        LineChart<Number, Number> chart = plotter.plotSolution(solution, "Euler", "x", "y(x)");


        chartContainer.getChildren().add(chart);

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
