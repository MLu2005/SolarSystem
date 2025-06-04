package com.example.main_gui;

import executables.solvers.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.Group;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.function.BiFunction;

public class ControllerBeta {

    @FXML private TextField stepSizeInput;
    @FXML private TextField stepsInput;
    @FXML private TextField initialConditionsInput;
    @FXML private TextArea equationInput;
    @FXML private TextField variablesInput;
    @FXML private ListView<String> resultListView;
    @FXML private Button ODEvisualize;
    @FXML private Pane chartContainer;
    @FXML private Button solveButton;
    @FXML private RadioButton rk4MethodRadio;
    @FXML private RadioButton eulerMethodRadio;
    @FXML private RadioButton rkf45MethodRadio;

    private ToggleGroup toggleGroup;

    @FXML
    public void initialize() {
        toggleGroup = new ToggleGroup();
        eulerMethodRadio.setToggleGroup(toggleGroup);
        rk4MethodRadio.setToggleGroup(toggleGroup);
        rkf45MethodRadio.setToggleGroup(toggleGroup);

        eulerMethodRadio.setSelected(true);

        eulerMethodRadio.setOnAction(event -> System.out.println("Euler method selected"));
        rk4MethodRadio.setOnAction(event -> System.out.println("Runge-Kutta 4 selected"));
        rkf45MethodRadio.setOnAction(event -> System.out.println("RKF45 method selected"));

        solveButton.setOnAction(e -> solveODE());
        ODEvisualize.setOnAction(e -> visualizeODE());
    }


    private static class ODEInput {
        double x0;
        double stepSize;
        int steps;
        double[] initialState;
        String[] variables;
        BiFunction<Double, double[], double[]> odeFunction;
    }

    private ODEInput parseInput() {
        String[] variables = variablesInput.getText().split(",");
        for (int i = 0; i < variables.length; i++) {
            variables[i] = variables[i].trim();
        }
        if (variables.length == 0 || variables[0].isEmpty()) {
            throw new IllegalArgumentException("Variables input is missing.");
        }

        String[] equations = equationInput.getText().split("\n");
        ODEUtility.setEquations(equations, variables);
        BiFunction<Double, double[], double[]> odeFunction = ODEUtility.textToFunction();

        String[] initialValues = initialConditionsInput.getText().split(",");
        if (initialValues.length != variables.length + 1) {
            throw new IllegalArgumentException("Initial conditions do not match the number of variables.");
        }

        double x0 = Double.parseDouble(initialValues[0].trim());
        double[] initialState = new double[variables.length];
        for (int i = 1; i < initialValues.length; i++) {
            initialState[i - 1] = Double.parseDouble(initialValues[i].trim());
        }

        double stepSize = Double.parseDouble(stepSizeInput.getText().trim());
        int steps = Integer.parseInt(stepsInput.getText().trim());

        ODEInput input = new ODEInput();
        input.x0 = x0;
        input.stepSize = stepSize;
        input.steps = steps;
        input.initialState = initialState;
        input.variables = variables;
        input.odeFunction = odeFunction;
        return input;
    }

    @FXML
    public void solveODE() {
        try {
            ODEInput input = parseInput();
            double[][] result;

            if (eulerMethodRadio.isSelected()) {
                EulerSolver euler = new EulerSolver();
                result = euler.solve(input.odeFunction::apply, input.x0, input.initialState, input.stepSize, input.steps, null);
            } else if (rk4MethodRadio.isSelected()) {
                RK4Solver rk4 = new RK4Solver();
                result = rk4.solve(input.odeFunction, input.x0, input.initialState, input.stepSize, input.steps, null);
            } else if (rkf45MethodRadio.isSelected()) {
                RKF45Solver rkf45 = new RKF45Solver();
                result = rkf45.solve(input.odeFunction, input.x0, input.initialState, input.stepSize, input.steps, null);
            } else {
                throw new IllegalStateException("No solver method selected.");
            }

            displayResults(result, input.variables);
        } catch (NumberFormatException ex) {
            fightError("Invalid input. Please enter valid numbers.");
        } catch (IllegalArgumentException ex) {
            fightError(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            fightError("Unknown error: " + ex.getMessage());
        }
    }

    private void displayResults(double[][] result, String[] variables) {
        StringBuilder output = new StringBuilder("t");
        for (String var : variables) {
            output.append("\t").append(var);
        }

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
    }

    @FXML
    public void visualizeODE() {
        try {
            ODEInput input = parseInput();
            double[][] solution;

            if (eulerMethodRadio.isSelected()) {
                EulerSolver euler = new EulerSolver();
                solution = euler.solve(input.odeFunction, input.x0, input.initialState, input.stepSize, input.steps, null);
            } else if (rk4MethodRadio.isSelected()) {
                RK4Solver rk4 = new RK4Solver();
                solution = rk4.solve(input.odeFunction, input.x0, input.initialState, input.stepSize, input.steps, null);
            } else if (rkf45MethodRadio.isSelected()) {
                RKF45Solver rkf45 = new RKF45Solver();
                solution = rkf45.solve(input.odeFunction, input.x0, input.initialState, input.stepSize, input.steps, null);
            } else {
                throw new IllegalStateException("No solver method selected.");
            }

            // Assuming plotter is your utility class instance, you might need to instantiate or inject it
            LineChart<Number, Number> chart = plotter.plotSolution(solution, "ODE Visualization", "t", "y(t)");

            Stage chartStage = new Stage();
            chartStage.setTitle("ODE Visualization");
            Scene chartScene = new Scene(new Group(chart), 600, 400);
            chartStage.setScene(chartScene);
            chartStage.show();

        } catch (NumberFormatException ex) {
            fightError("Invalid input. Please enter valid numbers.");
        } catch (IllegalArgumentException ex) {
            fightError(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            fightError("Unknown error: " + ex.getMessage());
        }
    }

    private void fightError(String message) {
        System.err.println(message);
        Platform.runLater(() -> {
            resultListView.getItems().clear();
            resultListView.getItems().add(message);
        });
    }

    public void compareSolverAccuracy() {
        try {
            double[] stepSizes = { 0.2, 0.1, 0.05, 0.025, 0.0125 };
            double x0   = 0.0;
            double tEnd = 1.0;
            double[] y0 = { 1.0 };
            BiFunction<Double, double[], double[]> ode = (t, y) -> new double[]{ -y[0] };
            double exactAtEnd = Math.exp(-tEnd);

            ODESolver[] solvers = {
                    new EulerSolver(),
                    new RK4Solver(),
                    new RKF45Solver()
            };
            String[] solverId = { "Euler", "RK4", "RKF45" };


            NumberAxis xAxis = new NumberAxis(-2.0, -0.6, 0.4);
            xAxis.setLabel("Step size h");

            xAxis.setMinorTickVisible(true);
            xAxis.setMinorTickCount(15);

            xAxis.setTickLabelFormatter(new StringConverter<Number>() {
                @Override public String toString(Number object) {
                    double h = Math.pow(10, object.doubleValue());
                    return String.format("%.1e", h);
                }
                @Override public Number fromString(String string) { return null; }
            });

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Error");
            yAxis.setAutoRanging(true);
            yAxis.setTickLabelFormatter(new StringConverter<Number>() {
                @Override
                public String toString(Number object) {
                    double errValue = Math.pow(10, object.doubleValue());
                    return String.format("%.1e", errValue);
                }
                @Override
                public Number fromString(String string) {
                    return null;
                }
            });

            LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle("Solver Accuracy (log–log)");
            chart.setCreateSymbols(true);

            for (int s = 0; s < solvers.length; s++) {
                double[] logHs = new double[stepSizes.length];
                double[] logEs = new double[stepSizes.length];

                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(solverId[s]);

                for (int i = 0; i < stepSizes.length; i++) {
                    double h = stepSizes[i];
                    int steps = (int) ((tEnd - x0) / h);

                    double[][] result = solvers[s].solve(
                            ode,
                            x0,
                            Arrays.copyOf(y0, y0.length),
                            h,
                            steps,
                            null
                    );
                    double err = Math.abs(result[result.length - 1][1] - exactAtEnd);

                    double lx = Math.log10(h);
                    double ly = Math.log10(err);
                    logHs[i] = lx;
                    logEs[i] = ly;

                    series.getData().add(new XYChart.Data<>(lx, ly));
                }

                double slope = fitSlope(logHs, logEs);
                series.setName(
                        solverId[s] + String.format(" (slope ≈ %.2f)", slope)
                );

                chart.getData().add(series);
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

    private static double fitSlope(double[] xs, double[] ys) {
        int n = xs.length;
        if (n < 2) return Double.NaN;

        double meanX = 0, meanY = 0;
        for (int i = 0; i < n; i++) {
            meanX += xs[i];
            meanY += ys[i];
        }
        meanX /= n;
        meanY /= n;

        double num = 0, den = 0;
        for (int i = 0; i < n; i++) {
            double dx = xs[i] - meanX;
            double dy = ys[i] - meanY;
            num += dx * dy;
            den += dx * dx;
        }
        return num / den;
    }
}
