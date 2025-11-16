# Integration Testing
## PdfImporter & ParserResult Integration Testing
### Description
This integration test verifies the interaction between the `PdfImporter` module and the `ParserResult` / `BibEntry` data model.  
It ensures that the importer correctly creates and connects entries within the parsing result structure.

### How to Run the Tests
From the repository root, run the following command:
```bash
./gradlew :jablib:test --tests "org.jabref.integration.PdfImporterIntegrationTest"
./gradlew :jablib:test --tests "org.jabref.integration.DuplicateCheckIntegrationTest"
```

### Viewing the Results
After running the tests, open the report generated at:
```
jablib/build/reports/tests/index.html
```
The report shows detailed results, including any passing or failing test cases.
---

## Database Duplication Integration Test
### Description
Tests the interaction between database storage and duplicate detection logic.
The goal of this test is to verify that the database is correctly updated when a duplicate entry is detected.

### How to Run the Tests
From the repository root, run the following command:
```bash
./gradlew :jablib:test --tests "org.jabref.integration.DuplicateCheckIntegrationTest"
```

### Viewing the Results
After running the tests, open the report generated at:
```
jablib/build/reports/tests/index.html
```
The report shows detailed results, including any passing or failing test cases.
---
