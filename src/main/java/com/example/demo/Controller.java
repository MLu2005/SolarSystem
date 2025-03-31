package com.example.demo;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;

import java.util.Arrays;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;


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
    private TextField  stepSizeInput;

    @FXML
    private TextField  stepsInput;

    @FXML
    private TextField initialConditionsInput;
    @FXML
    private TextArea equationInput;

    @FXML
    private TextField variablesInput;

    @FXML
    private ListView<String> resultListView;

    @FXML
    private Button vODE;

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

    private void solveODE() {
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

            /* The part below is mainly focused on how the GUI is able to recieve multiple equations at the same time.
            Basically, this function --> ODEUtility.setEquations(equations, variables)
            And this function --> BiFunction<Double, double[], double[]> odeFunction = ODEUtility.textToFunction()

            they simultaneously convert the user input from the equationInput[textArea] into a function that can work on multiple ODES by storing
            them in the string array of equations variables which then splits them by lines and extracts the variables
            from (variablesInput)[textField] by splitting the commas then it gets stored in equations[].

             */

            ODEUtility.setEquations(equations, variables); //ODEUTILITY CLASS
            BiFunction<Double, double[], double[]> odeFunction = ODEUtility.textToFunction(); //ODEUTILITY CLASS
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
                result = NthDimension.eulerNth(odeFunction::apply, x0, initialState, stepSize, steps);
            } else {
                System.out.println("You're using Runge-Kutta 4 Method");
                result = NthDimension.rungeKutta4(odeFunction, x0, initialState, stepSize, steps);
            }



            // * the part below is responsible for displaying the results in the viewlist
            // * making the headrows for the variables, representing the independent variable, and loops through the dependent variable adding it to header :D
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

    plotter plot = new plotter();

    @FXML
    public void visualizeODE() {
        try {

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
            if (initialValues.length != stateSize + 1) {
                fightError("Initial conditions do not match the number of variables.");
                return;
            }

            for (int i = 1; i < initialValues.length; i++) {
                initialState[i - 1] = Double.parseDouble(initialValues[i].trim());
            }


            double[][] solution = FirstDimension.euler1st(odeFunction, x0, initialState, stepSize, steps);


            LineChart<Number, Number> chart = plot.plotSolution(solution, "Euler Method", "x", "y(x)");


            Platform.runLater(() -> {
                chartContainer.getChildren().clear();
                chartContainer.getChildren().add(chart);
            });

        } catch (NumberFormatException ex) {
            fightError("Invalid input. Make sure to enter valid numbers.");
        } catch (Exception ex) {
            fightError("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}

