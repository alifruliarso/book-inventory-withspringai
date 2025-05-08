package com.galapea.techblog.bookinventory.domain;

public record Book(String id, String title, String authors, String publisher, Double rating, String genres,
        String summary, Long goodreadsBookId, String goodreadsUrl) {
    public Book(String id, String title, String authors, String publisher, Double rating, String genres, String summary,
            Long goodreadsBookId) {
        this(id, title, authors, publisher, rating, genres, summary, goodreadsBookId, null);
    }
}
