# CSV Log Monitoring

A simple Java application to parse CSV log files and track job durations.

## Features

- Parses CSV log files with format: `HH:MM:SS,job_description,START/END,PID`
- Tracks job start and end times
- Calculates job durations
- Reports warnings for jobs taking longer than 5 minutes
- Reports errors for jobs taking longer than 10 minutes

## CSV Format

The CSV file should have the following format:
```
HH:MM:SS,job_description,START/END,PID
```

Example:
```
09:00:00,Data Processing Job,START,12345
09:02:30,Data Processing Job,END,12345
09:05:00,Backup Task,START,12346
09:12:00,Backup Task,END,12346
```

## Application Logic

The application follows a simple 4-step process:

1. **Validation** - Validates CSV format (4 columns, timestamp format, START/END values, numeric PID)
2. **Parsing** - Reads CSV file and creates `LogEntry` objects for each row
3. **Tracking** - Matches START/END pairs by PID to calculate job durations
4. **Reporting** - Generates performance report with warnings (>5 min) and errors (>10 min)

**Key Components:**
- `LogEntry` - Represents a single log row with timestamp, job description, process type, and PID
- `JobInfo` - Stores completed job information with calculated duration
- `CSVLogMonitoring` - Main orchestrator that coordinates the entire analysis process

## Usage

1. **Compile and run with your log file:**
```bash
mvn clean compile exec:java -Dexec.args="/path/to/your/logfile.csv"
```

2. **Test with sample data:**
```bash
mvn clean compile exec:java -Dexec.args="src/main/resources/sample_log.csv"
```

3. **Save output to file:**
```bash
mvn -q clean compile exec:java -Dexec.args="src/main/resources/sample_log.csv output.txt"
```

4. **Run tests:**
```bash
mvn test
```

5. **Package the application:**
```bash
mvn clean package
```

## Sample Output

```
CSV format validation passed
Parsed 88 log entries
Found 43 completed jobs

=== JOB ANALYSIS REPORT ===
OK: Job: scheduled task 032 (PID: 37980) - Duration: 0.55 minutes
WARNING: Job: scheduled task 074 (PID: 71766) - Duration: 5.78 minutes (took longer than 5 minutes)
ERROR: Job: scheduled task 051 (PID: 39547) - Duration: 11.48 minutes (took longer than 10 minutes)

=== SUMMARY ===
Total jobs: 43
Warnings (>5 min): 9
Errors (>10 min): 10
```

## Work in Progress

This application is currently in development and represents a foundation for CSV log monitoring. The current implementation provides core functionality for parsing CSV logs and generating basic performance reports.

### Potential Upgrades

The application can be enhanced with additional features such as:
- Configuration file for customizable thresholds (warning/error time limits)
- Real time monitoring, directory scanning with pattern matching
- Email notification for critical alerts
- Log level configuration


