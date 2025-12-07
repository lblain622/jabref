# Performance Testing

---

## Stress Test (Windows and Gradle-Based)

This repository contains a simple PowerShell-based stress-testing script for the JabRef project when running it via Gradle on Windows.
The script repeatedly launches JabRef through the Gradle wrapper (`gradlew.bat`), lets it run for a few seconds, gathers system metrics, and logs them for analysis.


### Requirements

- Windows 10
- PowerShell

### How to Execute the Script
From JabRef's root directory:
```bash
powershell -ExecutionPolicy Bypass -File .\stress_test.ps1
```
To specify the number of runs:
```bash
powershell -ExecutionPolicy Bypass -File .\stress_test.ps1 -runs 10
```

### Results
For each test run:

1. Starts a timer
2. Launches JabRef using:
    ```bash
    .\gradlew.bat run
    ```
3. Lets the application run for 5 seconds
4. Forces the process to stop
5. Records:
   - Duration 
   - CPU usage (%)
   - Available memory (MB)
6. Appends the results to a timestamped log file: `stress_log_YYYYMMDD_HHMMSS.txt`  
**Example:** `Run 1: Duration=5.4182487s CPU=61.990474350366% AvailableMem=2285MB`

---

