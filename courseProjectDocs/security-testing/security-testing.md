## Security Testing Results 

###  Test Scope and Coverage

The following project components were scanned using Snyk:
- Root project
- Submodules:
    - jabgui
    - jabkit
    - jablib
    - jabls
    - jabls-cli
    - jabsrv
    - jabsrv-cli
    - test-support
    - versions

The scan analyzed:
- Direct Gradle dependencies
- Transitive dependencies
- Dependency version vulnerabilities
- Known issues from public vulnerability databases

The scan did **not** test:
- Runtime behavior
- Network services
- Authentication mechanisms
- User input validation logic

---

###  Vulnerability Summary

| Title | Type | Severity | Recommended Fix | Reported By |
|------|------|----------|-----------------|-------------|
| Information Exposure in Kotlin Standard Library (SNYK-JAVA-ORGJETBRAINSKOTLIN-2393744) | Dependency Vulnerability | Low | Upgrade `org.jetbrains.kotlin:kotlin-stdlib` to version **2.1.0** or later | Member 1 |
| Deprecated Gradle Features Detected | Misconfiguration | Low | Update Gradle build scripts and plugins to remove deprecated features | Member 2 |
| Multiple Vulnerable Dependency Paths Detected | Configuration Weakness | Low | Refactor dependency tree to reduce transitive risk exposure | Member 3 |

---

### Execution and Results

**Tool Used:** Snyk (Gradle Plugin and needs api key to run)

**Commands Executed:**
```bash
./gradlew snyk-test
```

Most tools have issues working with Gradle submodules in the project, but through some trial and error synk seems to work.
The arguments to have it scan further subprojects seem not to work,so mainly initial folders were scanned to find vulnerabilities
### Group Contributions

| Member | Task                               | Notes                                                         |
| -------- |------------------------------------|---------------------------------------------------------------|
| Lucille | set up and scann project with snyk |                               |
| Geoffrey | tba                                | tba    |
| Vanessa | Ttba                               | tba                                           |
