package com.galapea.techblog.bookinventory.service;

import com.galapea.techblog.bookinventory.domain.Book;
import com.galapea.techblog.bookinventory.domain.BookAIReply;
import com.galapea.techblog.bookinventory.domain.BookContainer;
import com.github.f4b6a3.tsid.TsidCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    private void createBook(Book book) {
        String id = book.id();
        if (id == null || id.isEmpty()) {
            id = nextId();
            book = new Book(id, book.title(), book.authors(), book.publisher(), book.rating(), book.genres(),
                    book.summary(), book.goodreadsBookId());
        }
        if (bookStore.containsKey(id)) {
            throw new IllegalArgumentException("Book with ID " + id + " already exists.");
        }
        bookStore.put(id, book);
    }

    public List<Book> listBooks() {
        return this.bookContainer.getBooks();
    }

    public Book getBook(String id) {
        Book book = this.bookContainer.getBook(id);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + id + " does not exist.");
        }
        return book;
    }

    private void updateBook(String id, Book updatedBook) {
        if (!bookStore.containsKey(id)) {
            throw new IllegalArgumentException("Book with ID " + id + " does not exist.");
        }
        bookStore.put(id, updatedBook);
    }

    private void updateBookSummary(String id, String summary) {
        Book book = bookStore.get(id);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + id + " does not exist.");
        }
        log.info("Updating summary for book with ID {}: {}", id, summary);
    }

    public void generateSummary(String bookId) {
        Book book = getBook(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }

        BookAIReply reply = bookAssistant.findBookSummary(book.title(), book.authors());
        String summary = reply.value();
        if (summary == null || summary.isEmpty()) {
            throw new IllegalArgumentException("Failed to generate summary for book with ID " + bookId);
        }
        Book updated = new Book(book.id(), book.title(), book.authors(), book.publisher(), book.rating(), book.genres(),
                summary, book.goodreadsBookId(), reply.sourceUrl());
        saveBooks(List.of(updated));
    }

    public void generateGenre(String bookId) {
        Book book = getBook(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }

        BookAIReply reply = bookAssistant.findBookGenre(book.title(), book.authors());
        String genres = reply.value();
        log.info("Fetched genre for book with ID {}: {}", bookId, genres);
        Book updated = new Book(book.id(), book.title(), book.authors(), book.publisher(), book.rating(), genres,
                book.summary(), book.goodreadsBookId(), book.goodreadsUrl());
        saveBooks(List.of(updated));
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
        if (books == null || books.isEmpty()) {
            log.warn("No books to save.");
            return;
        }
        List<Book> newBooks = books.stream().map(book -> {
            String id = (book.id() != null) ? book.id() : nextId();
            return new Book(id, book.title(), book.authors(), book.publisher(), book.rating(), book.genres(),
                    book.summary(), book.goodreadsBookId(), book.goodreadsUrl());
        }).collect(Collectors.toList());
        this.bookContainer.saveBooks(newBooks);
    }

    public static String nextId() {
        return "book_" + TsidCreator.getTsid().format("%S");
    }
}
