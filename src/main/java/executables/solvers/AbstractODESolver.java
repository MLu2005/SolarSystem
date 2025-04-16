package executables.solvers;

public abstract class AbstractODESolver implements ODESolver {

    protected double roundTime(double t) {
        return Math.round(t * 100.0) / 100.0;
    }

    protected double[][] initStorage(int maxSteps, double t0, double[] y0) {
        int dim = y0.length;
        double[][] result = new double[maxSteps + 1][dim + 1];
        result[0][0] = t0;
        for (int i = 0; i < dim; i++) {
            result[0][i + 1] = y0[i];
        }
        return result;
    }
}
