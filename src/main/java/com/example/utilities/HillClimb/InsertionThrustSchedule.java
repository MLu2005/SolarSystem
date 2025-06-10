package com.example.utilities.HillClimb;

import com.example.utilities.Vector3D;
import com.example.Constants;

/**
 * An InsertionThrustSchedule represents a discrete sequence of instantaneous ΔV impulses
 * (in m/s) applied in consecutive “slots” around Titan approach.
 */
public class InsertionThrustSchedule implements Cloneable {
    // Vectors (in m/s) for each time slot
    private Vector3D[] deltaVSlots;
    // Duration of each slot in seconds
    private double slotDurationSec;

    private double[] fuelUsedSlots;

    /**
     * Construct a schedule with nSlots, each of length slotDurationSec.
     * All ΔV vectors are initially zero.
     */
    public InsertionThrustSchedule(int nSlots, double slotDurationSec) {
        this.slotDurationSec = slotDurationSec;
        this.deltaVSlots = new Vector3D[nSlots];
        this.fuelUsedSlots  = new double[nSlots];
        for (int i = 0; i < nSlots; i++) {
            this.deltaVSlots[i] = new Vector3D(0.0, 0.0, 0.0);
            this.fuelUsedSlots[i] = 0.0;
        }
    }

    /**
     * Maximum allowed Δv magnitude in m/s for one slot,
     * given the impulse cap F_T_MAX * slotDurationSec.
     */
    private double maxDeltaV() {
        // F = m * a => a = F / m
        return (Constants.F_T_MAX * slotDurationSec) / Constants.PROBE_MASS;
    }

    /**
     * Returns the number of ΔV “slots” in this schedule.
     */
    public int getNumSlots() {
        return deltaVSlots.length;
    }

    /**
     * Returns the ΔV vector (in m/s) scheduled at index i [0..nSlots−1].
     */
    public Vector3D getDeltaVAt(int i) {
        return deltaVSlots[i];
    }

    /**
     * Sets the ΔV vector (in m/s) for a particular slot index [0..nSlots−1].
     */
    /**
     * Sets the ΔV vector (in m/s) for a particular slot index [0..nSlots−1],
     * but clamps its magnitude so it never exceeds the hard thrust cap.
     */
    public void setDeltaVAt(int i, Vector3D dv) {
        double dvMag = dv.magnitude();
        double dvMax = maxDeltaV();

        if (dvMag > dvMax) {
            // shrink it down so |dv| = dvMax, preserving direction
            dv = dv.normalize().scale(dvMax);
        }
        deltaVSlots[i] = dv;
        // — fuel (kg) = impulse (N·s) = m * |Δv|  [per manual’s C_I = ‖I‖ × 1]
        double impulse = Constants.PROBE_MASS * dv.magnitude();
        fuelUsedSlots[i] = impulse;
    }

    public double getFuelUsedAt(int i) {
        return fuelUsedSlots[i];
    }

    public double getTotalFuelUsed() {
        double sum = 0.0;
        for (double f : fuelUsedSlots) sum+= f;
        return sum;
    }


    /**
     * Returns the total ΔV magnitude (in m/s) summed over all slots.
     */
    public double getTotalDeltaVMagnitude() {
        double sum = 0.0;
        for (Vector3D dv : deltaVSlots) {
            sum += dv.magnitude();
        }
        return sum;
    }

    /**
     * Returns the slot‐duration in seconds.
     */
    public double getSlotDuration() {
        return slotDurationSec;
    }

    /**
     * Compute the magnitude of the thrust (in N) that would be required
     * to produce this ΔV over one slot.
     *
     * @param slotIdx   which burn slot
     * @return thrust in newtons
     */
    public double getThrustAt(int slotIdx) {
        // ΔV is stored in km/s; convert to m/s
        Vector3D dV = getDeltaVAt(slotIdx);
        double dvMs = dV.magnitude() * 1_000.0;

        // impulse I = m * Δv  →  units kg·m/s
        double impulse = Constants.PROBE_MASS * dvMs;

        // slotDurationSec is your existing slotDuration in seconds
        return impulse / slotDurationSec;
    }

    public Vector3D getThrustVectorAt(int slotIdx, double massKg) {
        Vector3D dV = getDeltaVAt(slotIdx).scale(1_000.0);   // → m/s
        Vector3D impulse= dV.scalarMultiply(massKg);            // → kg·m/s per component
        return impulse.scale(1.0 / slotDurationSec);              // → N per component
    }

    /** Return a deep copy of this schedule. */
    @Override
    public InsertionThrustSchedule clone() {
        InsertionThrustSchedule copy = new InsertionThrustSchedule(deltaVSlots.length, slotDurationSec);
        for (int i = 0; i < deltaVSlots.length; i++) {
            Vector3D v = deltaVSlots[i];
            copy.deltaVSlots[i] = new Vector3D(v.x, v.y, v.z);
            copy.fuelUsedSlots[i] = this.fuelUsedSlots[i];
        }
        return copy;
    }

}
