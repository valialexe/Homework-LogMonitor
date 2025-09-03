import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests core functionality of LogEntry and JobInfo classes
 */
public class CSVLogMonitoringTest {
    
    @Test
    public void testLogEntryCreation() {
        // Test LogEntry object creation and getter methods
        LogEntry entry = new LogEntry("09:00:00", "Test Job", "START", "12345");
        
        // Verify all getter methods return expected values
        assertEquals("Test Job", entry.getJobDescription());
        assertEquals("START", entry.getProcessType());
        assertEquals(12345L, entry.getPid());
        
        // Test boolean helper methods
        assertTrue(entry.isStart());
        assertFalse(entry.isEnd());
    }
    
    @Test
    public void testJobInfoDuration() {
        // Test JobInfo duration calculation with 5-minute difference
        JobInfo job = new JobInfo("Test Job", 12345L, 
                                 java.time.LocalTime.of(9, 0, 0),  // 09:00:00
                                 java.time.LocalTime.of(9, 5, 0)); // 09:05:00
        
        // Verify duration calculation is accurate (5.0 minutes)
        assertEquals(5.0, job.getDurationInMinutes(), 0.01);
        assertEquals("Test Job", job.getJobDescription());
        assertEquals(12345L, job.getPid());
    }
    
    @Test
    public void testLogEntryEndProcess() {
        // Test LogEntry with END process type
        LogEntry entry = new LogEntry("10:30:15", "Backup Task", "END", "67890");
        
        // Verify END process type behavior
        assertFalse(entry.isStart());
        assertTrue(entry.isEnd());
        assertEquals("END", entry.getProcessType());
        assertEquals(67890L, entry.getPid());
    }
    
    @Test
    public void testJobInfoWarningThreshold() {
        // Test job that should trigger warning (>5 minutes)
        JobInfo job = new JobInfo("Slow Job", 99999L,
                                 java.time.LocalTime.of(9, 0, 0),  // 09:00:00
                                 java.time.LocalTime.of(9, 7, 30)); // 09:07:30 (7.5 minutes)
        
        // Verify duration is above warning threshold
        assertTrue(job.getDurationInMinutes() > 5.0);
        assertEquals(7.5, job.getDurationInMinutes(), 0.01);
    }
    
    @Test
    public void testJobInfoErrorThreshold() {
        // Test job that should trigger error (>10 minutes)
        JobInfo job = new JobInfo("Very Slow Job", 11111L,
                                 java.time.LocalTime.of(9, 0, 0),  // 09:00:00
                                 java.time.LocalTime.of(9, 15, 0)); // 09:15:00 (15 minutes)
        
        // Verify duration is above error threshold
        assertTrue(job.getDurationInMinutes() > 10.0);
        assertEquals(15.0, job.getDurationInMinutes(), 0.01);
    }
}
