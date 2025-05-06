package com.galapea.techblog.base.griddb;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

public class GridDbCloudClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RestClient restClient;

    public GridDbCloudClient(String baseUrl, String authToken) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).defaultHeader("Authorization", "Basic " + authToken)
                .defaultHeader("Content-Type", "application/json").defaultHeader("Accept", "application/json")
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    log.error("GridDBCloud API Error HTTP status text: {}", response.getStatusText());
                    // String errorBody = new String(response.getBody().readAllBytes());
                    String errorBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                    String message = String.format("%s - %s", response.getStatusCode().value(), errorBody);
                    log.error("GridDBCloud API Error: {}", message);
                    throw new GridDbException("GridDBCloud API request failed", response.getStatusCode(), errorBody);
                }).requestInterceptor((request, body, execution) -> {
                    final long begin = System.currentTimeMillis();
                    ClientHttpResponse response = execution.execute(request, body);
                    long duration = System.currentTimeMillis() - begin;
                    log.info("[HttpRequestInterceptor] {} {} {} Duration: {}s", response.getStatusCode().value(),
                            request.getMethod(), request.getURI(), TimeUnit.MILLISECONDS.toSeconds(duration));
                    log.info("[HttpRequestInterceptor] Headers: {}", request.getHeaders());
                    if (body != null && body.length > 0) {
                        log.info("[HttpRequestInterceptor] Body: {}", new String(body, StandardCharsets.UTF_8));
                    }
                    return response;
                }).build();
    }

    public void createContainer(GridDbContainerDefinition containerDefinition) {
        try {
            restClient.post().uri("/containers").body(containerDefinition).retrieve().toBodilessEntity();
        } catch (GridDbException e) {
            if (e.getStatusCode().value() == 409) {
                return;
            }
            throw e;
        } catch (Exception e) {
            throw new GridDbException("Failed to create container", HttpStatusCode.valueOf(500), e.getMessage(), e);
        }
    }

    public void post(String uri, Object body) {
        try {
            restClient.post().uri(uri).body(body).retrieve().toBodilessEntity();
        } catch (GridDbException e) {
            throw e;
        } catch (Exception e) {
            throw new GridDbException("Failed to execute POST request", HttpStatusCode.valueOf(500), e.getMessage(), e);
        }
    }

    /**
     * Registers rows of data into a specified GridDB container. For more details,
     * refer to the <a href=
     * "https://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v5_7/GridDB_Web_API_Reference.html#row-registration-in-a-single-container">GridDB
     * Web API Reference</a>
     *
     * @param containerName
     *            The name of the container where rows will be registered
     * @param body
     *            The data to be registered in the container
     * @throws GridDbException
     *             If there's an error during the registration process with GridDB
     *             or if the REST request fails
     */
    public void registerRows(String containerName, Object body) {
        try {
            ResponseEntity<String> result = restClient.put().uri("/containers/" + containerName + "/rows").body(body)
                    .retrieve().toEntity(String.class);
            log.info("Register row response:{}", result);
        } catch (GridDbException e) {
            throw e;
        } catch (Exception e) {
            throw new GridDbException("Failed to execute PUT request", HttpStatusCode.valueOf(500), e.getMessage(), e);
        }
    }
}
