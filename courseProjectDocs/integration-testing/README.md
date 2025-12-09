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
./gradlew :jablib:test --tests "org.jabref.integration.FormAuthStudyIntegrationTest"
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

---

## Author and Forms Integration Test

#### Summary:
Integration test for Author and Formatter modules.
Tests the interaction between author and StudyCatalog logic.

#### Modules Integrated
- `org.jabref.logic.cleanup.Formatter` — manages Entry Forms logic
- `org.jabref.model.entry.Author` — manages author of entry form
- `org.jabref.model.study.StudyCatalog` - model view for GUI StudyCatalog
#### Test Data Prep:
One author entry created and formatter applied function to remove special characters then study catalog created based off that Author made to verfy it went into a Library object.

#### Execution :
```bash
.\gradlew :jablib:test --tests "org.jabref.integration.FormAuthStudyIntegrationTest"
```

### Viewing the Results
After running the tests, open the report generated at:
```
jablib/build/reports/tests/index.html
```
The report shows detailed results, including any passing or failing test cases.
---
