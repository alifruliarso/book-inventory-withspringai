package com.galapea.techblog.bookinventory.domain;

/**
 * Represents a reply generated by an AI model for a book-related query.
 * <p>
 * The {@code value} field contains the answer from the AI model. The
 * {@code sourceUrl} field contains links to the source URLs referenced by the
 * AI in its answer.
 * </p>
 */
public record BookAIReply(String value, String sourceUrl) {
    /**
     * Constructs a BookAIReply with only a value. The sourceUrl will be set to
     * null.
     *
     * @param value
     *            the answer from the AI model
     */
    public BookAIReply(String value) {
        this(value, null);
    }

    /**
     * Constructs a BookAIReply with a value and a sourceUrl.
     *
     * @param value
     *            the answer from the AI model
     * @param sourceUrl
     *            links to the source URLs referenced by the AI
     */
    public BookAIReply(String value, String sourceUrl) {
        this.value = value;
        this.sourceUrl = sourceUrl;
    }
}
