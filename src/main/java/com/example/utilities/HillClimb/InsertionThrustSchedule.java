package com.example.utilities.HillClimb;

import com.example.utilities.GA.Individual;
import com.example.utilities.Vector3D;
import java.util.Vector;

/**
 * An InsertionThrustSchedule represents a discrete sequence of instantaneous ΔV impulses
 * (in m/s) applied in consecutive “slots” around Titan approach.
 */
public class InsertionThrustSchedule implements Cloneable {
    // ΔV vectors (in m/s) for each time slot
    private Vector3D[] deltaVSlots;
    // Duration of each slot in seconds
    private double slotDurationSec;

    /**
     * Construct a schedule with nSlots, each of length slotDurationSec.
     * All ΔV vectors are initially zero.
     */
    public InsertionThrustSchedule(int nSlots, double slotDurationSec) {
        this.slotDurationSec = slotDurationSec;
        this.deltaVSlots = new Vector3D[nSlots];
        for (int i = 0; i < nSlots; i++) {
            this.deltaVSlots[i] = new Vector3D(0.0, 0.0, 0.0);
        }
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
    public void setDeltaVAt(int i, Vector3D dv) {
        deltaVSlots[i] = dv;
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

    /** Return a deep copy of this schedule. */
    @Override
    public InsertionThrustSchedule clone() {
        InsertionThrustSchedule copy = new InsertionThrustSchedule(deltaVSlots.length, slotDurationSec);
        for (int i = 0; i < deltaVSlots.length; i++) {
            Vector3D v = deltaVSlots[i];
            copy.deltaVSlots[i] = new Vector3D(v.x, v.y, v.z);
        }
        return copy;
    }

    /**
     * Creates an InsertionThrustSchedule from an Individual's genome.
     * The genome is expected to contain 3*nSlots values representing the x, y, z components
     * of the delta-V vectors for each slot.
     * 
     * @param individual The Individual containing the genome
     * @param slotDurationSec The duration of each slot in seconds
     * @return A new InsertionThrustSchedule with delta-V vectors from the genome
     */
    public static InsertionThrustSchedule fromGenome(Individual individual, double slotDurationSec) {
        Vector<Double> genes = individual.genes();
        int nSlots = genes.size() / 3;

        InsertionThrustSchedule schedule = new InsertionThrustSchedule(nSlots, slotDurationSec);

        for (int i = 0; i < nSlots; i++) {
            double x = genes.get(i * 3);
            double y = genes.get(i * 3 + 1);
            double z = genes.get(i * 3 + 2);
            schedule.setDeltaVAt(i, new Vector3D(x, y, z));
        }

        return schedule;
    }
}
