package com.galapea.techblog.bookinventory.seeder;

import com.galapea.techblog.bookinventory.domain.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GoodReadBookCSVParser {
    private static final Logger log = LoggerFactory.getLogger(GoodReadBookCSVParser.class);

    public List<Book> parseBooksFromCsv(InputStream is) throws Exception {
        List<Book> books = new ArrayList<>();
        if (is == null) {
            log.error("CSV file not found in resources");
            return books;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirst = true;
            while ((line = reader.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
                // Use a CSV parser to handle quoted fields and commas
                String[] fields = parseCsvLine(line);
                if (fields.length < 18)
                    continue;
                String title = fields[1]; // 2nd column, should match quoted value
                String authors = fields[12];
                String publisher = fields[8];
                Double rating = null;
                try {
                    rating = fields[13].isEmpty() ? null : Double.parseDouble(fields[13]);
                } catch (NumberFormatException e) {
                }
                Long goodreadsBookId = null;
                try {
                    goodreadsBookId = fields[0].isEmpty() ? null : Long.parseLong(fields[0]);
                } catch (NumberFormatException e) {
                }
                Book book = new Book(null, title, authors, publisher, rating, null, null, goodreadsBookId);
                books.add(book);
                if (books.size() % 55 == 0) {
                    log.info("Parsed {} books...", books.size());
                    break;
                }
            }
        }
        return books;
    }

    // Simple CSV parser for quoted fields
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }
}
