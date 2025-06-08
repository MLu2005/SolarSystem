package com.example.lander;

public class OpenLoopController implements Controller {
    private static final double[][] BURNS = {
        {     0.0, -80.04850434341043,  -43.94430155323007},
        { 86400.0,  55.58131104689241,  213.64400318220342},
        {172800.0,  68.97168415712228, -140.6078877540702 },
        {259200.0, -41.09315567406595,   42.93089451447104},
        {345600.0,  -8.948848762144895,  -6.6411424028483435}
    };

    @Override
    public double getU(double time, double[] state) {
        for (double[] b : BURNS) {
            if (Math.abs(time - b[0]) < 1e-6) {
                return Math.hypot(b[1], b[2]);
            }
        }
        return 0.0;
    }

    @Override
    public double getV(double time, double[] state) {
        return 0.0;
    }
}
