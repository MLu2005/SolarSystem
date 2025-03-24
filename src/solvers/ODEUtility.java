package src.solvers;

import static src.solvers.Constants.*;

public class ODEUtility {


    // method to fetch a value for a specific x in a list, if it doesnt exist return NaN
    // O(n)
    public static double getValueAt(double[][] valuePairs, double val) {
        for (double[] e : valuePairs) {
            // Giving it some tolerance
            if (Math.abs(e[0] - val) < TOLERANCE) return e[1];
        }
        // Return NaN if the value is not found
        return Double.NaN;
    }
}
