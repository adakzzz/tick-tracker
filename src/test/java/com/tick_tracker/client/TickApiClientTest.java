package com.tick_tracker.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TickApiClientTest {

    private static final String URL = "http://fake-api/tick-sightings";

    private static final String ONE_SIGHTING = """
            [{"id":"a1","date":"2022-08-01T06:40:31","location":"London","species":"Marsh tick","latinName":"Ixodes apronophorus"}]
            """;

    private MockRestServiceServer fakeApi;
    private TickApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://fake-api");
        fakeApi = MockRestServiceServer.bindTo(builder).build();
        client = new TickApiClient(builder.build(), 0);
    }

    @Test
    void returnsSightingsWhenApiWorks() {
        // Arrange: script the fake API's reply
        fakeApi.expect(requestTo(URL)).andRespond(withSuccess(ONE_SIGHTING, MediaType.APPLICATION_JSON));

        // Act: call the real code
        List<SightingApiDto> result = client.fetchAllSightings();

        // Assert: check what came back
        assertEquals(1, result.size());
        assertEquals("London", result.getFirst().location());
        fakeApi.verify();
    }

    @Test
    void retriesAfterRateLimitThenSucceeds() {
        // First call gets 429, second call gets the data
        fakeApi.expect(requestTo(URL)).andRespond(withTooManyRequests());
        fakeApi.expect(requestTo(URL)).andRespond(withSuccess(ONE_SIGHTING, MediaType.APPLICATION_JSON));

        assertEquals(1, client.fetchAllSightings().size());
        fakeApi.verify();
    }

    @Test
    void doesNotRetryNotFound() {
        // Exactly one call, answered with 404
        fakeApi.expect(times(1), requestTo(URL)).andRespond(withResourceNotFound());

        assertThrows(TickApiException.class, () -> client.fetchAllSightings());
        fakeApi.verify();
    }

    @Test
    void givesUpAfterFourServerErrors() {
        // Four calls, every one answered with 500
        fakeApi.expect(times(4), requestTo(URL)).andRespond(withServerError());

        assertThrows(TickApiException.class, () -> client.fetchAllSightings());
        fakeApi.verify();
    }

}