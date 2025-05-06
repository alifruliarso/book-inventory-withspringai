package com.galapea.techblog.bookinventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galapea.techblog.base.griddb.GridDbCloudClient;
import com.galapea.techblog.base.griddb.GridDbCloudSQLInsert;
import com.galapea.techblog.base.griddb.GridDbColumn;
import com.galapea.techblog.base.griddb.GridDbContainerDefinition;
import com.galapea.techblog.base.griddb.GridDbException;
import com.galapea.techblog.base.griddb.acquisition.AcquireRowsRequest;
import com.galapea.techblog.base.griddb.acquisition.AcquireRowsResponse;

@Service
public class BookContainer {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final GridDbCloudClient gridDbCloudClient;
    private static final String BOOKS_TBL_NAME = "Books";

    @Autowired
    public ObjectMapper objectMapper;

    public BookContainer(GridDbCloudClient gridDbCloudClient) {
        this.gridDbCloudClient = gridDbCloudClient;
    }

    /**
     * Creates a "Books" table in GridDB with predefined schema.
     *
     * This method defines the structure of the Books table with columns for book
     * details including id, title, authors, publisher, rating, genres, summary, and
     * goodreadsBookId. The 'id' column is set as the primary key with TREE
     * indexing.
     *
     * After defining the schema, it creates the container in GridDB using the
     * GridDbCloudClient.
     *
     * @throws GridDbException
     *             If there's an error during container creation in GridDB
     */
    public void createTableBooks() {
        log.info("Creating table Books in GridDB...");
        List<GridDbColumn> columns = List.of(new GridDbColumn("id", "STRING", Set.of("TREE")),
                new GridDbColumn("title", "STRING"), new GridDbColumn("authors", "STRING"),
                new GridDbColumn("publisher", "STRING"), new GridDbColumn("rating", "DOUBLE"),
                new GridDbColumn("genres", "STRING"), new GridDbColumn("summary", "STRING"),
                new GridDbColumn("goodreadsBookId", "LONG"));

        GridDbContainerDefinition containerDefinition = GridDbContainerDefinition.createContainer(BOOKS_TBL_NAME,
                columns);
        this.gridDbCloudClient.createContainer(containerDefinition);
        log.info("Created table Books with columns: {}", columns);
    }

    private void post(String uri, Object body) {
        try {
            this.gridDbCloudClient.post(uri, body);
        } catch (GridDbException e) {
            throw e;
        } catch (Exception e) {
            throw new GridDbException("Failed to execute POST request", HttpStatusCode.valueOf(500), e.getMessage(), e);
        }
    }

    public void insert(Book book) {
        String stmt = "INSERT INTO " + BOOKS_TBL_NAME + "(id, title, authors, genres) VALUES ('" + book.id() + "', '"
                + book.title() + "', '" + book.authors() + "', '" + book.genres() + "')";
        GridDbCloudSQLInsert insert = new GridDbCloudSQLInsert(stmt);

        post("/sql/update", List.of(insert));
    }

    /**
     * Saves a list of books to the GridDB database. This method converts a list of
     * Book objects into a JSON-like string representation and uses the GridDB
     * client to register them in the Books table. Request body example:
     * 
     * <pre>
     * [
     *   ["abf8e412", "The Ultimate Hitchhiker's Guide to the Galaxy", "Douglas Adams", "Del Rey Books", 4.37, "", "", 13],
     *   ["5f8bdef1", "The Lost Continent: Travels in Small Town America", "Bill Bryson", "William Morrow Paperbacks", 3.83, "", "", 26]
     * ]
     * </pre>
     *
     * @param books
     *            The list of Book objects to save to the database
     */
    public void saveBooks(List<Book> books) {
        // Initialize a StringBuilder to create the JSON-like array representation
        StringBuilder sb = new StringBuilder();
        sb.append("["); // Start the outer array
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            sb.append("["); // Start the inner array (each book)
            // Append each book field in order
            sb.append("\"").append(book.id()).append("\"");
            sb.append(", ");
            sb.append("\"").append(book.title()).append("\"");
            sb.append(", ");
            sb.append("\"").append(book.authors()).append("\"");
            sb.append(", ");
            sb.append("\"").append(book.publisher()).append("\"");
            sb.append(", ");
            sb.append(book.rating()); // Numeric value, no quotes
            sb.append(", ");
            // Handle potentially null genres field
            sb.append("\"").append(book.genres() != null ? book.genres() : "").append("\"");
            sb.append(", ");
            // Handle potentially null summary field
            sb.append("\"").append(book.summary() != null ? book.summary() : "").append("\"");
            sb.append(", ");
            sb.append(book.goodreadsBookId()); // Numeric value, no quotes
            sb.append("]"); // End the inner array
            // Add a comma between books, except for the last one
            if (i < books.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]"); // End the outer array
        String result = sb.toString();
        log.info("Books array: {}", result);
        this.gridDbCloudClient.registerRows(BOOKS_TBL_NAME, result);
    }

    public List<Book> getBooks() {
        AcquireRowsRequest requestBody = AcquireRowsRequest.builder().limit(50L).build();
        AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(BOOKS_TBL_NAME, requestBody);
        if (response == null || response.getRows() == null) {
            log.error("Failed to acquire rows from GridDB");
            return List.of();
        }
        List<Book> books = convertResponseToBook(response);
        log.info("Fetched {} books from GridDB", books.size());
        return books;
    }

    public Book getBook(String bookId) {
        AcquireRowsRequest requestBody = AcquireRowsRequest.builder().limit(1L).condition("id == \'" + bookId + "\'")
                .build();
        AcquireRowsResponse response = this.gridDbCloudClient.acquireRows(BOOKS_TBL_NAME, requestBody);
        if (response == null || response.getRows() == null) {
            log.error("Failed to acquire rows from GridDB");
            return null;
        }
        List<Book> books = convertResponseToBook(response);
        log.info("Fetched {} books from GridDB", books.size());
        return books.stream().filter(b -> b.id().equals(bookId)).findFirst().orElse(null);
    }

    private List<Book> convertResponseToBook(AcquireRowsResponse response) {
        List<Book> books = response.getRows().stream().map(row -> {
            try {
                var book = new Book(row.get(0).toString(), row.get(1).toString(), row.get(2).toString(),
                        row.get(3).toString(),
                        Optional.ofNullable(row.get(4)).map(Object::toString).map(Double::valueOf).orElse(null),
                        Optional.ofNullable(row.get(5)).map(Object::toString).orElse(null),
                        Optional.ofNullable(row.get(6)).map(Object::toString).orElse(null),
                        Optional.ofNullable(row.get(7)).map(Object::toString).map(Long::valueOf).orElse(null));
                return book;
            } catch (Exception e) {
                log.error("Error parsing book row: {}. Error: {}", row.toString(), e.getMessage());
                return null;
            }
        }).filter(book -> book != null).toList();
        return books;
    }

}
