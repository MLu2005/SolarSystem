package com.example.lander;

public class CombinedController implements Controller {
    private final Controller feedForward;
    private final Controller feedback;

    public CombinedController(Controller feedForward, Controller feedback) {
        this.feedForward = feedForward;
        this.feedback    = feedback;
    }

    @Override
    public double getU(double t, double[] state) {
        double uPlan = feedForward.getU(t, state);
        double uCorr = feedback   .getU(t, state);
        double uTot  = uPlan + uCorr;
        if (uTot < 0)    uTot = 0;
        if (uTot > FeedbackController.U_MAX)
                         uTot = FeedbackController.U_MAX;
        return uTot;
    }

    @Override
    public double getV(double t, double[] state) {
        double vPlan = feedForward.getV(t, state);
        double vCorr = feedback   .getV(t, state);
        double vTot  = vPlan + vCorr;
        if (vTot >  FeedbackController.V_MAX) vTot =  FeedbackController.V_MAX;
        if (vTot < -FeedbackController.V_MAX) vTot = -FeedbackController.V_MAX;
        return vTot;
    }
}