package executables.testing;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;


import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;


public class plotter {

    public static LineChart<Number, Number> plotSolution(
            double[][] solution,
            String solverName,
            String xAxisLabel,
            String yAxisLabel
    ) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisLabel);

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("ODE Solution - " + solverName);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(solverName);

        // Assuming the first dimension is time (t), and we want to plot the first dependent variable (y1)
        for (int i = 0; i < solution.length; i++) {
            // solution[i][0] is the time value, solution[i][1] is the first dependent variable
            series.getData().add(new XYChart.Data<>(solution[i][0], solution[i][1]));
        }

        lineChart.getData().add(series);
        return lineChart;
    }

}


