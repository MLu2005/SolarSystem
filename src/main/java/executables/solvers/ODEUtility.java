package executables.solvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static executables.solvers.Constants.*;

// rewrote all methods to support nth Dimension ODE results
// All are highly inefficient and should not be finally submitted
public class ODEUtility {

    public static Double[] getValueAt(double[][] valuePairs, double val) {
        for (double[] e : valuePairs) {
            // Giving it some tolerance
            if (Math.abs(e[0] - val) < TOLERANCE) {
                Double[] result = new Double[e.length - 1];
                for (int i = 1; i < e.length; i++) {
                    result[i - 1] = e[i];
                }
                return result;
            }
        }
        return null;
    }

    public static Double[] getMax(double[][] valuePairs) {
        if (valuePairs.length == 0) return null;

        int n = valuePairs[0].length - 1;  // Number of y variables
        Double[] maxValues = new Double[n];

        Arrays.fill(maxValues, Double.NEGATIVE_INFINITY);

        for (double[] e : valuePairs) {
            for (int j = 0; j < n; j++) {
                if (e[j + 1] > maxValues[j]) {
                    maxValues[j] = e[j + 1];
                }
            }
        }
        return maxValues;
    }

    public static Double[] getMin(double[][] valuePairs) {
        if (valuePairs.length == 0) return null;

        int n = valuePairs[0].length - 1;  // Number of y variables
        Double[] minValues = new Double[n];

        Arrays.fill(minValues, Double.POSITIVE_INFINITY);

        for (double[] e : valuePairs) {
            for (int j = 0; j < n; j++) {
                if (e[j + 1] < minValues[j]) {
                    minValues[j] = e[j + 1];
                }
            }
        }
        return minValues;
    }
    // still a bit ugly have to maybe change through
    public static Double[][] xIntersectInStep(double[][] valuePairs) {
        if (valuePairs.length == 0) return null;

        int n = valuePairs[0].length - 1; // Number of y variables
        List<List<Double>> intersects = new ArrayList<>();

        for (int j = 0; j < n; j++) {
            intersects.add(new ArrayList<>()); // Create list for each dimension
        }

        for (double[] e : valuePairs) {
            for (int j = 0; j < n; j++) {
                if (e[j + 1] < TOLERANCE && e[j + 1] > -TOLERANCE) {
                    intersects.get(j).add(e[0]); // Store x value where yj crosses 0
                }
            }
        }

        Double[][] result = new Double[n][];
        for (int j = 0; j < n; j++) {
            result[j] = intersects.get(j).toArray(new Double[0]);
        }
        return result;
    }

    public static Double[][][] xIntersectInterval(double[][] valuePairs) {
        if (valuePairs.length == 0) return null;

        int n = valuePairs[0].length - 1; // Number of y variables
        List<List<Double[]>> intersects = new ArrayList<>();

        for (int j = 0; j < n; j++) {
            intersects.add(new ArrayList<>()); // Create list for each dimension
        }

        for (int i = 0; i < valuePairs.length - 1; i++) {
            for (int j = 0; j < n; j++) {
                double y1 = valuePairs[i][j + 1];
                double y2 = valuePairs[i + 1][j + 1];

                // Detect sign change (crossing x-axis)
                if ((y1 > 0 && y2 < 0) || (y1 < 0 && y2 > 0)) {
                    intersects.get(j).add(new Double[]{valuePairs[i][0], valuePairs[i + 1][0]});
                }
            }
        }
        Double[][][] result = new Double[n][][];
        for (int j = 0; j < n; j++) {
            result[j] = intersects.get(j).toArray(new Double[0][]);
        }
        return result;
    }

    // Adds two vectors element-wise
    public static double[] addVectors(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    // Scales a vector by a constant
    public static double[] scaleVector(double[] v, double scalar) {
        double[] result = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            result[i] = scalar * v[i];
        }
        return result;
    }

}
