package org.jabref.integration;

import org.jabref.logic.database.DuplicateCheck;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibEntryTypesManager;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.jabref.model.database.BibDatabaseMode.BIBTEX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DuplicateCheckIntegrationTest {

    private BibDatabase database;
    private DuplicateCheck duplicateCheck;

    @BeforeEach
    public void setUp() {
        database = new BibDatabase();
        BibEntryTypesManager entryTypesManager = new BibEntryTypesManager();
        duplicateCheck = new DuplicateCheck(entryTypesManager);
    }

    @Test
    public void testDuplicateDetection_identicalEntries() {
        BibEntry entry1 = new BibEntry(StandardEntryType.Article);
        entry1.setField(StandardField.TITLE, "Machine Learning in Software Engineering");
        entry1.setField(StandardField.AUTHOR, "Smith, John and Doe, Jane");
        entry1.setField(StandardField.YEAR, "2023");
        entry1.setField(StandardField.JOURNAL, "IEEE Transactions on Software Engineering");

        BibEntry entry2 = new BibEntry(StandardEntryType.Article);
        entry2.setField(StandardField.TITLE, "Machine Learning in Software Engineering");
        entry2.setField(StandardField.AUTHOR, "Smith, John and Doe, Jane");
        entry2.setField(StandardField.YEAR, "2023");
        entry2.setField(StandardField.JOURNAL, "IEEE Transactions on Software Engineering");

        database.insertEntry(entry1);
        database.insertEntry(entry2);

        assertEquals(2, database.getEntryCount(), "Database should contain both entries");

        boolean isDuplicate = duplicateCheck.isDuplicate(entry1, entry2, BIBTEX);
        assertTrue(isDuplicate, "Entries should be detected as duplicates");
    }

    @Test
    public void testDuplicateDetection_similarButNotDuplicate() {
        BibEntry entry1 = new BibEntry(StandardEntryType.Article);
        entry1.setField(StandardField.TITLE, "Deep Learning Approaches");
        entry1.setField(StandardField.AUTHOR, "Brown, Alice");
        entry1.setField(StandardField.YEAR, "2022");
        entry1.setField(StandardField.JOURNAL, "ACM Computing Surveys");

        BibEntry entry2 = new BibEntry(StandardEntryType.Article);
        entry2.setField(StandardField.TITLE, "Deep Learning Approaches");
        entry2.setField(StandardField.AUTHOR, "Brown, Alice");
        entry2.setField(StandardField.YEAR, "2024");
        entry2.setField(StandardField.JOURNAL, "ACM Computing Surveys");

        // Add entries to a database
        database.insertEntry(entry1);
        database.insertEntry(entry2);

        // Verify database contains both entries
        assertEquals(2, database.getEntryCount());

        // Verify they are not detected as duplicates
        boolean isDuplicate = duplicateCheck.isDuplicate(entry1, entry2, BIBTEX);
        assertFalse(isDuplicate, "Entries with different years should not be duplicates");
    }

    @Test
    public void testDuplicateDetection_differentEntries() {
        BibEntry entry1 = new BibEntry(StandardEntryType.Book);
        entry1.setField(StandardField.TITLE, "Introduction to Algorithms");
        entry1.setField(StandardField.AUTHOR, "Cormen, Thomas H.");
        entry1.setField(StandardField.YEAR, "2009");
        entry1.setField(StandardField.PUBLISHER, "MIT Press");

        BibEntry entry2 = new BibEntry(StandardEntryType.Article);
        entry2.setField(StandardField.TITLE, "Quantum Computing Fundamentals");
        entry2.setField(StandardField.AUTHOR, "Johnson, Emily");
        entry2.setField(StandardField.YEAR, "2023");
        entry2.setField(StandardField.JOURNAL, "Nature Physics");

        database.insertEntry(entry1);
        database.insertEntry(entry2);

        assertEquals(2, database.getEntryCount());
        assertTrue(database.getEntries().contains(entry1));
        assertTrue(database.getEntries().contains(entry2));

        // Verify they are not duplicates
        boolean isDuplicate = duplicateCheck.isDuplicate(entry1, entry2, BIBTEX);
        assertFalse(isDuplicate, "Completely different entries should not be duplicates");
    }
}
