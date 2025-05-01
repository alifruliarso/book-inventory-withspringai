package com.galapea.techblog.bookinventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import com.galapea.techblog.bookinventory.domain.BookAIReply;

/**
 * Service for interacting with AI to retrieve book-related information such as
 * genre and summary.
 * <p>
 * This class uses a <a href=
 * "https://docs.spring.io/spring-ai/reference/api/chatclient.html">ChatClient</a>
 * to send prompts to an AI model (e.g., OpenAI) and receive structured answers.
 * </p>
 */
@Service
public class BookAssistant {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ChatClient chatClient;

    /**
     * Constructs a BookAssistant with the given ChatClient.
     *
     * @param chatClient
     *            the chat client used to communicate with the AI service
     */
    public BookAssistant(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Finds the genre of a book using the AI model.
     *
     * @param title
     *            the title of the book
     * @param authors
     *            the authors of the book
     * @return a BookAIReply containing the genre and source URL
     */
    public BookAIReply findBookGenre(String title, String authors) {
        log.info("Requesting OpenAI for book genre: {}, {}", title, authors);
        BookAIReply reply = chatClient.prompt()
                .user(user -> user.text("What is the genre of the book {title} by {authors}. Provide the source url.")
                        .param("title", title).param("authors", authors))
                .call().entity(BookAIReply.class);
        log.info("Received OpenAI response: {}", reply);
        return reply;
    }

    /**
     * Finds the summary of a book using the AI model.
     *
     * @param title
     *            the title of the book
     * @param authors
     *            the authors of the book
     * @return a BookAIReply containing the summary and source URL
     */
    public BookAIReply findBookSummary(String title, String authors) {
        log.info("Requesting OpenAI for book summary: {}, {}", title, authors);
        BookAIReply reply = chatClient.prompt()
                .user(user -> user.text("What is the summary of the book {title} by {authors}. Provide the source url.")
                        .param("title", title).param("authors", authors))
                .call().entity(BookAIReply.class);
        log.info("Received OpenAI response: {}", reply);
        return reply;
    }

}
