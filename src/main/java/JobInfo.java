import java.time.Duration;
import java.time.LocalTime;

/**
 * Represents information about a job including start time, end time, and calculated duration
 */
public class JobInfo {
    private String jobDescription;
    private long pid;
    private LocalTime startTime;
    private LocalTime endTime;
    private Duration duration;
    
    public JobInfo(String jobDescription, long pid, LocalTime startTime, LocalTime endTime) {
        this.jobDescription = jobDescription;
        this.pid = pid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = Duration.between(startTime, endTime);
    }
    
    /**
     * Gets duration in minutes
     * Uses duration.toMillis() for decimal precision since toMinutes() only returns whole minutes
     */
    public double getDurationInMinutes() {
        return duration.toMillis() / (1000.0 * 60.0);
    }
    
    // Getters
    public String getJobDescription() { return jobDescription; }
    public long getPid() { return pid; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    
    @Override
    public String toString() {
        return String.format("Job: %s (PID: %d) - Duration: %.2f minutes", 
                           jobDescription, pid, getDurationInMinutes());
    }
}
