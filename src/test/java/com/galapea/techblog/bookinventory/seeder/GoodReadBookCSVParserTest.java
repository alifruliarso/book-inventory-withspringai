package com.galapea.techblog.bookinventory.seeder;

import com.galapea.techblog.bookinventory.domain.Book;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoodReadBookCSVParserTest {
    @Test
    void testParseBooksFromCsv_withRealCsvFile() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("goodreads-datasets-book1-100k.csv");
        assertNotNull(is, "CSV file should be found in test resources");
        GoodReadBookCSVParser parser = new GoodReadBookCSVParser();
        List<Book> books = parser.parseBooksFromCsv(is);
        assertFalse(books.isEmpty(), "Parsed books list should not be empty");
        Book firstBook = books.get(0);
        assertNotNull(firstBook.title(), "Book title should not be null");
        assertNotNull(firstBook.authors(), "Book authors should not be null");
        assertNull(firstBook.genres(), "Book genres should be null");
        assertNull(firstBook.summary(), "Book summary should be null");
        assertNotNull(firstBook.goodreadsBookId(), "Book goodreadsBookId should not be null");
        assertEquals("Harry Potter and the Half-Blood Prince (Harry Potter, #6)", firstBook.title());
        assertEquals(firstBook.publisher(), "Scholastic Inc.");
        assertEquals(4.57, firstBook.rating(), 0.01, "Book rating should be approximately 4.57");
        assertEquals("J.K. Rowling", firstBook.authors(), "Book authors should be 'J.K. Rowling'");
    }
}
