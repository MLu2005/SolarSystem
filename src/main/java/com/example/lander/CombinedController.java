package com.example.lander;

public class CombinedController implements Controller {
    private final Controller feedForwardController;
    private final Controller feedbackController;

    public CombinedController(Controller feedForwardController, Controller feedbackController) {
        this.feedForwardController = feedForwardController;
        this.feedbackController = feedbackController;
    }

    @Override
    public double getU(double time, double[] state) {
        double plannedControl = feedForwardController.getU(time, state);
        double correctiveControl = feedbackController.getU(time, state);
        double totalControl = plannedControl + correctiveControl;
        
        if (totalControl < 0) {
            totalControl = 0;
        }
        if (totalControl > FeedbackController.MAX_THRUST) {
            totalControl = FeedbackController.MAX_THRUST;
        }
        
        return totalControl;
    }

    @Override
    public double getV(double time, double[] state) {
        double plannedSteering = feedForwardController.getV(time, state);
        double correctiveSteering = feedbackController.getV(time, state);
        double totalSteering = plannedSteering + correctiveSteering;
        
        if (totalSteering > FeedbackController.MAX_STEERING_RATE) {
            totalSteering = FeedbackController.MAX_STEERING_RATE;
        }
        if (totalSteering < -FeedbackController.MAX_STEERING_RATE) {
            totalSteering = -FeedbackController.MAX_STEERING_RATE;
        }
        
        return totalSteering;
    }
}