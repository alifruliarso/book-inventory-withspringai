package com.galapea.techblog.bookinventory.service;

import com.galapea.techblog.bookinventory.domain.Book;
import com.galapea.techblog.bookinventory.domain.BookContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceIntegrationTest {
    private BookService bookService;
    private BookAssistant bookAssistant;
    private BookContainer bookContainer;

    @BeforeEach
    void setUp() {
        bookAssistant = Mockito.mock(BookAssistant.class);
        bookContainer = Mockito.mock(BookContainer.class);
        bookService = new BookService(bookAssistant, bookContainer);
    }

    @Test
    void testSaveBooksCallsBookContainerWithCorrectData() {
        BookContainer mockBookContainer = mock(BookContainer.class);
        BookAssistant mockBookAssistant = mock(BookAssistant.class);
        BookService bookService = new BookService(mockBookAssistant, mockBookContainer);
        String bookId = "bookIDXXJXJXJ";
        List<Book> books = List.of(new Book(null, "Title1", "Author1", "Publisher1", 4.5, "Genre1", "Summary1", 123L),
                new Book(bookId, "Title2", "Author2", "Publisher2", 3.2, "Genre2", "Summary2", 456L));
        bookService.saveBooks(books);
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<Book>> bookListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockBookContainer, times(1)).saveBooks(bookListArgumentCaptor.capture());
        List<Book> insertedBooks = bookListArgumentCaptor.getAllValues().get(0);
        assertEquals(2, insertedBooks.size());
        for (Book b : insertedBooks) {
            assertNotNull(b.id());
            assertTrue(b.id().length() > 0);
        }
        assertEquals("Title1", insertedBooks.get(0).title());
        assertEquals("Title2", insertedBooks.get(1).title());
        assertEquals(bookId, insertedBooks.get(1).id());
    }
}
