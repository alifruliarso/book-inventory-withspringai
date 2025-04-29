package com.galapea.techblog.bookinventory.seeder;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.galapea.techblog.bookinventory.domain.Book;
import com.galapea.techblog.bookinventory.service.BookService;

@Component
public class BookSeeder implements CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BookService bookService;

    public BookSeeder(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void run(String... args) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("goodreads-datasets-book1-100k.csv");
        List<Book> books = new GoodReadBookCSVParser().parseBooksFromCsv(is);
        saveBooks(books);
    }

    private void saveBooks(List<Book> books) {
        int count = 0;
        Instant start = Instant.now();
        for (Book book : books) {
            try {
                bookService.createBook(book);
            } catch (Exception e) {
                log.warn("Failed to create book with goodreadsBookId {}: {}", book.goodreadsBookId(), e.getMessage());
            }
            count++;
            if (count % 500 == 0) {
                log.info("Seeded {} books...", count);
            }
        }
        Instant end = Instant.now();
        log.info("Seeding completed in {} seconds", (end.toEpochMilli() - start.toEpochMilli()) / 1000.0);
        log.info("Seeding completed. Total books seeded: {}", count);
    }
}
