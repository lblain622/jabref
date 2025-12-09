# Integration Testing Report

## Test Design Summary
### PdfImporter & ParserResult Integration Test:
This integration test verifies the interaction between the **PDF Importer** (`PdfImporter`) module and the **Parser Result** (`ParserResult`) module in the JabRef system.
The goal is to ensure that when a PDF file is imported, the extracted bibliographic data is correctly propagated into the internal database representation.

#### Modules Integrated
- `org.jabref.logic.importer.fileformat.pdf` — handles PDF import logic.
- `org.jabref.logic.importer` — manages parsing results and database contexts.

#### Test Scenario
A simulated PDF import was performed using a mocked `PdfImporter` to produce a bibliographic entry.  
The resulting `ParserResult` was validated to ensure correct data flow and entry creation.

#### Test Data Preparation
A dummy PDF path (`build/resources/test/pdfs/test.pdf`) was used. No actual parsing of a file occurs; the test simulates integration logic at the module interface level.

#### Execution
Executed using:
```bash
.\gradlew :jablib:test --tests "org.jabref.integration.PdfImporterIntegrationTest"
```

#### Results:
- Test passed successfully.
- Verified that ParserResult correctly stores the entry returned from PdfImporter.
- No defects were discovered.

#### Bug reports
No new defects were identified during integration testing.


## Database Duplication Integration Test

#### Summary:
Integration test for BibDatabase and DuplicateCheck modules. Tests the interaction between database storage and duplicate detection logic.
The goal of this test is to verify that the database is correctly updated when a duplicate entry is detected.

#### Modules Integrated
- `org.jabref.model.database` — bibliography database logic
- `org.jabref.logic.database` — manages duplication detection logic for entries in the database.
#### Test Data Prep:
 Two BibEntries Objects were created with identical or near identical fields and then inserted into the database
#### Execution :
```bash
.\gradlew :jablib:test --tests "org.jabref.integration.DuplicateCheckIntegrationTest"
```
#### Results:
- 2 Tests passed successfully.
- 1 Failed testDuplicateDetection_similarButNotDuplicate
#### Bug Report:
 1. When two entries have the same name,author, but a different year, the duplicate check module detects them as duplicates.

## Group contributions
| Member   | Task/Contribution                                     | Notes                                                         |
|----------|-------------------------------------------------------|---------------------------------------------------------------|
| Geoffrey | Designed and implemented `PdfImporterIntegrationTest` | Verified data flow between importer and parser result modules |
| Lucille  | Designed and implemented  `DuplicateCheckIntegrationTest`                             | N/A                                                           |
| Vanessa  | X                                                     | X                                                             |
