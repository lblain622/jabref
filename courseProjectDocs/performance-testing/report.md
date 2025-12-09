# Performance Testing

---

## Stress Test
### 1. Test Scope and Design

This stress test focused on **repeated application startup** of JabRef to evaluate stability under heavy launch cycles.  
No UI interaction was performed.  
**Tool Used:** Custom PowerShell script (`stress_test.ps1`) that repeatedly runs `gradlew.bat run`, monitors resource usage, and logs metrics.  
**Tested component:** JabRef startup sequence (JavaFX initialization, dependency loading, Gradle wrapper execution)

### 2. Configuration

- **Test Type:** Stress Test
- **Number of runs:** 10 consecutive launches (configurable)
- **Duration per run:** Application allowed to run for ~5 seconds before forced termination
- **Load pattern:** Continuous rapid restarts with no cool-down period
- **Users/requests simulated:** Single process repeatedly stress-cycled (equivalent to high-frequency user launches)

### 3. Results

Sample metrics collected (CPU, memory, startup duration):
```aiignore
Run 1: Duration=5.4182487s CPU=61.990474350366% AvailableMem=2285MB
Run 2: Duration=5.0301904s CPU=74.3528104246294% AvailableMem=1921MB
Run 3: Duration=5.0739818s CPU=95.5376284591105% AvailableMem=1700MB
Run 4: Duration=5.0630017s CPU=100% AvailableMem=1544MB
Run 5: Duration=5.0528118s CPU=100% AvailableMem=1255MB
Run 6: Duration=5.0675709s CPU=100% AvailableMem=1046MB
Run 7: Duration=5.1321829s CPU=100% AvailableMem=738MB
Run 8: Duration=5.0845048s CPU=100% AvailableMem=576MB
Run 9: Duration=5.1145317s CPU=100% AvailableMem=534MB
Run 10: Duration=5.050668s CPU=100% AvailableMem=464MB
```

Trends observed:
- **CPU usage:** 60–100% during startup
- **Memory:** Available Memory drastically decreased per successive runs
- **Duration:** Startup time consistent within +0.5s of the 5s window

### 4. Performance Findings

- **Finding 1 — CPU Saturation:**  
CPU usage climbed rapidly across runs, reaching 100% from Run 4 onward, indicating that repeated rapid startup cycles fully saturated the CPU.
This suggests that JabRef’s initialization path is CPU-bound under stress.

- **Finding 2 — Progressive Memory Decline:**  
Available memory dropped sharply from 2285 MB (Run 1) to 464 MB (Run 10).
While some of this is normal Windows caching, the consistent decline suggests that repeated startup may trigger memory pressure or temporary allocation buildup that is not fully reclaimed between runs.

- **Finding 3 — Stable Startup Duration Despite High Load:**  
Even under 100% CPU utilization and decreasing memory availability, startup times stayed within 5.03–5.42 seconds,
showing that the critical startup path is stable but constrained by system resource exhaustion.

- **Finding 4 — System-Level Bottleneck Detected:**  
The stress test does not reveal a JabRef crash, but the system reaches a point where both CPU and RAM are heavily taxed.
The bottleneck appears environmental: repeated launches overwhelm the system before revealing deeper application-level issues.

---

## Load Test

###  Scope and Design
 The load testing mainly focuses on evaluating the performance of backend uner a sustained workload. We uses multiple threads to perform serveal operations such as inserting a ey entry, search the entries, and modify entries.
 **Tool Used:** Custom Java-based load testing utility integrated into the project.
 **Tested component:** JabRef backend database layer (entry insertion, search, and modification logic)
### Configuration

**Threads:** 30 worker threads 

**Operations per thread: 1000 operations per thread 

**Timeout threshold:** 5000 ms (operations exceeding this time were classified as timeouts)

**Load pattern:** Continuous mixed read/write operations with short randomized delays

### Results
See `load-test-results` folder for detailed results.

### Performance Findings
- **Finding 1 — Stable Throughput Under Moderate Concurrency:**
  With 30 concurrent threads and 30,000 total operations, the system completed the full workload without failures or crashes. This indicates the backend can reliably sustainat high levels
- **Finding 2 —Low Average Response Time:**
  The average response time of 1.22 ms shows that individual operations were processed very quickly, suggesting minimal internal processing latency under this load.
-  **Finding 3 —No Timeouts or Failures Observed:**
   The system handled the full test run without thread timeouts or failed operations, showing good stability at the tested level.
- **Finding 4 — Mostly Low CPU Usage with Later Spike:**
  CPU usage remained very low throughout most test, and available memory decreased gradually but remained stable. The highest CPU usages got were 96% towards the end of the operation set, suggesting that the system was not overloaded.

## Group Contributions
| Member   | Contribution                                                                                 |
|----------|----------------------------------------------------------------------------------------------|
| Geoffrey | Implemented and executed Windows stress test script, collected metrics, summarized findings. |
| Lucille  | Implemented Load testing using Java,collected and stored metrics                             |

