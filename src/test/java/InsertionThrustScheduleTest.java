import com.example.utilities.HillClimb.InsertionThrustSchedule;
import com.example.utilities.Vector3D;
import com.example.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InsertionThrustScheduleTest {

    @Test
    void testConstructorAndInitialization() {
        // Test creating a schedule with specific parameters
        int numSlots = 5;
        double slotDuration = 100.0;
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(numSlots, slotDuration);

        // Verify the schedule properties
        assertEquals(numSlots, schedule.getNumSlots(), "Number of slots should match constructor parameter");
        assertEquals(slotDuration, schedule.getSlotDuration(), "Slot duration should match constructor parameter");

        // Verify initial delta-V values are zero
        for (int i = 0; i < numSlots; i++) {
            Vector3D dv = schedule.getDeltaVAt(i);
            assertEquals(0.0, dv.getX(), "Initial X component should be zero");
            assertEquals(0.0, dv.getY(), "Initial Y component should be zero");
            assertEquals(0.0, dv.getZ(), "Initial Z component should be zero");
            assertEquals(0.0, schedule.getFuelUsedAt(i), "Initial fuel used should be zero");
        }

        assertEquals(0.0, schedule.getTotalFuelUsed(), "Initial total fuel used should be zero");
        assertEquals(0.0, schedule.getTotalDeltaVMagnitude(), "Initial total delta-V magnitude should be zero");
    }

    @Test
    void testSetAndGetDeltaV() {
        // Create a schedule
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(3, 100.0);

        // Set delta-V values
        Vector3D dv1 = new Vector3D(10.0, 20.0, 30.0);
        schedule.setDeltaVAt(0, dv1);

        Vector3D dv2 = new Vector3D(40.0, 50.0, 60.0);
        schedule.setDeltaVAt(1, dv2);

        Vector3D dv3 = new Vector3D(70.0, 80.0, 90.0);
        schedule.setDeltaVAt(2, dv3);

        // Verify the delta-V values
        Vector3D retrievedDv1 = schedule.getDeltaVAt(0);
        assertEquals(dv1.getX(), retrievedDv1.getX(), 0.001, "X component should match set value");
        assertEquals(dv1.getY(), retrievedDv1.getY(), 0.001, "Y component should match set value");
        assertEquals(dv1.getZ(), retrievedDv1.getZ(), 0.001, "Z component should match set value");

        Vector3D retrievedDv2 = schedule.getDeltaVAt(1);
        assertEquals(dv2.getX(), retrievedDv2.getX(), 0.001, "X component should match set value");
        assertEquals(dv2.getY(), retrievedDv2.getY(), 0.001, "Y component should match set value");
        assertEquals(dv2.getZ(), retrievedDv2.getZ(), 0.001, "Z component should match set value");

        Vector3D retrievedDv3 = schedule.getDeltaVAt(2);
        assertEquals(dv3.getX(), retrievedDv3.getX(), 0.001, "X component should match set value");
        assertEquals(dv3.getY(), retrievedDv3.getY(), 0.001, "Y component should match set value");
        assertEquals(dv3.getZ(), retrievedDv3.getZ(), 0.001, "Z component should match set value");
    }

    @Test
    void testTotalDeltaVMagnitude() {
        // Create a schedule
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(3, 100.0);

        // Set delta-V values with known magnitudes
        schedule.setDeltaVAt(0, new Vector3D(3.0, 0.0, 0.0));  // magnitude 3
        schedule.setDeltaVAt(1, new Vector3D(0.0, 4.0, 0.0));  // magnitude 4
        schedule.setDeltaVAt(2, new Vector3D(0.0, 0.0, 12.0)); // magnitude 12

        // Verify total delta-V magnitude
        double expectedTotalMagnitude = 3.0 + 4.0 + 12.0;
        assertEquals(expectedTotalMagnitude, schedule.getTotalDeltaVMagnitude(), 0.001, 
                "Total delta-V magnitude should be the sum of individual magnitudes");
    }

    @Test
    void testClone() {
        // Create a schedule
        InsertionThrustSchedule original = new InsertionThrustSchedule(3, 100.0);
        original.setDeltaVAt(0, new Vector3D(10.0, 20.0, 30.0));
        original.setDeltaVAt(1, new Vector3D(40.0, 50.0, 60.0));
        original.setDeltaVAt(2, new Vector3D(70.0, 80.0, 90.0));

        // Clone the schedule
        InsertionThrustSchedule clone = original.clone();

        // Verify the clone has the same properties
        assertEquals(original.getNumSlots(), clone.getNumSlots(), "Clone should have same number of slots");
        assertEquals(original.getSlotDuration(), clone.getSlotDuration(), "Clone should have same slot duration");
        assertEquals(original.getTotalDeltaVMagnitude(), clone.getTotalDeltaVMagnitude(), 0.001, 
                "Clone should have same total delta-V magnitude");
        assertEquals(original.getTotalFuelUsed(), clone.getTotalFuelUsed(), 0.001, 
                "Clone should have same total fuel used");

        for (int i = 0; i < original.getNumSlots(); i++) {
            Vector3D originalDv = original.getDeltaVAt(i);
            Vector3D cloneDv = clone.getDeltaVAt(i);

            assertEquals(originalDv.getX(), cloneDv.getX(), 0.001, "Clone X component should match original");
            assertEquals(originalDv.getY(), cloneDv.getY(), 0.001, "Clone Y component should match original");
            assertEquals(originalDv.getZ(), cloneDv.getZ(), 0.001, "Clone Z component should match original");
            assertEquals(original.getFuelUsedAt(i), clone.getFuelUsedAt(i), 0.001, 
                    "Clone fuel used should match original");
        }

        // Modify the clone and verify it doesn't affect the original
        clone.setDeltaVAt(0, new Vector3D(100.0, 200.0, 300.0));

        Vector3D originalDv = original.getDeltaVAt(0);
        Vector3D cloneDv = clone.getDeltaVAt(0);

        assertEquals(10.0, originalDv.getX(), 0.001, "Original X component should remain unchanged");
        assertEquals(20.0, originalDv.getY(), 0.001, "Original Y component should remain unchanged");
        assertEquals(30.0, originalDv.getZ(), 0.001, "Original Z component should remain unchanged");

        assertEquals(100.0, cloneDv.getX(), 0.001, "Clone X component should be updated");
        assertEquals(200.0, cloneDv.getY(), 0.001, "Clone Y component should be updated");
        assertEquals(300.0, cloneDv.getZ(), 0.001, "Clone Z component should be updated");
    }

    @Test
    void testThrustCalculation() {
        // Create a schedule
        double slotDuration = 10.0; // 10 seconds
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(1, slotDuration);

        // Set a delta-V that would require a specific thrust
        double dvMagnitude = 100.0; // 100 m/s
        schedule.setDeltaVAt(0, new Vector3D(dvMagnitude, 0.0, 0.0));

        // Calculate expected thrust
        double expectedThrust = Constants.PROBE_MASS * dvMagnitude * 1000.0 / slotDuration;

        // Verify thrust calculation
        double actualThrust = schedule.getThrustAt(0);
        assertEquals(expectedThrust, actualThrust, 0.001, "Thrust calculation should match expected value");

        // Test thrust vector calculation
        Vector3D thrustVector = schedule.getThrustVectorAt(0, Constants.PROBE_MASS);
        assertEquals(expectedThrust, thrustVector.getX(), 0.001, "X component of thrust vector should match expected value");
        assertEquals(0.0, thrustVector.getY(), 0.001, "Y component of thrust vector should be zero");
        assertEquals(0.0, thrustVector.getZ(), 0.001, "Z component of thrust vector should be zero");
    }

    @Test
    void testDeltaVClamping() {
        // Create a schedule
        double slotDuration = 10.0; // 10 seconds
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(1, slotDuration);

        // Calculate max allowed delta-V based on F_T_MAX
        double maxDeltaV = (Constants.F_T_MAX * slotDuration) / Constants.PROBE_MASS;

        // Try to set a delta-V that exceeds the maximum
        double excessiveDvMagnitude = maxDeltaV * 2.0;
        schedule.setDeltaVAt(0, new Vector3D(excessiveDvMagnitude, 0.0, 0.0));

        // Verify the delta-V was clamped
        Vector3D actualDv = schedule.getDeltaVAt(0);
        double actualMagnitude = actualDv.magnitude();

        assertEquals(maxDeltaV, actualMagnitude, 0.001, "Delta-V magnitude should be clamped to maximum allowed value");
        // Direction should be preserved
        assertEquals(1.0, actualDv.getX() / actualMagnitude, 0.001, "Direction of delta-V should be preserved");
    }

    @Test
    void testFuelUsageCalculation() {
        // Create a schedule
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(3, 100.0);

        // Set delta-V values
        Vector3D dv1 = new Vector3D(3.0, 0.0, 0.0);  // magnitude 3
        schedule.setDeltaVAt(0, dv1);

        Vector3D dv2 = new Vector3D(0.0, 4.0, 0.0);  // magnitude 4
        schedule.setDeltaVAt(1, dv2);

        Vector3D dv3 = new Vector3D(0.0, 0.0, 12.0); // magnitude 12
        schedule.setDeltaVAt(2, dv3);

        // Calculate expected fuel usage for each slot
        double expectedFuel1 = Constants.PROBE_MASS * dv1.magnitude();
        double expectedFuel2 = Constants.PROBE_MASS * dv2.magnitude();
        double expectedFuel3 = Constants.PROBE_MASS * dv3.magnitude();
        double expectedTotalFuel = expectedFuel1 + expectedFuel2 + expectedFuel3;

        // Verify fuel usage calculations
        assertEquals(expectedFuel1, schedule.getFuelUsedAt(0), 0.001, "Fuel used at slot 0 should match expected value");
        assertEquals(expectedFuel2, schedule.getFuelUsedAt(1), 0.001, "Fuel used at slot 1 should match expected value");
        assertEquals(expectedFuel3, schedule.getFuelUsedAt(2), 0.001, "Fuel used at slot 2 should match expected value");
        assertEquals(expectedTotalFuel, schedule.getTotalFuelUsed(), 0.001, "Total fuel used should match expected value");
    }
}
