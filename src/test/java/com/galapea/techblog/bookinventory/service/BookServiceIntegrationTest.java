package com.galapea.techblog.bookinventory.service;

import com.galapea.techblog.bookinventory.domain.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceIntegrationTest {
    private BookService bookService;
    private BookAssistant bookAssistant;

    @BeforeEach
    void setUp() {
        bookAssistant = Mockito.mock(BookAssistant.class);
        bookService = new BookService(bookAssistant);
    }

    @Test
    void testCreateAndListBooks() {
        Book book = new Book("1", "Title", "Author", "Publisher", 4.5, "Genre", "Summary", 123L);
        bookService.createBook(book);
        List<Book> books = bookService.listBooks();
        assertEquals(1, books.size());
        assertEquals(book, books.get(0));
    }

    @Test
    void testCreateBookWithDuplicateIdThrowsException() {
        Book book = new Book("1", "Title", "Author", "Publisher", 4.5, "Genre", "Summary", 123L);
        bookService.createBook(book);
        Book duplicate = new Book("1", "Another Title", "Another Author", "Another Publisher", 3.0, "Another Genre",
                "Another Summary", 456L);
        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(duplicate));
    }

    @Test
    void testUpdateBook() {
        Book book = new Book("1", "Title", "Author", "Publisher", 4.5, "Genre", "Summary", 123L);
        bookService.createBook(book);
        Book updated = new Book("1", "Updated Title", "Updated Author", "Updated Publisher", 5.0, "Updated Genre",
                "Updated Summary", 789L);
        bookService.updateBook("1", updated);
        List<Book> books = bookService.listBooks();
        assertEquals(1, books.size());
        assertEquals(updated, books.get(0));
    }

    @Test
    void testUpdateNonExistentBookThrowsException() {
        Book updated = new Book("2", "Updated Title", "Updated Author", "Updated Publisher", 5.0, "Updated Genre",
                "Updated Summary", 789L);
        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook("2", updated));
    }
}
