package com.tick_tracker.client;

import java.util.Arrays;
import java.util.List;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component //
public class TickApiClient {

    private final RestClient restClient;

    public TickApiClient(@Value("${tick.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<SightingApiDto> fetchAllSightings() {
        SightingApiDto[] sightings = restClient.get().uri("/tick-sightings").retrieve().body(SightingApiDto[].class);

        if (sightings == null) {
            return List.of();
        }
        return Arrays.asList(sightings);
    }

}