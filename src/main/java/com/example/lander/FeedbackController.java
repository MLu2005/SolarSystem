package com.example.lander;

public class FeedbackController implements Controller {
    public static final double MAX_THRUST = 10 * 1.352e-3;
    public static final double MAX_STEERING_RATE = 1.0;
    private final double proportional_gain_horizontal = 0.5;
    private final double derivative_gain_horizontal = 0.2;
    private final double max_tilt_angle = Math.toRadians(45.0);
    private final double proportional_gain_vertical = 0.6;
    private final double proportional_gain_tilt = 20.0;
    private final double derivative_gain_tilt = 10.0;

    @Override
    public double getU(double time, double[] state) {
        double vertical_velocity = state[3];
        double velocity_error = -vertical_velocity;
        if (Math.abs(velocity_error) < 1e-4) {
            velocity_error = 0;
        }
        double control_signal = proportional_gain_vertical * velocity_error;
        return clamp(control_signal, -MAX_THRUST, MAX_THRUST);
    }

    @Override
    public double getV(double time, double[] state) {
        double altitude = state[1];
        double angular_velocity = state[5];

        if (altitude <= 0.02) {
            return -Math.signum(angular_velocity) * MAX_STEERING_RATE;
        }

        double horizontal_position = state[0];
        double horizontal_velocity = state[2];
        double tilt_angle = state[4];

        double desired_tilt_angle = -(proportional_gain_horizontal * horizontal_position
                + derivative_gain_horizontal * horizontal_velocity);
        desired_tilt_angle = Math.max(-max_tilt_angle,
                Math.min(max_tilt_angle, desired_tilt_angle));

        double tilt_error = desired_tilt_angle - tilt_angle;
        double tilt_rate_error = -angular_velocity;
        double torque = proportional_gain_tilt * tilt_error
                + derivative_gain_tilt * tilt_rate_error;

        return clamp(torque, -MAX_STEERING_RATE, MAX_STEERING_RATE);
    }

    private double clamp(double value, double minimum, double maximum) {
        if (value < minimum) {
            return minimum;
        }
        if (value > maximum) {
            return maximum;
        }
        return value;
    }
}
