import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * CSV Log Monitoring - Parses CSV log files and tracks job durations with warnings/errors
 */
public class CSVLogMonitoring {
    
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage: java CSVLogMonitoring <csv-file-path> <output-file-path>");
            System.out.println("Example: java CSVLogMonitoring src/main/resources/sample_log.csv");
            System.out.println("Example: java CSVLogMonitoring src/main/resources/sample_log.csv output.txt");
            System.exit(1);
        }
        
        String csvFile = args[0];
        String outputFile = args.length == 2 ? args[1] : null;
        
        CSVLogMonitoring analyzer = new CSVLogMonitoring();
        analyzer.analyzeLogFile(csvFile, outputFile);
    }
    
    /**
     * Main method that orchestrates the entire process
     */
    public void analyzeLogFile(String csvFilePath, String outputFilePath) {
        PrintWriter output = null;
        try {
            // Set up output destination (file or console)
            if (outputFilePath != null) {
                output = new PrintWriter(new FileWriter(outputFilePath));
            } else {
                output = new PrintWriter(System.out);
            }
            
            // Step 0: Validate CSV format before processing
            validateCSVFormat(csvFilePath);
            
            // Step 1: Parse CSV file into structured LogEntry objects
            List<LogEntry> logEntries = parseCSV(csvFilePath);
            
            // Step 2: Track job lifecycles and calculate durations
            List<JobInfo> completedJobs = trackJobs(logEntries);
            
            // Step 3: Generate performance report with warnings/errors
            generateReport(completedJobs, output);
            
        } catch (IOException e) {
            // Handle file I/O and CSV parsing errors gracefully
            System.err.println("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle CSV format validation errors
            System.err.println("CSV format error: " + e.getMessage());
        } finally {
            if (output != null && outputFilePath != null) {
                output.close();
            }
        }
    }
    
    /**
     * Validates column count, timestamp format, process type, and PID format
     */
    private void validateCSVFormat(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            
            for (CSVRecord record : parser) {
                // Check column count - must have exactly 4 columns
                if (record.size() != 4) {
                    throw new IllegalArgumentException(
                        "Line " + record.getRecordNumber() + ": Expected 4 columns, found " + record.size() +
                        " - Format should be: timestamp,job_description,START/END,PID"
                    );
                }
                
                // Check timestamp format (HH:MM:SS)
                try {
                    LocalTime.parse(record.get(0).trim());
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException(
                        "Line " + record.getRecordNumber() + ": Invalid timestamp format '" + record.get(0) + 
                        "' - Expected format: HH:MM:SS (e.g., 09:30:15)"
                    );
                }
                
                // Check process type (must be START or END)
                String processType = record.get(2).trim();
                if (!"START".equals(processType) && !"END".equals(processType)) {
                    throw new IllegalArgumentException(
                        "Line " + record.getRecordNumber() + ": Invalid process type '" + processType + 
                        "' - Must be either 'START' or 'END'"
                    );
                }
                
                // Check PID format (must be a valid number)
                try {
                    Long.parseLong(record.get(3).trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Line " + record.getRecordNumber() + ": Invalid PID format '" + record.get(3) + 
                        "' - Must be a valid number"
                    );
                }
            }
        }
    }
    
    /**
     * Parse CSV file into LogEntry objects using Apache Commons CSV
     */
    private List<LogEntry> parseCSV(String filePath) throws IOException {
        List<LogEntry> entries = new ArrayList<>();
        
        try (Reader reader = new FileReader(filePath);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            
            // Iterate through CSV records
            for (CSVRecord record : parser) {
                LogEntry entry = new LogEntry(
                    record.get(0).trim(),  // timestamp
                    record.get(1).trim(),  // job description
                    record.get(2).trim(),  // process type (START/END)
                    record.get(3).trim()   // PID
                );
                entries.add(entry);
            }
        }
        
        return entries;
    }
    
    /**
     * Track jobs and calculate durations
     */
    private List<JobInfo> trackJobs(List<LogEntry> logEntries) {
        Map<Long, LocalTime> startTimes = new HashMap<>();
        
        // List to store completed jobs with calculated durations
        List<JobInfo> completedJobs = new ArrayList<>();
        
        for (LogEntry entry : logEntries) {
            Long pid = entry.getPid();
            
            if (entry.isStart()) {
                // Job started - store the start time for this PID
                startTimes.put(pid, entry.getTimestamp());
            } else if (entry.isEnd()) {
                // Job ended - try to find matching start time
                LocalTime startTime = startTimes.remove(pid);
                
                // If startTime is null, it means we got an END without a matching START
                if (startTime != null) {
                    JobInfo jobInfo = new JobInfo(
                        entry.getJobDescription(),
                        entry.getPid(),
                        startTime,
                        entry.getTimestamp()
                    );
                    completedJobs.add(jobInfo);
                }
            }
        }
        
        return completedJobs;
    }
    
    /**
     * Generate report with warnings and errors based on duration thresholds
     */
    private void generateReport(List<JobInfo> jobs, PrintWriter output) {
        output.println("\n=== JOB ANALYSIS REPORT ===");
        
        int warningCount = 0;
        int errorCount = 0;
        
        for (JobInfo job : jobs) {
            double durationMinutes = job.getDurationInMinutes();
            
            if (durationMinutes > 10) {
                output.println("ERROR: " + job + " (took longer than 10 minutes)");
                errorCount++;
            } else if (durationMinutes > 5) {
                output.println("WARNING: " + job + " (took longer than 5 minutes)");
                warningCount++;
            } else {
                output.println("OK: " + job);
            }
        }
        
        output.println("\n=== SUMMARY ===");
        output.println("Total jobs: " + jobs.size());
        output.println("Warnings (>5 min): " + warningCount);
        output.println("Errors (>10 min): " + errorCount);
        output.flush();
    }
}
