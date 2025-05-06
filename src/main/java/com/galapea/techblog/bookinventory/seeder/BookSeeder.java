package com.galapea.techblog.bookinventory.seeder;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
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
        bookService.createTableBooks();
        if (bookService.listBooks().isEmpty()) {
            log.info("Seeding books...");
            InputStream is = getClass().getClassLoader().getResourceAsStream("goodreads-datasets-book1-100k.csv");
            List<Book> parsedBooks = new GoodReadBookCSVParser().parseBooksFromCsv(is);
            saveBooks(parsedBooks);
        } else {
            log.info("Books already seeded.");
        }
    }

    private void saveBooks(List<Book> books) {
        int count = 0;
        Instant start = Instant.now();

        List<List<Book>> chunks = splitIntoChunks(books, 10);
        for (List<Book> chunk : chunks) {
            count += chunk.size();
            try {
                bookService.saveBooks(chunk);
            } catch (Exception e) {
                log.warn("Failed to save chunk of books: {}", e.getMessage());
            }
        }
        Instant end = Instant.now();
        log.info("Seeding completed in {} seconds", (end.toEpochMilli() - start.toEpochMilli()) / 1000.0);
        log.info("Seeding completed. Total books seeded: {}", count);
    }

    public static <T> List<List<T>> splitIntoChunks(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            int end = Math.min(list.size(), i + chunkSize);
            chunks.add(list.subList(i, end));
        }
        return chunks;
    }
}
