package solvers;

import java.util.ArrayList;

import static solvers.Constants.*;

public class ODEUtility {

    // method to fetch a value for a specific x in a list, if it doesn't exist return NaN
    // O(n)
    public static double getValueAt(double[][] valuePairs, double val) {
        for (double[] e : valuePairs) {
            // Giving it some tolerance
            if (Math.abs(e[0] - val) < TOLERANCE) return e[1];
        }
        // Return NaN if the value is not found
        return Double.NaN;
    }
    public static double getMax(double[][] valuePairs) {
        if (valuePairs.length == 0) return Double.NaN;
        double max = Double.NEGATIVE_INFINITY;
        for (double[] e : valuePairs) {
            if (e[1] > max) max = e[1];
        }
        return max;
    }

    public static double getMin(double[][] valuePairs) {
        if (valuePairs.length == 0) return Double.NaN;
        double min = Double.POSITIVE_INFINITY;
        for (double[] e : valuePairs) {
            if (e[1] < min) min = e[1];
        }
        return min;
    }

    public static double[] getXIntersectInStep(double[][] valuePairs) {
        if (valuePairs.length == 0) return null;
        if (getMin(valuePairs) > TOLERANCE) return null;

        ArrayList<Double> intersects = new ArrayList<>();
        for (double[] e : valuePairs) {
            if (e[1] < TOLERANCE && e[1] > -TOLERANCE) intersects.add(e[0]);
        }
        double[] intersectArray = new double[intersects.size()];
        for (int i = 0; i < intersectArray.length; i++) {
            intersectArray[i] = intersects.get(i);
        }
        return intersectArray;
    }
    public static double[][] getXIntersectInterval(double[][] valuePairs) {
        if (valuePairs.length == 0) return null;
        if (getMin(valuePairs) > 0) return null;
        if (getMax(valuePairs) < 0) return null;

        ArrayList<double[]> intersects = new ArrayList<>();

        for (int i = 0; i < valuePairs.length - 1; i++) {
            double y1 = valuePairs[i][1];
            double y2 = valuePairs[i+1][1];

            // Detect sign change (crossing the x-axis)
            if ((y1 > 0 && y2 < 0) || (y1 < 0 && y2 > 0)) {
                intersects.add(new double[]{valuePairs[i][0], valuePairs[i+1][0]});
            }
        }
        return intersects.toArray(new double[0][]);
    }

    public static double getSlopeAt(double[][] valuePairs, double x) {
        for (int i = 0; i < valuePairs.length - 1; i++) {
            double x1 = valuePairs[i][0], y1 = valuePairs[i][1];
            double x2 = valuePairs[i + 1][0], y2 = valuePairs[i + 1][1];

            if (x1 <= x && x <= x2) {
                return (y2 - y1) / (x2 - x1);
            }
        }
        return Double.NaN;
    }


}
