package com.galapea.techblog.bookinventory.service;

import com.galapea.techblog.bookinventory.domain.Book;
import com.galapea.techblog.bookinventory.domain.BookAIReply;
import com.galapea.techblog.bookinventory.domain.BookContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class BookService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ConcurrentHashMap<String, Book> bookStore = new ConcurrentHashMap<>();
    private final BookAssistant bookAssistant;
    private final BookContainer bookContainer;

    public BookService(BookAssistant bookAssistant, BookContainer bookContainer) {
        this.bookAssistant = bookAssistant;
        this.bookContainer = bookContainer;
    }

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
        bookContainer.insert(book);
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

        BookAIReply reply = bookAssistant.findBookSummary(book.title(), book.authors());
        String summary = reply.value();
        if (summary == null || summary.isEmpty()) {
            throw new IllegalArgumentException("Failed to generate summary for book with ID " + bookId);
        }
        Book updated = new Book(book.id(), book.title(), book.authors(), book.publisher(), book.rating(), book.genres(),
                summary, book.goodreadsBookId());
        bookStore.put(bookId, updated);
    }

    public void generateGenre(String bookId) {
        Book book = bookStore.get(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }

        BookAIReply reply = bookAssistant.findBookGenre(book.title(), book.authors());
        String genres = reply.value();
        Book updated = new Book(book.id(), book.title(), book.authors(), book.publisher(), book.rating(), genres,
                book.summary(), book.goodreadsBookId());
        bookStore.put(bookId, updated);
        log.info("Fetched genre for book with ID {}: {}", bookId, genres);
    }

    @Async
    public void asyncGenerateGenre(String bookId, Consumer<String> onComplete, Consumer<Double> onProgress,
            Consumer<Exception> onError) {
        try {
            onProgress.accept(0.5);
            generateGenre(bookId);
            onProgress.accept(1.0);
            onComplete.accept(bookId);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Async
    public void asyncGenerateSummary(String bookId, Consumer<String> onComplete, Consumer<Double> onProgress,
            Consumer<Exception> onError) {
        try {
            onProgress.accept(0.5);
            generateSummary(bookId);
            onProgress.accept(1.0);
            onComplete.accept(bookId);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    public void createTableBooks() {
        this.bookContainer.createTableBooks();
    }

    public void saveBooks(List<Book> books) {
        List<Book> newBooks = books.stream().map(book -> {
            String id = (book.id() != null) ? book.id() : UUID.randomUUID().toString();
            return new Book(id, book.title(), book.authors(), book.publisher(), book.rating(), book.genres(),
                    book.summary(), book.goodreadsBookId());
        }).collect(Collectors.toList());
        this.bookContainer.saveBooks(newBooks);
    }
}
