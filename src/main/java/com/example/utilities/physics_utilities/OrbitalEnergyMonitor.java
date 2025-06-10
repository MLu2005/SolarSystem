package com.example.utilities.physics_utilities;

import com.example.solar_system.CelestialBody;
import com.example.utilities.Vector3D;

import java.util.ArrayList;
import java.util.List;

import static com.example.Constants.G;

public class OrbitalEnergyMonitor {

    private final List<CelestialBody> bodies;
    public final List<Double> energyHistory = new ArrayList<>();
    public final List<Double> timeHistory = new ArrayList<>();

    public OrbitalEnergyMonitor(List<CelestialBody> bodies) {
        this.bodies = bodies;
    }

    public void recordEnergy(double timeSeconds) {
        double totalKinetic = 0.0;
        double totalPotential = 0.0;

        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody bi = bodies.get(i);
            double mi = bi.getMass();
            Vector3D vi = bi.getVelocity();
            totalKinetic += 0.5 * mi * vi.magnitudeSquared();

            for (int j = i + 1; j < bodies.size(); j++) {
                CelestialBody bj = bodies.get(j);
                double mj = bj.getMass();
                double distance = bi.getPosition().subtract(bj.getPosition()).magnitude();
                totalPotential += -G * mi * mj / distance;
            }
        }

        double totalEnergy = totalKinetic + totalPotential;
        energyHistory.add(totalEnergy);
        timeHistory.add(timeSeconds);
    }

    public double getInitialEnergy() {
        return energyHistory.isEmpty() ? 0.0 : energyHistory.get(0);
    }

    public double getCurrentEnergy() {
        return energyHistory.isEmpty() ? 0.0 : energyHistory.get(energyHistory.size() - 1);
    }

    public double getRelativeEnergyDrift() {
        if (energyHistory.size() < 2) return 0.0;
        double initial = getInitialEnergy();
        double current = getCurrentEnergy();
        return (current - initial) / Math.abs(initial);
    }

    public void printStatus() {
        System.out.printf("Energy drift: %.6e%n", getRelativeEnergyDrift());
    }
}
