package com.tick_tracker.client;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class TickApiClient {

    private static final Logger log = LoggerFactory.getLogger(TickApiClient.class);
    private static final int MAX_ATTEMPTS = 4;
    private final RestClient restClient;
    private final long firstDelayMs;

    public TickApiClient(RestClient restClient, @Value("${tick.api.retry-delay-ms}") long firstDelayMs) {
        this.restClient = restClient;
        this.firstDelayMs = firstDelayMs;
    }

    public List<SightingApiDto> fetchAllSightings() {
        long delayMs = firstDelayMs;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return fetchOnce();
            } catch (RestClientException e) {
                if (!isWorthRetrying(e) || attempt == MAX_ATTEMPTS) {
                    throw new TickApiException("Could not fetch sightings after " + attempt + " attempt(s)", e);
                }
                log.warn("Attempt {} of {} failed: {}. Retrying in {} ms", attempt, MAX_ATTEMPTS, e.getMessage(), delayMs);
                sleep(delayMs);
                delayMs = delayMs * 2;
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    // One single attempt to call the API, with no retrying
    private List<SightingApiDto> fetchOnce() {
        SightingApiDto[] sightings = restClient.get().uri("/tick-sightings").retrieve().body(SightingApiDto[].class);

        if (sightings == null) {
            return List.of();
        }
        return Arrays.asList(sightings);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TickApiException("Interrupted while waiting to retry", e);
        }
    }

    private boolean isWorthRetrying(RestClientException e) {
        return e instanceof HttpClientErrorException.TooManyRequests || e instanceof HttpServerErrorException || e instanceof ResourceAccessException;
    }

}
