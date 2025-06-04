package com.example.utilities.GD.Opitmizers;

import java.util.function.Function;
import java.util.List;

/**
 * Optimizer interface defines the common operations for optimization algorithms.
 * This follows the Interface Segregation Principle by providing a minimal interface
 * that clients can depend on.
 */
public interface Optimizer {
    
    /**
     * Performs a single optimization step.
     * 
     * @param objectiveFunction Function that evaluates the objective value for a given set of parameters
     * @param parameters Current parameter values
     * @param minimize True if the objective should be minimized, false if it should be maximized
     * @return Updated parameters after one step of optimization
     */
    double[] optimizeStep(Function<double[], Double> objectiveFunction, double[] parameters, boolean minimize);
    
    /**
     * Performs a complete optimization process.
     * 
     * @param objectiveFunction Function that evaluates the objective value for a given set of parameters
     * @param initialParameters Initial parameter values
     * @param minimize True if the objective should be minimized, false if it should be maximized
     * @return Result of the optimization process
     */
    OptimizationResult optimize(Function<double[], Double> objectiveFunction, double[] initialParameters, boolean minimize);
    
    /**
     * Interface for optimization result.
     */
    interface OptimizationResult {
        /**
         * Gets the optimized parameters.
         * 
         * @return The optimized parameters
         */
        double[] getParameters();
        
        /**
         * Gets the final objective value.
         * 
         * @return The final objective value
         */
        double getObjectiveValue();
        
        /**
         * Gets the number of iterations performed.
         * 
         * @return The number of iterations
         */
        int getIterations();
        
        /**
         * Checks if the optimization converged.
         * 
         * @return True if the optimization converged, false otherwise
         */
        boolean isConverged();
        
        /**
         * Gets the history of objective values during optimization.
         * 
         * @return The history of objective values
         */
        List<Double> getObjectiveHistory();
    }
}