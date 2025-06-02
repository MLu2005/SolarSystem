package com.example.main_gui;

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

        for (double[] point : solution) {
            series.getData().add(new XYChart.Data<>(point[0], point[1]));
        }

        lineChart.getData().add(series);
        return lineChart;
    }
}
