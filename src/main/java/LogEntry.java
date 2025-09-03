import java.time.LocalTime;

/**
 * Represents a single log entry from the CSV log file
 */
public class LogEntry {
    private final LocalTime timestamp;
    private final String jobDescription;
    private final String processType; // "START" or "END"
    private final long pid;
    
    public LogEntry(String timestamp, String jobDescription, String processType, String pid) {
        this.timestamp = LocalTime.parse(timestamp);
        this.jobDescription = jobDescription;
        this.processType = processType;
        this.pid = Long.parseLong(pid);
    }
    
    // Getters
    public LocalTime getTimestamp() { return timestamp; }
    public String getJobDescription() { return jobDescription; }
    public String getProcessType() { return processType; }
    public long getPid() { return pid; }
    
    // Using "START".equals() instead of processType.equals("START") to avoid NullPointerException
    public boolean isStart() { return "START".equals(processType); }
    public boolean isEnd() { return "END".equals(processType); }
}
