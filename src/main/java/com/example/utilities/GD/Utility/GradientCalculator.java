package com.example.utilities.GD.Utility;

import java.util.function.Function;

/**
 * GradientCalculator is responsible for computing numerical gradients of objective functions.
 * This class follows the Single Responsibility Principle by focusing solely on gradient calculation.
 */
public class GradientCalculator {
    
    private double stepSize;
    
    /**
     * Constructs a GradientCalculator with the default step size.
     */
    public GradientCalculator() {
        this.stepSize = 0.01; // Default step size
    }
    
    /**
     * Constructs a GradientCalculator with a custom step size.
     * 
     * @param stepSize Step size for numerical gradient estimation
     */
    public GradientCalculator(double stepSize) {
        this.stepSize = stepSize;
    }
    
    /**
     * Computes the numerical gradient of the objective function with respect to parameters
     * using the central difference method, which is more accurate than forward difference.
     * 
     * This implementation ensures that perturbed values stay within valid constraints (e.g., [0,1] for thruster settings).
     * 
     * @param objectiveFunction Function that evaluates the objective value for a given set of parameters
     * @param parameters Current parameter values
     * @return Gradient vector (partial derivatives of objective function with respect to each parameter)
     */
    public double[] estimateGradient(Function<double[], Double> objectiveFunction, double[] parameters) {
        int dimensions = parameters.length;
        double[] gradient = new double[dimensions];
        double baseObjective = objectiveFunction.apply(parameters);
        
        // Compute partial derivatives using central differences when possible,
        // or forward/backward differences when near boundaries
        for (int i = 0; i < dimensions; i++) {
            double forwardStep = Math.min(stepSize, 1.0 - parameters[i]);
            double backwardStep = Math.min(stepSize, parameters[i]);
            
            if (forwardStep > 0 && backwardStep > 0) {
                // Can use central difference
                double[] forwardParams = parameters.clone();
                forwardParams[i] += forwardStep;
                double forwardObjective = objectiveFunction.apply(forwardParams);
                
                double[] backwardParams = parameters.clone();
                backwardParams[i] -= backwardStep;
                double backwardObjective = objectiveFunction.apply(backwardParams);
                
                // Adjusted central difference formula with potentially different step sizes
                gradient[i] = (forwardObjective - backwardObjective) / (forwardStep + backwardStep);
            } else if (forwardStep > 0) {
                // Can only use forward difference
                double[] forwardParams = parameters.clone();
                forwardParams[i] += forwardStep;
                double forwardObjective = objectiveFunction.apply(forwardParams);
                
                gradient[i] = (forwardObjective - baseObjective) / forwardStep;
            } else if (backwardStep > 0) {
                // Can only use backward difference
                double[] backwardParams = parameters.clone();
                backwardParams[i] -= backwardStep;
                double backwardObjective = objectiveFunction.apply(backwardParams);
                
                gradient[i] = (baseObjective - backwardObjective) / backwardStep;
            } else {
                // At exact boundary and can't move - gradient is 0
                gradient[i] = 0.0;
            }
        }
        
        return gradient;
    }
    
    /**
     * Computes how small changes in parameters affect the objective function.
     * Returns a sensitivity array where each element [i] represents how sensitive
     * the objective function is to changes in parameter [i].
     * 
     * @param objectiveFunction Function that evaluates the objective value for a given set of parameters
     * @param parameters Current parameter values
     * @return Sensitivity values for each parameter
     */
    public double[] computeSensitivity(Function<double[], Double> objectiveFunction, double[] parameters) {
        double[] gradient = estimateGradient(objectiveFunction, parameters);
        double[] sensitivity = new double[gradient.length];
        
        // Compute absolute sensitivity values
        for (int i = 0; i < gradient.length; i++) {
            sensitivity[i] = Math.abs(gradient[i]);
        }
        
        return sensitivity;
    }
    
    /**
     * Gets the step size used for numerical gradient estimation.
     * 
     * @return The step size
     */
    public double getStepSize() {
        return stepSize;
    }
    
    /**
     * Sets the step size for numerical gradient estimation.
     * 
     * @param stepSize The new step size
     */
    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }
}