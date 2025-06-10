import com.example.utilities.HillClimb.InsertionThrustSchedule;
import com.example.utilities.HillClimb.TitanInsertionHillClimbing;
import com.example.utilities.Vector3D;
import executables.Constants;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class TitanInsertionHillClimbingTest {

    @Test
    void testResultClassCreation() {
        // Create a simple thrust schedule
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(3, 100.0);
        schedule.setDeltaVAt(0, new Vector3D(10.0, 20.0, 30.0));
        schedule.setDeltaVAt(1, new Vector3D(40.0, 50.0, 60.0));
        schedule.setDeltaVAt(2, new Vector3D(70.0, 80.0, 90.0));

        // Create a Result object
        double approachTime = 1000.0;
        double cost = 500.0;
        TitanInsertionHillClimbing.Result result = new TitanInsertionHillClimbing.Result(schedule, approachTime, cost);

        // Verify the Result object properties
        assertEquals(schedule, result.schedule);
        assertEquals(approachTime, result.approachTimeSec);
        assertEquals(cost, result.cost);

        // Test toString method
        String resultString = result.toString();
        assertNotNull(resultString);
        assertTrue(resultString.contains("Approach time: 1000.0"));
        assertTrue(resultString.contains("Total cost: 500.000"));
    }

    @Test
    void testInsertionThrustScheduleCreation() {
        // Test creating a schedule with specific parameters
        int numSlots = 5;
        double slotDuration = 86400.0;
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(numSlots, slotDuration);

        // Verify the schedule properties
        assertEquals(numSlots, schedule.getNumSlots());
        assertEquals(slotDuration, schedule.getSlotDuration());

        // Verify initial delta-V values are zero
        for (int i = 0; i < numSlots; i++) {
            Vector3D dv = schedule.getDeltaVAt(i);
            assertEquals(0.0, dv.getX());
            assertEquals(0.0, dv.getY());
            assertEquals(0.0, dv.getZ());
        }
    }

    @Test
    void testInsertionThrustScheduleSetAndGetDeltaV() {
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
        assertEquals(dv1.getX(), retrievedDv1.getX(), 0.001);
        assertEquals(dv1.getY(), retrievedDv1.getY(), 0.001);
        assertEquals(dv1.getZ(), retrievedDv1.getZ(), 0.001);

        Vector3D retrievedDv2 = schedule.getDeltaVAt(1);
        assertEquals(dv2.getX(), retrievedDv2.getX(), 0.001);
        assertEquals(dv2.getY(), retrievedDv2.getY(), 0.001);
        assertEquals(dv2.getZ(), retrievedDv2.getZ(), 0.001);

        Vector3D retrievedDv3 = schedule.getDeltaVAt(2);
        assertEquals(dv3.getX(), retrievedDv3.getX(), 0.001);
        assertEquals(dv3.getY(), retrievedDv3.getY(), 0.001);
        assertEquals(dv3.getZ(), retrievedDv3.getZ(), 0.001);
    }

    @Test
    void testInsertionThrustScheduleTotalDeltaVMagnitude() {
        // Create a schedule
        InsertionThrustSchedule schedule = new InsertionThrustSchedule(3, 100.0);

        // Set delta-V values
        schedule.setDeltaVAt(0, new Vector3D(3.0, 0.0, 0.0));  // magnitude 3
        schedule.setDeltaVAt(1, new Vector3D(0.0, 4.0, 0.0));  // magnitude 4
        schedule.setDeltaVAt(2, new Vector3D(0.0, 0.0, 12.0)); // magnitude 12

        // Verify total delta-V magnitude
        double expectedTotalMagnitude = 3.0 + 4.0 + 12.0;
        assertEquals(expectedTotalMagnitude, schedule.getTotalDeltaVMagnitude(), 0.001);
    }

    @Test
    void testInsertionThrustScheduleClone() {
        // Create a schedule
        InsertionThrustSchedule original = new InsertionThrustSchedule(3, 100.0);
        original.setDeltaVAt(0, new Vector3D(10.0, 20.0, 30.0));
        original.setDeltaVAt(1, new Vector3D(40.0, 50.0, 60.0));
        original.setDeltaVAt(2, new Vector3D(70.0, 80.0, 90.0));

        // Clone the schedule
        InsertionThrustSchedule clone = original.clone();

        // Verify the clone has the same properties
        assertEquals(original.getNumSlots(), clone.getNumSlots());
        assertEquals(original.getSlotDuration(), clone.getSlotDuration());

        for (int i = 0; i < original.getNumSlots(); i++) {
            Vector3D originalDv = original.getDeltaVAt(i);
            Vector3D cloneDv = clone.getDeltaVAt(i);

            assertEquals(originalDv.getX(), cloneDv.getX(), 0.001);
            assertEquals(originalDv.getY(), cloneDv.getY(), 0.001);
            assertEquals(originalDv.getZ(), cloneDv.getZ(), 0.001);
        }

        // Modify the clone and verify it doesn't affect the original
        clone.setDeltaVAt(0, new Vector3D(100.0, 200.0, 300.0));

        Vector3D originalDv = original.getDeltaVAt(0);
        Vector3D cloneDv = clone.getDeltaVAt(0);

        assertEquals(10.0, originalDv.getX(), 0.001);
        assertEquals(20.0, originalDv.getY(), 0.001);
        assertEquals(30.0, originalDv.getZ(), 0.001);

        assertEquals(100.0, cloneDv.getX(), 0.001);
        assertEquals(200.0, cloneDv.getY(), 0.001);
        assertEquals(300.0, cloneDv.getZ(), 0.001);
    }

    @Test
    void testInsertionThrustScheduleThrustCalculation() {
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
        assertEquals(expectedThrust, actualThrust, 0.001);
    }

    @Test
    void testInsertionThrustScheduleDeltaVClamping() {
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

        assertEquals(maxDeltaV, actualMagnitude, 0.001);
        // Direction should be preserved
        assertEquals(1.0, actualDv.getX() / actualMagnitude, 0.001);
    }
}
