package executables;
import executables.solvers.*;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.chart.LineChart;
import executables.testing.plotter;

import java.util.*;
import java.util.function.BiFunction;


import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;


import java.util.function.BiFunction;


public class Controller {

    @FXML
    private AnchorPane root;
    @FXML
    private AnchorPane sun;
    @FXML
    private AnchorPane venus;
    @FXML
    private AnchorPane mercury;
    @FXML
    private AnchorPane earth;
    @FXML
    private AnchorPane mars;
    @FXML
    private AnchorPane jupiter;
    @FXML
    private AnchorPane saturn;


    private void bindPlanetPositions() {


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

    @FXML
    private TextField stepSizeInput;

    @FXML
    private TextField stepsInput;

    @FXML
    private TextField initialConditionsInput;

    @FXML
    private TextArea equationInput;

    @FXML
    private TextField variablesInput;

    @FXML
    private ListView<String> resultListView;

    @FXML
    private Button ODEvisualize;

    @FXML
    private Pane chartContainer;

    @FXML
    private Button solveButton;

    @FXML
    private RadioButton rk4MethodRadio;

    @FXML
    private RadioButton eulerMethodRadio;

    private ToggleGroup toggleGroup;

    @FXML
    public void initialize() {
        toggleGroup = new ToggleGroup();
        eulerMethodRadio.setToggleGroup(toggleGroup);
        rk4MethodRadio.setToggleGroup(toggleGroup);

        // * Setting eulermethod class as default
        eulerMethodRadio.setSelected(true);

        eulerMethodRadio.setOnAction(event -> System.out.println("Euler method selected"));
        rk4MethodRadio.setOnAction(event -> System.out.println("Runge-Kutta 4 selected"));

        solveButton.setOnAction(e -> solveODE());
    }

    @FXML
    public void solveODE() {
        try {
            // * Var(x0) represents the independent variable it's set to zero by default but the user can explicitly put it as a different number.
            // * and i parsed them because they're initially strings in the GUI just to make my life easier.
            double x0 = Double.parseDouble(initialConditionsInput.getText().split(",")[0].trim());
            double stepSize = Double.parseDouble(stepSizeInput.getText().trim());
            int steps = Integer.parseInt(stepsInput.getText().trim());

            String[] equations = equationInput.getText().split("\n");
            String[] variables = variablesInput.getText().split(",");

            for (int i = 0; i < variables.length; i++) {
                variables[i] = variables[i].trim();
            }

            if (variables.length == 0 || variables[0].isEmpty()) {
                System.out.println("??, Forgot to input the variables.");
            }

            ODEUtility.setEquations(equations, variables); // ODEUTILITY CLASS
            BiFunction<Double, double[], double[]> odeFunction = ODEUtility.textToFunction(); // ODEUTILITY CLASS
            int stateSize = variables.length;
            double[] initialState = new double[stateSize];

            String[] initialValues = initialConditionsInput.getText().split(",");

            // *  checking if the initial conditions match the number of state variables (EXCLUDING X0)!
            if (initialValues.length != stateSize + 1) {
                System.out.println("initial conditions do not match the number of variables.");
            }

            for (int i = 1; i < initialValues.length; i++) {
                initialState[i - 1] = Double.parseDouble(initialValues[i].trim());
            }

            // * cool radiobuttons to choose from higher dimension and first dimension can be applied by the user.
            double[][] result;
            if (eulerMethodRadio.isSelected()) {
                System.out.println("You're using Euler Method");
                EulerSolver euler = new EulerSolver();
                result = euler.solve(odeFunction::apply, x0, initialState, stepSize, steps, null);
            } else {
                System.out.println("You're using Runge-Kutta 4 Method");
                RK4Solver rk4 = new RK4Solver();
                result = rk4.solve(odeFunction, x0, initialState, stepSize, steps, null);
            }

            // * the part below is responsible for displaying the results in the viewlist
            StringBuilder output = new StringBuilder("t"); // t is our initial.
            for (String var : variables) {
                output.append("\t").append(var);
            }
            output.append("\n");

            Platform.runLater(() -> {
                resultListView.getItems().clear();
                resultListView.getItems().add(output.toString());

                for (double[] row : result) {
                    StringBuilder rowText = new StringBuilder("t = " + row[0]);
                    for (int i = 1; i < row.length; i++) {
                        rowText.append(", ").append(variables[i - 1]).append(" = ").append(row[i]);
                    }
                    resultListView.getItems().add(rowText.toString());
                }
            });

        } catch (NumberFormatException ex) {
            fightError("???Your input is invalid, make sure you enter valid numbers.???");
        } catch (IllegalArgumentException ex) {
            fightError(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            fightError("Unknown error?" + ex.getMessage());
        }
    }

    private void fightError(String message) {
        System.err.println(message);
        Platform.runLater(() -> {
            resultListView.getItems().clear();
            resultListView.getItems().add(message);
        });
    }

    @FXML
    public void visualizeODE() {
        try {
            // * reusing what i did in ODESolver method again
            double x0 = Double.parseDouble(initialConditionsInput.getText().split(",")[0].trim());
            double stepSize = Double.parseDouble(stepSizeInput.getText().trim());
            int steps = Integer.parseInt(stepsInput.getText().trim());

            String[] equations = equationInput.getText().split("\n");
            String[] variables = variablesInput.getText().split(",");

            for (int i = 0; i < variables.length; i++) {
                variables[i] = variables[i].trim();
            }

            ODEUtility.setEquations(equations, variables);
            BiFunction<Double, double[], double[]> odeFunction = ODEUtility.textToFunction();

            int stateSize = variables.length;
            double[] initialState = new double[stateSize];

            String[] initialValues = initialConditionsInput.getText().split(",");

            // * error fighter exception
            if (initialValues.length != stateSize + 1) {
                fightError("???initial conditions do not match the number of variables.???");
                return;
            }

            for (int i = 1; i < initialValues.length; i++) {
                initialState[i - 1] = Double.parseDouble(initialValues[i].trim());
            }

            RK4Solver rk4 = new RK4Solver();
            double[][] solution = rk4.solve(odeFunction, x0, initialState, stepSize, steps, null);

            LineChart<Number, Number> chart = plotter.plotSolution(solution, "Euler Method", "t", "y(t)");

            // * make a new popup window (Stage) to display the chart
            Stage chartStage = new Stage();
            chartStage.setTitle("ODE Visualization");


            Scene chartScene = new Scene(new Group(chart), 600, 400);
            chartStage.setScene(chartScene);

            chartStage.show();

        } catch (NumberFormatException ex) {
            fightError("??invalid input, Make sure to enter valid numbers.??");
        } catch (Exception ex) {
            fightError("UKNOWN ERROR?!: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
    * Compares the accuracy of our solvers negative numbers mean 10^(x) so -1 is 10^(-1) and so on, may still have to fix this!
     *
     */
    public void compareSolverAccuracy() {
        try {
            double[] stepSizes = {1, 0.4, 0.2, 0.1, 0.05, 0.025, 0.0125, 0.00625};
            double x0   = 0.0;
            double tEnd = 1.0;
            double[] y0 = {1.0};

            BiFunction<Double, double[], double[]> ode = (t, y) -> new double[]{-y[0]};
            double exactAtEnd = Math.exp(-tEnd);

            ODESolver[] solvers   = { new EulerSolver(), new RK4Solver(), new RKF45Solver() };
            String[]solverId = { "Euler","RK4","RKF45"}; // RKF45 should give a slope of approx 0, as its adaptive method

            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("log10(step size h)");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("log10(error)");
            LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle("Solver Accuracy (log–log)");
            chart.setCreateSymbols(true);

            for (int s = 0; s < solvers.length; s++) {

                double[][] data = new double[stepSizes.length][2];

                for (int i = 0; i < stepSizes.length; i++) {
                    double h    = stepSizes[i];
                    int steps   = (int) ((tEnd - x0) / h);

                    double[][] result = solvers[s].solve(
                            ode,
                            x0,
                            Arrays.copyOf(y0, y0.length),
                            h,
                            steps,
                            null
                    );
                    double err = Math.abs(result[result.length - 1][1] - exactAtEnd);

                    data[i][0] = h;
                    data[i][1] = err;
                }

                LineChart<Number, Number> tmp = plotter.plotLogLog(
                        data,
                        solverId[s],
                        "step size h",
                        "error"
                );

                chart.getData().addAll(tmp.getData());
                XYChart.Series<Number, Number> series =
                        chart.getData().get(chart.getData().size() - 1);

                double m = slope(data);  // calculate slop to verfiy correctness order should correlate with slope
                series.setName(solverId[s] + "  (slope ≈ " + String.format("%.2f", m) + ")");
            }


            Stage stage = new Stage();
            stage.setTitle("Solver Error Comparison");
            stage.setScene(new Scene(new Group(chart), 800, 600));
            stage.show();

        } catch (Exception e) {
            fightError("Error during log-log comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static double slope(double[][] d) {

        if (d == null || d.length < 2) return Double.NaN;

        int n = d.length;

        double h1 = d[n - 2][0];
        double e1 = d[n - 2][1];
        double h2 = d[n - 1][0];
        double e2 = d[n - 1][1];

        return (Math.log10(e2) - Math.log10(e1)) /
                (Math.log10(h2) - Math.log10(h1));
    }

    @FXML
    private Button compareButton;

}

