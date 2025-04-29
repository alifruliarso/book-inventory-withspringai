package com.galapea.techblog.bookinventory.service;

import com.galapea.techblog.bookinventory.domain.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.UUID;

@Service
public class BookService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ConcurrentHashMap<String, Book> bookStore = new ConcurrentHashMap<>();

    public void createBook(Book book) {
        String id = book.id();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            book = new Book(id, book.title(), book.authors(), book.publisher(), book.rating(), book.genres(),
                    book.summary(), book.goodreadsBookId());
        }
        if (bookStore.containsKey(id)) {
            throw new IllegalArgumentException("Book with ID " + id + " already exists.");
        }
        bookStore.put(id, book);
    }

    public List<Book> listBooks() {
        return new ArrayList<>(bookStore.values());
    }

    public Book getBook(String id) {
        Book book = bookStore.get(id);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + id + " does not exist.");
        }
        return book;
    }

    public void updateBook(String id, Book updatedBook) {
        if (!bookStore.containsKey(id)) {
            throw new IllegalArgumentException("Book with ID " + id + " does not exist.");
        }
        bookStore.put(id, updatedBook);
    }

    public void updateBookSummary(String id, String summary) {
        Book book = bookStore.get(id);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + id + " does not exist.");
        }
        log.info("Updating summary for book with ID {}: {}", id, summary);
    }

    public void generateSummary(String bookId) {
        Book book = bookStore.get(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }

        String summary = "[TEST] Summary for '" + book.title() + "' by " + book.authors();
        Book updated = new Book(book.id(), book.title(), book.authors(), book.publisher(), book.rating(), book.genres(),
                summary, book.goodreadsBookId());
        bookStore.put(bookId, updated);
    }

    public void fetchGenre(String bookId) {
        Book book = bookStore.get(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }

        String genres = "[TEST] Genre for '" + book.title() + "' by " + book.authors();
        Book updated = new Book(book.id(), book.title(), book.authors(), book.publisher(), book.rating(), genres,
                book.summary(), book.goodreadsBookId());
        bookStore.put(bookId, updated);
        try {
            Thread.sleep(Duration.ofSeconds(15).toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Fetched genre for book with ID {}: {}", bookId, genres);
    }

    @Async
    public void asyncFetchGenre(String bookId, Consumer<String> onComplete, Consumer<Double> onProgress,
            Consumer<Exception> onError) {
        try {
            onProgress.accept(0.5);
            fetchGenre(bookId);
            onProgress.accept(1.0);
            onComplete.accept(bookId);
        } catch (Exception e) {
            onError.accept(e);
        }
    }
}
