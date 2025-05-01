package com.galapea.techblog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Interceptor to log requests and responses to/from the AI service.
 * https://bootcamptoprod.com/spring-ai-log-model-requests-and-responses/
 */
public class AIRequestLogInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger("AI-Communication-Logger");

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        // Log what we're sending to the AI service
        logAIRequest(request, body);

        // Make the actual call to the AI service
        ClientHttpResponse response = execution.execute(request, body);

        // Create a wrapper that lets us read the response twice
        BufferedResponseWrapper responseWrapper = new BufferedResponseWrapper(response);

        // Log what the AI service sent back
        logAIResponse(responseWrapper);

        return responseWrapper;
    }

    private void logAIRequest(HttpRequest request, byte[] body) {
        logger.info("➡️ AI REQUEST: {} {}", request.getMethod(), request.getURI());
        logger.info("📋 HEADERS: {}", request.getHeaders());

        String bodyText = new String(body, StandardCharsets.UTF_8);
        logger.info("📤 BODY: {}", bodyText);
    }

    private void logAIResponse(ClientHttpResponse response) throws IOException {
        logger.info("⬅️ AI RESPONSE STATUS: {} {}", response.getStatusCode(), response.getStatusText());
        logger.info("📋 HEADERS: {}", response.getHeaders());

        String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        logger.info("📥 BODY: {}", responseBody.trim());
    }

    // This wrapper class allows us to read the response body twice
    private static class BufferedResponseWrapper implements ClientHttpResponse {
        private final ClientHttpResponse original;
        private final byte[] body;

        public BufferedResponseWrapper(ClientHttpResponse response) throws IOException {
            this.original = response;
            this.body = StreamUtils.copyToByteArray(response.getBody());
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return original.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return original.getStatusText();
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return original.getHeaders();
        }

        @Override
        public void close() {
            original.close();
        }
    }
}
