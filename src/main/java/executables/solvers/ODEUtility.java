package executables.solvers;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.function.BiFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



import static executables.solvers.Constants.*;

// rewrote all methods to support nth Dimension ODE results
// All are highly inefficient and should not be finally submitted
public class ODEUtility {

    private static String[] equations;
    private static String[] variables;

    public static void setEquations(String[] eqs, String[] vars) {
        equations = eqs;
        variables = vars;
    }

    // reference: https://www.baeldung.com/java-evaluate-math-expression-string
    public static BiFunction<Double, double[], double[]> textToFunction() {
        return (t, state) -> {
            if (equations == null || variables == null) {
                System.out.println("the equations or the variables have not been initialized.");
            }

            if (state.length != variables.length) {
                System.out.println( "the state length (" + state.length + ") does not match Variables length (" + variables.length + ")");
            }

            double[] results = new double[equations.length];

            for (int i = 0; i < equations.length; i++) {
                try {

                    Expression expr = new ExpressionBuilder(equations[i])
                            .variables(variables)
                            .variable("t")
                            .build();

                    for (int j = 0; j < variables.length; j++) {
                        expr.setVariable(variables[j], state[j]);
                    }
                    expr.setVariable("t", t);

                    // Evaluate expression
                    results[i] = expr.evaluate();
                } catch (Exception e) {
                    throw new RuntimeException("Error evaluating equation '" + equations[i] + "': " + e.getMessage(), e);
                }
            }
            return results;
        };
    }

    public static double[] getValueAt(double[][] valuePairs, double val) {
        if (valuePairs == null) return null;

        for (double[] e : valuePairs) {
            if (Math.abs(e[0] - val) < TOLERANCE) {
                return Arrays.copyOfRange(e, 1, e.length);
            }
        }
        return null;
    }

    public static Double[] getMax(double[][] valuePairs) {
        if (valuePairs == null || valuePairs.length == 0) return null;

        int n = valuePairs[0].length - 1;
        Double[] maxValues = new Double[n];
        Arrays.fill(maxValues, Double.NEGATIVE_INFINITY);

        for (double[] e : valuePairs) {
            for (int j = 0; j < n; j++) {
                maxValues[j] = Math.max(maxValues[j], e[j + 1]);
            }
        }
        return maxValues;
    }

    public static Double[] getMin(double[][] valuePairs) {
        if (valuePairs == null || valuePairs.length == 0) return null;

        int n = valuePairs[0].length - 1;
        Double[] minValues = new Double[n];
        Arrays.fill(minValues, Double.POSITIVE_INFINITY);

        for (double[] e : valuePairs) {
            for (int j = 0; j < n; j++) {
                minValues[j] = Math.min(minValues[j], e[j + 1]);
            }
        }
        return minValues;
    }

    public static Double[][] xIntersectInStep(double[][] valuePairs) {
        if (valuePairs == null || valuePairs.length == 0) return null;

        int n = valuePairs[0].length - 1;
        List<List<Double>> intersects = new ArrayList<>(n);

        for (int j = 0; j < n; j++) {
            intersects.add(new ArrayList<>());
        }

        for (double[] e : valuePairs) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(e[j + 1]) < TOLERANCE) {
                    intersects.get(j).add(e[0]);
                }
            }
        }

        Double[][] result = new Double[n][];
        for (int j = 0; j < n; j++) {
            result[j] = intersects.get(j).toArray(new Double[0]);
        }
        return result;
    }

    public static double[] scaleVector(double[] v, double scalar) {
        return Arrays.stream(v)
                .map(val -> val * scalar)
                .toArray();
    }

    public static double[] addVectors(double[] a, double[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Vectors must be of the same length");
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }
    protected double roundTime(double t) {
        return Math.round(t * 100.0) / 100.0;
    }

    public static double[][] initStorage(int maxSteps, double t0, double[] y0) {
        int dim = y0.length;
        double[][] result = new double[maxSteps + 1][dim + 1];
        result[0][0] = t0;
        for (int i = 0; i < dim; i++) {
            result[0][i + 1] = y0[i];
        }
        return result;
    }
}

